/*
 * luminegram - Icon Packs page: master switch, installed packs, install from a file.
 * Based on exteraless/exteraless (app.exteraless.icons.IconPacksActivity), rewritten on LumineSubPage.
 */
package app.lumine.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.icons.LumineIconConfig;
import app.lumine.icons.LumineIconError;
import app.lumine.icons.LumineIconManager;
import app.lumine.icons.LumineIconObserver;
import app.lumine.icons.LumineIconPicker;
import app.lumine.icons.LumineIconPack;
import app.lumine.icons.LumineIconStorage;
import app.lumine.restart.LumineRestart;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.Switch;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LumineIconPacksActivity extends LumineSubPage {

    private static final int REQUEST_PICK = 5821;

    @Override
    protected int titleRes() {
        return R.string.LumineIconPacksTitle;
    }

    @Override
    protected void build(Context context, LinearLayout list) {
        LinearLayout main = LumineCards.card(context, list, LumineStrings.get(R.string.LumineIconPacksTitle));
        LumineCards.toggleRow(context, main, LumineStrings.get(R.string.LumineIconPacksEnable), null, LumineIconConfig.enabled(), enabled -> {
            LumineIconConfig.setEnabled(enabled);
            LumineIconManager.get().reload();
            LumineRestart.hint(this);
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineIconPacksEnableInfo));

        LinearLayout installed = LumineCards.card(context, list, LumineStrings.get(R.string.LumineIconPacksInstalled));
        List<LumineIconPack> packs = sorted(LumineIconStorage.installed());
        if (packs.isEmpty()) {
            LumineCards.row(context, installed, LumineStrings.get(R.string.LumineIconPacksEmpty), null);
        }
        for (LumineIconPack pack : packs) {
            addPackRow(context, installed, pack);
        }
        LumineIconRow.add(context, installed, R.drawable.msg_add, LumineStrings.get(R.string.LumineIconPacksInstall), null, v -> pick());
        LumineIconRow.add(context, installed, R.drawable.msg_edit, LumineStrings.get(R.string.LumineIconPacksCreate), null, v -> showCreateDialog());
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineIconPacksHint));
    }

    private static List<LumineIconPack> sorted(List<LumineIconPack> all) {
        List<String> active = LumineIconConfig.activePackIds();
        List<LumineIconPack> result = new ArrayList<>();
        for (String id : active) {
            for (LumineIconPack pack : all) {
                if (pack.getId().equals(id)) {
                    result.add(pack);
                    break;
                }
            }
        }
        for (LumineIconPack pack : all) {
            if (!active.contains(pack.getId())) {
                result.add(pack);
            }
        }
        return result;
    }

    private void addPackRow(Context context, LinearLayout card, LumineIconPack pack) {
        String count = String.format(LumineStrings.get(R.string.LumineIconPackCount), pack.getIconCount());
        String subtitle = pack.getAuthor().isEmpty() ? count : pack.getAuthor() + " · " + count;
        Switch toggle = LumineCards.toggleRow(context, card, pack.getName(), subtitle, LumineIconConfig.isPackActive(pack.getId()), enabled -> {
            LumineIconConfig.togglePack(pack.getId());
            LumineIconManager.get().reload();
            LumineRestart.hint(this);
            rebuild(200);
        });
        View row = (View) toggle.getParent();
        row.setOnLongClickListener(v -> {
            showPackMenu(pack);
            return true;
        });
    }

    private void showPackMenu(LumineIconPack pack) {
        if (getParentActivity() == null) {
            return;
        }
        final boolean active = LumineIconConfig.isPackActive(pack.getId());
        List<CharSequence> items = new ArrayList<>();
        List<Integer> actions = new ArrayList<>();
        if (active) {
            items.add(LumineStrings.get(R.string.LumineIconPackMoveUp));
            actions.add(0);
            items.add(LumineStrings.get(R.string.LumineIconPackMoveDown));
            actions.add(1);
        }
        items.add(LumineStrings.get(R.string.LumineIconPackEdit));
        actions.add(3);
        items.add(LocaleController.getString(R.string.Delete));
        actions.add(2);
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setItems(items.toArray(new CharSequence[0]), (dialog, which) -> {
            int action = actions.get(which);
            if (action == 2) {
                confirmDelete(pack);
                return;
            }
            if (action == 3) {
                startEditing(pack);
                return;
            }
            LumineIconConfig.movePack(pack.getId(), action == 0 ? -1 : 1);
            LumineIconManager.get().reload();
            LumineRestart.hint(this);
            rebuild(200);
        });
        showDialog(builder.create());
    }

    private void confirmDelete(LumineIconPack pack) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LumineIconPackDeleteTitle));
        builder.setMessage(String.format(LumineStrings.get(R.string.LumineIconPackDeleteInfo), pack.getName()));
        builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
            LumineIconManager.get().deletePack(pack.getId(), () -> rebuild(0));
            LumineRestart.hint(this);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        showDialog(dialog);
        View button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (button != null) {
            ((android.widget.TextView) button).setTextColor(Theme.getColor(Theme.key_text_RedBold));
        }
    }

    private void startEditing(LumineIconPack pack) {
        if (!LumineIconConfig.enabled()) {
            LumineIconConfig.setEnabled(true);
        }
        if (!LumineIconConfig.isPackActive(pack.getId())) {
            LumineIconConfig.togglePack(pack.getId());
        }
        LumineIconManager.get().reload();
        LumineIconObserver.clear();
        LumineIconConfig.setEditingPackId(pack.getId());
        rebuild(200);
        BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LumineStrings.get(R.string.LumineIconPackEditStarted)).show();
    }

    private void showCreateDialog() {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        FrameLayout fields = new FrameLayout(context);
        OutlineEditText name = field(context, LumineStrings.get(R.string.LumineIconPackName), "", EditorInfo.IME_ACTION_NEXT);
        OutlineEditText author = field(context, LumineStrings.get(R.string.LumineIconPackAuthor), "", EditorInfo.IME_ACTION_NEXT);
        OutlineEditText version = field(context, LumineStrings.get(R.string.LumineIconPackVersion), "1.0", EditorInfo.IME_ACTION_DONE);
        fields.addView(name, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58f, Gravity.LEFT | Gravity.TOP, 0f, 0f, 0f, 0f));
        fields.addView(author, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58f, Gravity.LEFT | Gravity.TOP, 0f, 68f, 0f, 0f));
        fields.addView(version, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58f, Gravity.LEFT | Gravity.TOP, 0f, 136f, 0f, 0f));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LumineStrings.get(R.string.LumineIconPacksCreate));
        builder.setView(fields, AndroidUtilities.dp(204));
        builder.setPositiveButton(LumineStrings.get(R.string.LumineIconPackCreateAction), (dialog, which) -> {
            final String packName = name.getEditText().getText().toString().trim();
            final String packAuthor = author.getEditText().getText().toString().trim();
            final String packVersion = version.getEditText().getText().toString().trim();
            if (packName.isEmpty()) {
                BulletinFactory.of(this).createErrorBulletin(LumineStrings.get(R.string.LumineIconPackNameEmpty)).show();
                return;
            }
            Utilities.globalQueue.postRunnable(() -> {
                LumineIconPack created = LumineIconManager.get().createPack(packName, packAuthor, packVersion.isEmpty() ? "1.0" : packVersion);
                AndroidUtilities.runOnUIThread(() -> {
                    if (created == null) {
                        BulletinFactory.of(this).createErrorBulletin(LumineStrings.get(R.string.LumineIconErrStorage)).show();
                        return;
                    }
                    rebuild(0);
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.done, LumineStrings.get(R.string.LumineIconPackCreated)).show();
                });
            });
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private static OutlineEditText field(Context context, String hint, String text, int ime) {
        OutlineEditText field = new OutlineEditText(context);
        field.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.getEditText().setImeOptions(ime);
        field.setHint(hint);
        field.getEditText().setText(text);
        return field;
    }

    private void pick() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_PICK);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_PICK || resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        final Uri uri = data.getData();
        Utilities.globalQueue.postRunnable(() -> {
            File temp = new File(ApplicationLoader.applicationContext.getCacheDir(), "lumine_icons_pick_" + System.currentTimeMillis() + LumineIconStorage.PACK_EXTENSION);
            LumineIconPack parsed = null;
            try (InputStream in = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri)) {
                if (in != null && AndroidUtilities.copyFile(in, temp)) {
                    parsed = LumineIconStorage.parseArchive(temp);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            final LumineIconPack pack = parsed;
            AndroidUtilities.runOnUIThread(() -> confirmInstall(temp, pack));
        });
    }

    private void confirmInstall(File archive, LumineIconPack pack) {
        if (getParentActivity() == null) {
            archive.delete();
            return;
        }
        if (pack == null) {
            archive.delete();
            BulletinFactory.of(this).createErrorBulletin(LumineStrings.get(R.string.LumineIconErrInvalidArchive)).show();
            return;
        }
        String message = pack.getAuthor().isEmpty()
                ? String.format(LumineStrings.get(R.string.LumineIconPackInstallInfoNoAuthor), pack.getName(), pack.getIconCount())
                : String.format(LumineStrings.get(R.string.LumineIconPackInstallInfo), pack.getName(), pack.getAuthor(), pack.getIconCount());
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LumineIconPackInstallTitle));
        builder.setMessage(message);
        builder.setPositiveButton(LumineStrings.get(R.string.LumineIconPackInstallAction), (dialog, which) -> install(archive));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> archive.delete());
        showDialog(builder.create());
    }

    private void install(File archive) {
        if (getParentActivity() == null) {
            archive.delete();
            return;
        }
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.setCanCancel(false);
        progress.show();
        LumineIconManager.get().installPack(archive, error -> {
            try {
                progress.dismiss();
            } catch (Exception ignore) {
            }
            archive.delete();
            if (getParentActivity() == null) {
                return;
            }
            if (error != null) {
                BulletinFactory.of(this).createErrorBulletin(message(error)).show();
                return;
            }
            rebuild(0);
            BulletinFactory.of(this).createSimpleBulletin(R.raw.done, LumineStrings.get(R.string.LumineIconPackInstalled)).show();
        });
    }

    private static String message(LumineIconError error) {
        int res;
        switch (error) {
            case INVALID_ARCHIVE:
                res = R.string.LumineIconErrInvalidArchive;
                break;
            case MISSING_METADATA:
                res = R.string.LumineIconErrMissingMetadata;
                break;
            case METADATA_TOO_LARGE:
                res = R.string.LumineIconErrMetadataTooLarge;
                break;
            case INVALID_METADATA:
                res = R.string.LumineIconErrInvalidMetadata;
                break;
            case TOO_MANY_FILES:
                res = R.string.LumineIconErrTooManyFiles;
                break;
            case ARCHIVE_TOO_LARGE:
                res = R.string.LumineIconErrArchiveTooLarge;
                break;
            case FILE_TOO_LARGE:
                res = R.string.LumineIconErrFileTooLarge;
                break;
            case COMPRESSION_RATIO_TOO_HIGH:
                res = R.string.LumineIconErrCompression;
                break;
            case STORAGE_ERROR:
                res = R.string.LumineIconErrStorage;
                break;
            default:
                res = R.string.LumineIconErrUnknown;
                break;
        }
        return LumineStrings.get(res);
    }
}
