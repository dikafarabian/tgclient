/*
 * luminegram - decides what is saved.
 * Mode 0 saves everything except the marked chats, mode 1 saves only the marked chats.
 */
package app.lumine.saving;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public final class LumineSavePolicy {

    private LumineSavePolicy() {
    }

    public static boolean deleted(int account, long dialogId, long fromUserId) {
        if (!LumineSaving.deleted()) {
            return false;
        }
        final boolean marked = LumineSaving.excludedDeleted(dialogId) || fromUserId != 0 && LumineSaving.excludedDeleted(fromUserId);
        if (LumineSaving.deletedMode() == 1 ? !marked : marked) {
            return false;
        }
        if (!deletedForKind(account, dialogId) || !inFolder(account, dialogId)) {
            return false;
        }
        final MessagesController controller = MessagesController.getInstance(account);
        if (fromUserId != 0) {
            final TLRPC.User from = controller.getUser(fromUserId);
            if (from != null) {
                return !from.bot || LumineSaving.botUser();
            }
        }
        if (dialogId > 0) {
            final TLRPC.User peer = controller.getUser(dialogId);
            if (peer != null) {
                return !peer.bot || LumineSaving.botChat();
            }
        }
        return true;
    }

    public static boolean edited(int account, long dialogId, long fromUserId) {
        if (!LumineSaving.edits()) {
            return false;
        }
        final boolean marked = LumineSaving.excludedEdited(dialogId) || fromUserId != 0 && LumineSaving.excludedEdited(fromUserId);
        if (LumineSaving.editedMode() == 1 ? !marked : marked) {
            return false;
        }
        return inFolder(account, dialogId);
    }

    public static boolean keepsMedia(int account, long dialogId) {
        return LumineSaving.deleted() && mediaForKind(account, dialogId);
    }

    public static boolean mediaFor(int account, long dialogId, TLRPC.Message message) {
        return message != null && message.media != null && mediaForKind(account, dialogId);
    }

    private static boolean mediaForKind(int account, long dialogId) {
        if (!LumineSaving.media()) {
            return false;
        }
        if (DialogObject.isUserDialog(dialogId)) {
            return LumineSaving.mediaPrivateChats();
        }
        final TLRPC.Chat chat = MessagesController.getInstance(account).getChat(Math.abs(dialogId));
        if (chat == null) {
            return true;
        }
        final boolean isPublic = ChatObject.isPublic(chat);
        if (ChatObject.isChannelAndNotMegaGroup(chat)) {
            return isPublic ? LumineSaving.mediaPublicChannels() : LumineSaving.mediaPrivateChannels();
        }
        return isPublic ? LumineSaving.mediaPublicGroups() : LumineSaving.mediaPrivateGroups();
    }

    private static boolean deletedForKind(int account, long dialogId) {
        if (dialogId >= 0) {
            return LumineSaving.inPrivateChats();
        }
        final TLRPC.Chat chat = MessagesController.getInstance(account).getChat(-dialogId);
        if (chat == null) {
            return true;
        }
        return ChatObject.isChannelAndNotMegaGroup(chat) ? LumineSaving.inChannels() : LumineSaving.inGroups();
    }

    private static boolean inFolder(int account, long dialogId) {
        if (LumineSaving.archived()) {
            return true;
        }
        try {
            final TLRPC.Dialog dialog = MessagesController.getInstance(account).dialogs_dict.get(dialogId);
            return dialog == null || dialog.folder_id != 1;
        } catch (Exception e) {
            return true;
        }
    }
}
