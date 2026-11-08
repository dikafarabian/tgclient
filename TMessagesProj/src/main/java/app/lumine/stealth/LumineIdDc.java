/*
 * luminegram - ID and data center on the profile page.
 * The profile hooks call these; the row logic stays out of ProfileActivity.
 */
package app.lumine.stealth;

import java.util.Locale;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;

public final class LumineIdDc {

    private LumineIdDc() {
    }

    public static String dcName(int dc) {
        switch (dc) {
            case 1:
                return "Pluto";
            case 2:
                return "Venus";
            case 3:
                return "Aurora";
            case 4:
                return "Vesta";
            case 5:
                return "Flora";
            default:
                return "Unknown";
        }
    }

    public static String dcLocation(int dc) {
        switch (dc) {
            case 1:
            case 3:
                return "Miami";
            case 2:
            case 4:
                return "Amsterdam";
            case 5:
                return "Singapore";
            default:
                return "Unknown";
        }
    }

    public static String dcLine(int dc) {
        if (dc == 0) {
            return "DC " + LocaleController.getString(R.string.NumberUnknown);
        }
        return String.format(Locale.US, "DC%d %s, %s", dc, dcName(dc), dcLocation(dc));
    }

    public static long displayId(long id, TLRPC.Chat chat) {
        if (chat != null && LumineStealth.idDcType() == LumineStealth.ID_BOT_API) {
            return ChatObject.isChannel(chat) ? -1000000000000L - chat.id : -chat.id;
        }
        return id;
    }

    public static boolean enabled() {
        return LumineStealth.idDcType() != LumineStealth.ID_HIDDEN;
    }

    public static String idText(int account, long userId, long chatId) {
        final TLRPC.Chat chat = chatId != 0 ? MessagesController.getInstance(account).getChat(chatId) : null;
        return String.valueOf(displayId(userId != 0 ? userId : chatId, chat));
    }

    public static int dc(int account, long userId, long chatId, TLRPC.UserFull userInfo, TLRPC.ChatFull chatInfo) {
        final MessagesController controller = MessagesController.getInstance(account);
        if (userId != 0 || userInfo != null) {
            final TLRPC.User user = userInfo != null && userInfo.user != null ? userInfo.user : controller.getUser(userId);
            if (user == null) {
                return 0;
            }
            if (UserObject.isUserSelf(user)) {
                return ConnectionsManager.getInstance(account).getCurrentDatacenterId();
            }
            return user.photo != null ? user.photo.dc_id : 0;
        }
        int dc = chatInfo != null ? chatInfo.stats_dc : 0;
        if (dc == 0 && chatId != 0) {
            final TLRPC.Chat chat = controller.getChat(chatId);
            if (chat != null && chat.photo != null) {
                dc = chat.photo.dc_id;
            }
        }
        return dc;
    }

    public static void showSheet(BaseFragment fragment, long userId, long chatId) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        final int account = fragment.getCurrentAccount();
        final String shown = idText(account, userId, chatId);
        final long rawId = userId != 0 ? userId : chatId;

        final java.util.ArrayList<CharSequence> names = new java.util.ArrayList<>();
        final java.util.ArrayList<Runnable> actions = new java.util.ArrayList<>();
        final String copyLink = LocaleController.getString(R.string.CopyLink);

        names.add(LocaleController.getString(R.string.Copy));
        actions.add(() -> {
            AndroidUtilities.addToClipboard(shown);
            BulletinFactory.of(fragment).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
        });
        if (userId != 0) {
            names.add(copyLink);
            actions.add(() -> copyLink(fragment, "tg://user?id=" + rawId));
            names.add(copyLink + " (Android)");
            actions.add(() -> copyLink(fragment, "tg://openmessage?user_id=" + rawId));
            names.add(copyLink + " (iOS)");
            actions.add(() -> copyLink(fragment, "https://t.me/@id" + rawId));
        } else {
            names.add(copyLink + " (Android)");
            actions.add(() -> copyLink(fragment, "tg://openmessage?chat_id=" + rawId));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getParentActivity());
        builder.setTitle(shown);
        builder.setItems(names.toArray(new CharSequence[0]), (dialog, which) -> actions.get(which).run());
        fragment.showDialog(builder.create());
    }

    private static void copyLink(BaseFragment fragment, String link) {
        AndroidUtilities.addToClipboard(link);
        BulletinFactory.of(fragment).createCopyBulletin(LocaleController.getString(R.string.LinkCopied)).show();
    }
}
