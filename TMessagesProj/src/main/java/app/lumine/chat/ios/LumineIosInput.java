/*
 * luminegram - iOS style message input.
 * All logic of the iOS input lives here. ChatActivityEnterView only asks
 * isIOSInputStyle() and, in iOS mode, calls into this class right after the
 * stock statement it changes. Helpers take views as parameters so no field of
 * the enter view has to be opened.
 */
package app.lumine.chat.ios;

import app.lumine.chat.LumineChatConfig;
import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;

public final class LumineIosInput {

    private LumineIosInput() {
    }

    public static boolean active(ChatActivity fragment) {
        if (!LumineChatConfig.getIosInput() || fragment == null || fragment.isInPreviewMode()) {
            return false;
        }
        final TLRPC.Chat chat = fragment.getCurrentChat();
        if (chat == null || fragment.getCurrentEncryptedChat() != null) {
            return true;
        }
        final boolean broadcast = (chat instanceof TLRPC.TL_channel || chat instanceof TLRPC.TL_channelForbidden) && !chat.megagroup && !chat.gigagroup;
        return !broadcast || ChatObject.hasAdminRights(chat);
    }
}
