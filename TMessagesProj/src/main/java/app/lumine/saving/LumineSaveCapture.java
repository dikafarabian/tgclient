/*
 * luminegram - saves deleted and edited messages.
 * The hooks in MessagesController and MessagesStorage call these methods; the work runs off the UI thread.
 */
package app.lumine.saving;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.collection.LongSparseArray;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Locale;

public final class LumineSaveCapture {

    private LumineSaveCapture() {
    }

    public static boolean keepsMedia(int account, long dialogId) {
        return LumineSavePolicy.keepsMedia(account, dialogId);
    }

    public static void onServerDeleted(int account, LongSparseArray<ArrayList<Integer>> deleted) {
        if (!LumineSaving.deleted() || deleted == null || deleted.size() == 0) {
            return;
        }
        final MessagesStorage storage = MessagesStorage.getInstance(account);
        storage.getStorageQueue().postRunnable(() -> {
            final int catchTime = (int) (System.currentTimeMillis() / 1000L);
            for (int a = 0, size = deleted.size(); a < size; a++) {
                final long key = deleted.keyAt(a);
                final ArrayList<Integer> ids = deleted.valueAt(a);
                if (ids == null || ids.isEmpty()) {
                    continue;
                }
                readMessages(storage, key, ids, catchTime, account);
            }
        });
    }

    private static void readMessages(MessagesStorage storage, long key, ArrayList<Integer> ids, int catchTime, int account) {
        SQLiteCursor cursor = null;
        try {
            final String list = TextUtils.join(",", ids);
            final String where = key != 0 ? String.format(Locale.US, "uid = %d", key) : "is_channel = 0";
            cursor = storage.getDatabase().queryFinalized(String.format(Locale.US, "SELECT uid, data FROM messages_v2 WHERE mid IN (%s) AND %s", list, where));
            final ArrayList<Object[]> found = new ArrayList<>();
            while (cursor.next()) {
                final long dialogId = cursor.longValue(0);
                final NativeByteBuffer data = cursor.byteBufferValue(1);
                if (data == null) {
                    continue;
                }
                final TLRPC.Message message = TLRPC.Message.TLdeserialize(data, data.readInt32(false), false);
                if (message != null) {
                    message.readAttachPath(data, UserConfig.getInstance(account).clientUserId);
                    found.add(new Object[]{dialogId, message});
                }
                data.reuse();
            }
            cursor.dispose();
            cursor = null;
            for (Object[] item : found) {
                final long dialogId = (Long) item[0];
                final TLRPC.Message message = (TLRPC.Message) item[1];
                final long topicId = MessageObject.getTopicId(account, message, MessagesController.getInstance(account).isForum(dialogId));
                final LumineSaveMapper.Saved saved = new LumineSaveMapper.Saved(message, account, dialogId, topicId, catchTime);
                Utilities.globalQueue.postRunnable(() -> saveDeleted(saved));
            }
        } catch (Exception e) {
            FileLog.e("reading deleted messages failed", e);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }
    }

    private static void saveDeleted(LumineSaveMapper.Saved saved) {
        try {
            if (!LumineSavePolicy.deleted(saved.account, saved.dialogId, saved.fromUserId())) {
                return;
            }
            final SQLiteDatabase db = LumineSaveDb.get().getWritableDatabase();
            if (existsDeleted(db, saved)) {
                return;
            }
            final ContentValues values = LumineSaveMapper.map(saved);
            LumineSaveMapper.mapMedia(values, saved, true);
            final long fakeId = db.insert(LumineSaveDb.DELETED, null, values);
            if (fakeId > 0 && saved.message.reactions != null) {
                saveReactions(db, fakeId, saved.message.reactions);
            }
        } catch (Exception e) {
            FileLog.e("saving a deleted message failed", e);
        }
    }

