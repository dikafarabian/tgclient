/*
 * luminegram - live icon picker: a floating button that lists the icons used on the current screen
 * and lets the owner replace or reset one of them in the pack being edited.
 * Based on exteraless/exteraless (app.exteraless.icons.picker), rewritten with stock sheets.
 */
package app.lumine.icons;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.lumine.LumineStrings;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LumineIconPicker {

    private static FrameLayout button;
    private static boolean scheduled;
    private static BottomSheet listSheet;

    private LumineIconPicker() {
    }

    private static final class Entry {
        final int resId;
        final String name;

        Entry(int resId, String name) {
            this.resId = resId;
            this.name = name;
        }
    }

    public static void show(LaunchActivity activity) {
        if (scheduled || (button != null && button.getParent() != null)) {
            return;
        }
        scheduled = true;
        AndroidUtilities.runOnUIThread(() -> {
            scheduled = false;
            attach(activity);
        });
    }

    public static void hide() {
        FrameLayout view = button;
        button = null;
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        if (listSheet != null) {
            listSheet.dismiss();
            listSheet = null;
        }
    }

    public static void finishEditing() {
        LumineIconConfig.setEditingPackId(null);
        LumineIconObserver.clear();
        hide();
    }

    private static void attach(LaunchActivity activity) {
        if (LumineIconConfig.editingPackId() == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (button != null && button.getParent() != null) {
            return;
        }
        FrameLayout container = activity.getMainContainerFrameLayout();
        if (container == null) {
            return;
        }
        LumineIconObserver.setSuspended(true);
        try {
            FrameLayout view = new FrameLayout(activity);
            view.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56),
                    Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground)));
            ImageView icon = new ImageView(activity);
            icon.setScaleType(ImageView.ScaleType.CENTER);
            icon.setImageResource(R.drawable.msg_edit);
            icon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.SRC_IN));
            view.addView(icon, LayoutHelper.createFrame(56, 56));
            view.setElevation(AndroidUtilities.dp(4));
            view.setOnClickListener(v -> openList(activity));
            view.setOnLongClickListener(v -> {
                finishEditing();
                return true;
            });
            container.addView(view, LayoutHelper.createFrame(56, 56, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 16, 96));
            button = view;
        } catch (Throwable t) {
            FileLog.e("lumine icons: cannot show picker", t);
            button = null;
        } finally {
            LumineIconObserver.setSuspended(false);
        }
    }

    private static List<Entry> entries() {
        LumineIconManager manager = LumineIconManager.get();
        List<Entry> result = new ArrayList<>();
        for (int resId : LumineIconObserver.used()) {
            String name = manager.resourceName(resId);
            if (name != null && !LumineIconManager.isBlacklisted(name)) {
                result.add(new Entry(resId, name));
            }
        }
        Collections.sort(result, (a, b) -> a.name.compareTo(b.name));
        return result;
    }

    private static void openList(Context context) {
        final String packId = LumineIconConfig.editingPackId();
        final LumineIconPack pack = packId == null ? null : LumineIconStorage.find(packId);
        if (pack == null) {
            finishEditing();
            return;
        }
        final List<Entry> items = entries();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 18);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setText(pack.getName());
        title.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(16), AndroidUtilities.dp(21), AndroidUtilities.dp(4));
        root.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView sub = new TextView(context);
        sub.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 14);
        sub.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        sub.setText(items.isEmpty() ? LumineStrings.get(R.string.LumineIconPickerEmpty) : LumineStrings.get(R.string.LumineIconPickerHint));
        sub.setPadding(AndroidUtilities.dp(21), 0, AndroidUtilities.dp(21), AndroidUtilities.dp(8));
        root.addView(sub, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        final BottomSheet.Builder builder = new BottomSheet.Builder(context);
        if (!items.isEmpty()) {
            RecyclerListView grid = new RecyclerListView(context);
            grid.setLayoutManager(new GridLayoutManager(context, 4));
            grid.setAdapter(new IconAdapter(context, items, pack));
            grid.setOnItemClickListener((view, position) -> {
                Entry entry = items.get(position);
                if (listSheet != null) {
                    listSheet.dismiss();
                }
                openIcon(context, packId, entry);
            });
            root.addView(grid, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 320));
        }

        TextView finish = new TextView(context);
        finish.setGravity(Gravity.CENTER);
        finish.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 15);
        finish.setTypeface(AndroidUtilities.bold());
        finish.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        finish.setText(LumineStrings.get(R.string.LumineIconPickerFinish));
        finish.setOnClickListener(v -> finishEditing());
        root.addView(finish, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        builder.setCustomView(root);
        listSheet = builder.show();
    }

    private static void openIcon(Context context, String packId, Entry entry) {
        LumineIconPack pack = LumineIconStorage.find(packId);
        if (pack == null) {
            return;
        }
        final boolean replaced = pack.getIcons().containsKey(entry.name);
        List<CharSequence> labels = new ArrayList<>();
        labels.add(LumineStrings.get(R.string.LumineIconPickerChoose));
        if (replaced) {
            labels.add(LumineStrings.get(R.string.LumineIconPickerReset));
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setTitle(entry.name, true);
        builder.setItems(labels.toArray(new CharSequence[0]), (dialog, which) -> {
            if (which == 0) {
                LumineIconPickActivity.start(context, file -> applyPicked(packId, entry, file));
            } else {
                Utilities.globalQueue.postRunnable(() -> {
                    boolean ok = LumineIconManager.get().resetIcon(packId, entry.resId);
                    AndroidUtilities.runOnUIThread(() -> report(ok, R.string.LumineIconPickerResetDone));
                });
            }
        });
        builder.show();
    }

    private static void applyPicked(String packId, Entry entry, File file) {
        if (file == null) {
            return;
        }
        Utilities.globalQueue.postRunnable(() -> {
            boolean ok = LumineIconManager.get().saveIcon(packId, entry.resId, file);
            file.delete();
            AndroidUtilities.runOnUIThread(() -> report(ok, R.string.LumineIconPickerSaved));
        });
    }

    private static void report(boolean ok, int doneRes) {
        BaseFragment fragment = LaunchActivity.getSafeLastFragment();
        if (fragment == null) {
            return;
        }
        if (ok) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, LumineStrings.get(doneRes)).show();
        } else {
            BulletinFactory.of(fragment).createErrorBulletin(LumineStrings.get(R.string.LumineIconErrUnknown)).show();
        }
    }

    private static final class IconAdapter extends RecyclerListView.SelectionAdapter {

        private final Context context;
        private final List<Entry> items;
        private final LumineIconPack pack;

        IconAdapter(Context context, List<Entry> items, LumineIconPack pack) {
            this.context = context;
            this.items = items;
            this.pack = pack;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout cell = new LinearLayout(context);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(10), AndroidUtilities.dp(4), AndroidUtilities.dp(6));
            ImageView image = new ImageView(context);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cell.addView(image, LayoutHelper.createLinear(40, 40, Gravity.CENTER_HORIZONTAL));
            TextView label = new TextView(context);
            label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 11);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            cell.addView(label, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));
            cell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Entry entry = items.get(position);
            LinearLayout cell = (LinearLayout) holder.itemView;
            ImageView image = (ImageView) cell.getChildAt(0);
            TextView label = (TextView) cell.getChildAt(1);
            LumineIconObserver.setSuspended(true);
            try {
                Drawable drawable = context.getResources().getDrawable(entry.resId);
                if (drawable != null) {
                    drawable = drawable.mutate();
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.SRC_IN));
                }
                image.setImageDrawable(drawable);
            } catch (Throwable t) {
                image.setImageDrawable(null);
            } finally {
                LumineIconObserver.setSuspended(false);
            }
            label.setText(entry.name);
            label.setTextColor(Theme.getColor(pack.getIcons().containsKey(entry.name) ? Theme.key_dialogTextBlue : Theme.key_dialogTextGray2));
        }
    }
}
