/*
 * luminegram - Ghost Mode on the request path.
 * ConnectionsManager asks blocks() before a request leaves and wraps its callback with wrap().
 */
package app.lumine.ghost;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_stories;

public final class LumineGhostRequests {

    private static final int OFFLINE_DELAY_MS = 1000;

    private LumineGhostRequests() {
    }

    public static boolean blocks(TLObject object, RequestDelegate onComplete) {
        if (LumineGhost.get(LumineGhost.TYPING)
                && (object instanceof TLRPC.TL_messages_setTyping || object instanceof TLRPC.TL_messages_setEncryptedTyping)) {
            return true;
        }
        if (LumineGhost.get(LumineGhost.READ_MESSAGES)) {
            if (object instanceof TLRPC.TL_messages_getMessagesViews) {
                final TLRPC.TL_messages_getMessagesViews views = (TLRPC.TL_messages_getMessagesViews) object;
                if (views.increment && !LumineGhost.consumeAllowedRead()) {
                    views.increment = false;
                }
            } else if (isReadMessageRequest(object) && !LumineGhost.consumeAllowedRead()) {
                FileLog.d("Ghost: blocked " + object.getClass().getSimpleName());
                answerRead(object, onComplete);
                return true;
            }
        }
        if ((LumineGhost.get(LumineGhost.READ_STORIES) || LumineGhost.storyGhostSession) && isReadStoriesRequest(object)) {
            return true;
        }
        if (LumineGhost.get(LumineGhost.ONLINE) && object instanceof TL_account.updateStatus) {
            ((TL_account.updateStatus) object).offline = true;
        }
        return false;
    }

    public static RequestDelegate wrap(int account, TLObject object, RequestDelegate onComplete) {
        if (LumineGhost.markReadAfterSend() && LumineGhost.get(LumineGhost.READ_MESSAGES) && isMessageSendRequest(object)) {
            final TLRPC.InputPeer peer = sendPeer(object);
            if (peer != null) {
                markReadAfterSend(account, peer);
            }
        }
        if (LumineGhost.get(LumineGhost.OFFLINE_AFTER_SEND) && isOnlineRequest(object)) {
            if (onComplete == null) {
                Utilities.globalQueue.postRunnable(() -> sendStatus(account, true), OFFLINE_DELAY_MS);
                return null;
            }
            return (response, error) -> {
                Utilities.stageQueue.postRunnable(() -> onComplete.run(response, error));
                Utilities.globalQueue.postRunnable(() -> sendStatus(account, true), OFFLINE_DELAY_MS);
            };
        }
        return onComplete;
    }

    public static void sendStatus(int account, boolean offline) {
        final TL_account.updateStatus request = new TL_account.updateStatus();
        request.offline = offline;
        ConnectionsManager.getInstance(account).sendRequest(request, (response, error) -> {
        });
    }

    private static void markReadAfterSend(int account, TLRPC.InputPeer peer) {
        final long dialogId = dialogId(peer);
        final MessagesStorage storage = MessagesStorage.getInstance(account);
        storage.getDialogMaxMessageId(dialogId, maxId -> {
            if (maxId == 0) {
                return;
            }
            final TLObject request;
            if (peer instanceof TLRPC.TL_inputPeerChannel) {
                final TLRPC.TL_channels_readHistory channelRead = new TLRPC.TL_channels_readHistory();
                channelRead.channel = MessagesController.getInputChannel(peer);
                channelRead.max_id = maxId;
                request = channelRead;
            } else {
                final TLRPC.TL_messages_readHistory read = new TLRPC.TL_messages_readHistory();
                read.peer = peer;
                read.max_id = maxId;
                request = read;
            }
            LumineGhost.allowNextRead();
            ConnectionsManager.getInstance(account).sendRequest(request, (response, error) -> {
                if (error == null && response instanceof TLRPC.TL_messages_affectedMessages) {
                    final TLRPC.TL_messages_affectedMessages affected = (TLRPC.TL_messages_affectedMessages) response;
                    MessagesController.getInstance(account).processNewDifferenceParams(-1, affected.pts, -1, affected.pts_count);
                }
            });
        });
    }

    private static long dialogId(TLRPC.InputPeer peer) {
        if (peer.chat_id != 0) {
            return -peer.chat_id;
        } else if (peer.channel_id != 0) {
            return -peer.channel_id;
        }
        return peer.user_id;
    }

    private static void answerRead(TLObject request, RequestDelegate onComplete) {
        if (onComplete == null) {
            return;
        }
        final TLObject fake;
        if (request instanceof TLRPC.TL_messages_readHistory || request instanceof TLRPC.TL_messages_readMessageContents) {
            final TLRPC.TL_messages_affectedMessages affected = new TLRPC.TL_messages_affectedMessages();
            affected.pts = -1;
            affected.pts_count = 0;
            fake = affected;
        } else {
            fake = new TLRPC.TL_boolTrue();
        }
        Utilities.stageQueue.postRunnable(() -> {
            try {
                onComplete.run(fake, null);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    private static TLRPC.InputPeer sendPeer(TLObject object) {
        if (object instanceof TLRPC.TL_messages_sendMessage) {
            return ((TLRPC.TL_messages_sendMessage) object).peer;
        } else if (object instanceof TLRPC.TL_messages_sendMedia) {
            return ((TLRPC.TL_messages_sendMedia) object).peer;
        } else if (object instanceof TLRPC.TL_messages_sendMultiMedia) {
            return ((TLRPC.TL_messages_sendMultiMedia) object).peer;
        } else if (object instanceof TLRPC.TL_messages_forwardMessages) {
            return ((TLRPC.TL_messages_forwardMessages) object).to_peer;
        } else if (object instanceof TLRPC.TL_messages_sendInlineBotResult) {
            return ((TLRPC.TL_messages_sendInlineBotResult) object).peer;
        } else if (object instanceof TLRPC.TL_messages_sendScheduledMessages) {
            return ((TLRPC.TL_messages_sendScheduledMessages) object).peer;
        }
        return null;
    }

    private static boolean isReadMessageRequest(TLObject object) {
        return object instanceof TLRPC.TL_messages_readHistory
                || object instanceof TLRPC.TL_messages_readEncryptedHistory
                || object instanceof TLRPC.TL_messages_readDiscussion
                || object instanceof TLRPC.TL_messages_readMessageContents
                || object instanceof TLRPC.TL_channels_readMessageContents
                || object instanceof TLRPC.TL_channels_readHistory;
    }

    private static boolean isReadStoriesRequest(TLObject object) {
        return object instanceof TL_stories.TL_stories_readStories
                || object instanceof TL_stories.TL_stories_incrementStoryViews;
    }

    private static boolean isMessageSendRequest(TLObject object) {
        return object instanceof TLRPC.TL_messages_sendMessage
                || object instanceof TLRPC.TL_messages_sendMedia
                || object instanceof TLRPC.TL_messages_sendMultiMedia
                || object instanceof TLRPC.TL_messages_forwardMessages
                || object instanceof TLRPC.TL_messages_sendInlineBotResult
                || object instanceof TLRPC.TL_messages_sendScheduledMessages;
    }

    private static boolean isOnlineRequest(TLObject object) {
        return isMessageSendRequest(object)
                || object instanceof TLRPC.TL_messages_editMessage
                || object instanceof TLRPC.TL_messages_sendEncrypted
                || object instanceof TLRPC.TL_messages_sendEncryptedFile
                || object instanceof TLRPC.TL_messages_sendEncryptedMultiMedia
                || object instanceof TLRPC.TL_messages_sendEncryptedService;
    }
}