    private static boolean existsDeleted(SQLiteDatabase db, LumineSaveMapper.Saved saved) {
        try (Cursor cursor = db.query(LumineSaveDb.DELETED, new String[]{"fakeId"},
                "userId = ? AND dialogId = ? AND topicId = ? AND messageId = ?",
                new String[]{String.valueOf(saved.userId), String.valueOf(saved.dialogId), String.valueOf(saved.topicId), String.valueOf(saved.messageId)},
                null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    private static void saveReactions(SQLiteDatabase db, long fakeMessageId, TLRPC.TL_messageReactions reactions) {
        if (reactions.results == null) {
            return;
        }
        for (TLRPC.ReactionCount reaction : reactions.results) {
            if (reaction.reaction instanceof TLRPC.TL_reactionEmpty) {
                continue;
            }
            final ContentValues values = new ContentValues();
            values.put("deletedMessageId", fakeMessageId);
            values.put("count", reaction.count);
            values.put("selfSelected", reaction.chosen ? 1 : 0);
            values.put("documentId", 0L);
            values.put("isCustom", 0);
            if (reaction.reaction instanceof TLRPC.TL_reactionEmoji) {
                values.put("emoticon", ((TLRPC.TL_reactionEmoji) reaction.reaction).emoticon);
            } else if (reaction.reaction instanceof TLRPC.TL_reactionCustomEmoji) {
                values.put("documentId", ((TLRPC.TL_reactionCustomEmoji) reaction.reaction).document_id);
                values.put("isCustom", 1);
            } else {
                continue;
            }
            db.insert(LumineSaveDb.DELETED_REACTION, null, values);
        }
    }

    public static void onEdited(int account, long dialogId, TLRPC.Message oldMessage, TLRPC.Message newMessage, boolean sameMedia) {
        try {
            if (!LumineSaving.edits() || oldMessage == null || newMessage == null || newMessage.from_id == null) {
                return;
            }
            if (sameMedia && TextUtils.equals(oldMessage.message, newMessage.message)) {
                return;
            }
            final long fromUserId = oldMessage.from_id == null ? 0 : oldMessage.from_id.user_id;
            if (!LumineSavePolicy.edited(account, dialogId, fromUserId)) {
                return;
            }
            final long topicId = MessageObject.getTopicId(account, oldMessage, false);
            final LumineSaveMapper.Saved saved = new LumineSaveMapper.Saved(oldMessage, account, dialogId, topicId, (int) (System.currentTimeMillis() / 1000L));
            final ContentValues values = LumineSaveMapper.map(saved);
            LumineSaveMapper.mapMedia(values, saved, !sameMedia);
            Utilities.globalQueue.postRunnable(() -> saveEdited(saved, values, sameMedia));
        } catch (Exception e) {
            FileLog.e("saving an edit failed", e);
        }
    }

    private static void saveEdited(LumineSaveMapper.Saved saved, ContentValues values, boolean sameMedia) {
        try {
            final SQLiteDatabase db = LumineSaveDb.get().getWritableDatabase();
            final String newPath = values.getAsString("mediaPath");
            if (!sameMedia && !TextUtils.isEmpty(newPath)) {
                final String last = lastRevisionPath(db, saved);
                if (!TextUtils.isEmpty(last) && !TextUtils.equals(last, newPath) && !LumineSaveMapper.isInAttachments(last)) {
                    final ContentValues update = new ContentValues();
                    update.put("mediaPath", newPath);
                    db.update(LumineSaveDb.EDITED, update, "userId = ? AND dialogId = ? AND messageId = ? AND mediaPath = ?",
                            new String[]{String.valueOf(saved.userId), String.valueOf(saved.dialogId), String.valueOf(saved.messageId), last});
                }
            }
            db.insert(LumineSaveDb.EDITED, null, values);
        } catch (Exception e) {
            FileLog.e("saving an edit failed", e);
        }
    }

    private static String lastRevisionPath(SQLiteDatabase db, LumineSaveMapper.Saved saved) {
        try (Cursor cursor = db.query(LumineSaveDb.EDITED, new String[]{"mediaPath"},
                "userId = ? AND dialogId = ? AND messageId = ?",
                new String[]{String.valueOf(saved.userId), String.valueOf(saved.dialogId), String.valueOf(saved.messageId)},
                null, null, "entityCreateDate DESC, fakeId DESC", "1")) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }
}
