/*
 * luminegram - turns a TLRPC.Message into the columns of the saved message tables and keeps its media file.
 */
package app.lumine.saving;

import android.content.ContentValues;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public final class LumineSaveMapper {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_STICKER = 2;
    public static final int TYPE_FILE = 3;
    public static final int TYPE_WEBPAGE = 4;
    public static final int TYPE_STORY = 5;

    private static final String ATTACHMENTS = "Saved Attachments";
    private static final String NAME_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static final class Saved {
        public final TLRPC.Message message;
        public final int account;
        public final long userId;
        public final long dialogId;
        public final long topicId;
        public final int messageId;
        public final int catchTime;

        public Saved(TLRPC.Message message, int account, long dialogId, long topicId, int catchTime) {
            this.message = message;
            this.account = account;
            this.userId = UserConfig.getInstance(account).getClientUserId();
            this.dialogId = dialogId;
            this.topicId = topicId;
            this.messageId = message.id;
            this.catchTime = catchTime;
        }

        public long fromUserId() {
            return message.from_id == null ? 0 : message.from_id.user_id;
        }
    }

    private LumineSaveMapper() {
    }

    public static File attachmentsDir() {
        File base = null;
        try {
            base = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        } catch (Exception ignore) {
        }
        if (base == null) {
            base = ApplicationLoader.applicationContext.getFilesDir();
        }
        final File dir = new File(base, ATTACHMENTS);
        if (dir.exists() || dir.mkdirs()) {
            try {
                AndroidUtilities.createEmptyFile(new File(dir, ".nomedia"));
            } catch (Exception ignore) {
            }
        }
        return dir;
    }

    public static boolean isInAttachments(String path) {
        return path != null && path.contains(File.separator + ATTACHMENTS + File.separator);
    }

    public static ContentValues map(Saved saved) {
        final TLRPC.Message message = saved.message;
        final ContentValues values = new ContentValues();
        values.put("userId", saved.userId);
        values.put("dialogId", saved.dialogId);
        values.put("groupedId", message.grouped_id);
        values.put("peerId", MessageObject.getPeerId(message.peer_id));
        values.put("fromId", MessageObject.getPeerId(message.from_id));
        values.put("topicId", saved.topicId);
        values.put("messageId", message.id);
        values.put("date", message.date);
        values.put("flags", message.flags);
        values.put("editDate", message.edit_date);
        values.put("views", message.views);
        values.put("forwards", message.forwards);
        values.put("fwdFlags", 0);
        values.put("fwdFromId", 0L);
        values.put("fwdDate", 0);
        final TLRPC.MessageFwdHeader fwd = message.fwd_from;
        if (fwd != null) {
            values.put("fwdFlags", fwd.flags);
            values.put("fwdFromId", MessageObject.getPeerId(fwd.from_id));
            values.put("fwdName", fwd.from_name);
            values.put("fwdDate", fwd.date);
            values.put("fwdPostAuthor", fwd.post_author);
        }
        values.put("replyFlags", 0);
        values.put("replyMessageId", 0);
        values.put("replyPeerId", 0L);
        values.put("replyTopId", 0);
        values.put("replyForumTopic", 0);
        values.put("replyQuote", 0);
        final TLRPC.MessageReplyHeader reply = message.reply_to;
        if (reply != null) {
            values.put("replyFlags", reply.flags);
            values.put("replyMessageId", reply.reply_to_msg_id);
            values.put("replyPeerId", MessageObject.getPeerId(reply.reply_to_peer_id));
            values.put("replyTopId", reply.reply_to_top_id);
            values.put("replyForumTopic", reply.forum_topic ? 1 : 0);
            values.put("replyQuote", reply.quote ? 1 : 0);
            values.put("replyQuoteText", reply.quote_text);
            values.put("replyQuoteEntities", serializeMultiple(reply.quote_entities));
            values.put("replyFromSerialized", serialize(reply.reply_from));
        }
        values.put("entityCreateDate", saved.catchTime);
        values.put("text", message.message);
        values.put("textEntities", serializeMultiple(message.entities));
        values.put("replyMarkupSerialized", serialize(message.reply_markup));
        values.put("documentType", TYPE_NONE);
        return values;
    }

    public static void mapMedia(ContentValues values, Saved saved, boolean moveFile) {
        final TLRPC.Message message = saved.message;
        if (!LumineSavePolicy.mediaFor(saved.account, saved.dialogId, message)) {
            return;
        }
        final TLRPC.MessageMedia media = message.media;
        int type;
        if (media instanceof TLRPC.TL_messageMediaPhoto && media.photo != null) {
            type = TYPE_PHOTO;
        } else if (media instanceof TLRPC.TL_messageMediaStory) {
            type = TYPE_STORY;
        } else if (media instanceof TLRPC.TL_messageMediaDocument && media.document != null
                && (MessageObject.isStickerMessage(message) || "application/x-tgsticker".equals(media.document.mime_type))) {
            values.put("documentType", TYPE_STICKER);
            values.put("mimeType", media.document.mime_type);
            values.put("documentSerialized", serialize(media));
            return;
        } else if (media instanceof TLRPC.TL_messageMediaWebPage && media.webpage != null) {
            values.put("documentType", TYPE_WEBPAGE);
            values.put("documentSerialized", serialize(media));
            return;
        } else {
            type = TYPE_FILE;
        }
        values.put("documentType", type);

        File finalFile = new File("/");
        try {
            if (moveFile) {
                finalFile = keepFile(saved);
                final TLRPC.MessageMedia current = MessageObject.getMedia(message);
                if (current != null && current.document != null && MessageObject.isVideoDocument(current.document) && current.document.thumbs != null) {
                    for (TLRPC.PhotoSize size : current.document.thumbs) {
                        if (size instanceof TLRPC.TL_photoSize) {
                            final File thumb = keepFile(saved.account, size);
                            if (thumb != null && !thumb.getAbsolutePath().equals("/")) {
                                values.put("hqThumbPath", thumb.getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
            } else {
                finalFile = FileLoader.getInstance(saved.account).getPathToMessage(message);
            }
            final TLRPC.Document document = message.media.document;
            if (document != null) {
                values.put("documentAttributesSerialized", serializeMultiple(document.attributes));
                values.put("thumbsSerialized", serializeMultiple(document.thumbs));
                values.put("mimeType", document.mime_type);
            }
        } catch (Exception e) {
            FileLog.e("saving media failed", e);
        }
        final String path = finalFile.getAbsolutePath();
        if (!path.equals("/")) {
            values.put("mediaPath", path);
        }
        if ((path.equals("/") || type == TYPE_STORY) && message.media != null) {
            values.put("documentSerialized", serialize(message.media));
        }
    }

    private static File keepFile(Saved saved) {
        final TLRPC.Message message = saved.message;
        if (message.media instanceof TLRPC.TL_messageMediaStory) {
            final TLRPC.TL_messageMediaStory story = (TLRPC.TL_messageMediaStory) message.media;
            if (story.storyItem != null && story.storyItem.media != null) {
                if (story.storyItem.media.document != null) {
                    return keepFile(saved.account, story.storyItem.media.document);
                } else if (story.storyItem.media.photo != null) {
                    return keepFile(saved.account, story.storyItem.media.photo);
                }
            }
        }
        final FileLoader loader = FileLoader.getInstance(saved.account);
        File path = loader.getPathToMessage(message);
        if (!path.exists() && !path.getAbsolutePath().endsWith("/cache")) {
            path = loader.getPathToMessage(message, false);
        }
        if (path.exists() || message.media.document == null) {
            if (path.exists() || message.media.photo == null) {
                return moveOrCopy(path, new File(attachmentsDir(), fileName(message, path)));
            }
            return keepFile(saved.account, message.media.photo);
        }
        return keepFile(saved.account, message.media.document);
    }

    private static File keepFile(int account, TLObject object) {
        final FileLoader loader = FileLoader.getInstance(account);
        File path = loader.getPathToAttach(object);
        if (!path.exists()) {
            final File cached = loader.getPathToAttach(object, true);
            if (!cached.getAbsolutePath().endsWith("/cache")) {
                path = cached;
            }
        }
        return moveOrCopy(path, new File(attachmentsDir(), fileName(object, path)));
    }

    private static File moveOrCopy(File source, File target) {
        if (!source.exists()) {
            return new File("/");
        }
        boolean done = false;
        try {
            done = source.renameTo(target);
        } catch (Exception ignore) {
        }
        if (!done) {
            try {
                done = AndroidUtilities.copyFile(source, target);
                if (done && !source.delete()) {
                    source.deleteOnExit();
                }
            } catch (Exception e) {
                FileLog.e("copying media failed", e);
            }
        }
        if (!done) {
            FileLog.e("could not keep " + source.getAbsolutePath());
            return new File("/");
        }
        return target;
    }

    private static String fileName(TLObject object, File source) {
        String name = null;
        if (object instanceof TLRPC.Message && ((TLRPC.Message) object).media != null) {
            name = FileLoader.getDocumentFileName(((TLRPC.Message) object).media.document);
        }
        if (object instanceof TLRPC.Document) {
            name = FileLoader.getDocumentFileName((TLRPC.Document) object);
        }
        if (TextUtils.isEmpty(name) && object instanceof TLRPC.Message) {
            name = FileLoader.getMessageFileName((TLRPC.Message) object);
        }
        if (TextUtils.isEmpty(name) && source != null) {
            name = source.getName();
        }
        if (TextUtils.isEmpty(name)) {
            name = "unnamed";
        }
        final int dot = name.lastIndexOf('.');
        final String base = dot > 0 ? name.substring(0, dot) : name;
        final String extension = dot > 0 ? name.substring(dot) : "";
        final StringBuilder result = new StringBuilder(base);
        if (object instanceof TLRPC.Message) {
            final TLRPC.Message message = (TLRPC.Message) object;
            if (message.media instanceof TLRPC.TL_messageMediaPhoto && message.media.photo != null
                    && message.media.photo.sizes != null && !message.media.photo.sizes.isEmpty()) {
                final TLRPC.PhotoSize size = FileLoader.getClosestPhotoSizeWithSize(message.media.photo.sizes, AndroidUtilities.getPhotoSize());
                if (size != null) {
                    result.append('#').append(size.w).append('x').append(size.h);
                }
            }
        }
        final Random random = new Random();
        result.append('@');
        for (int i = 0; i < 6; i++) {
            result.append(NAME_CHARS.charAt(random.nextInt(NAME_CHARS.length())));
        }
        return result.append(extension).toString();
    }

    public static byte[] serialize(TLObject object) {
        if (object == null) {
            return null;
        }
        NativeByteBuffer data = null;
        try {
            final int size = object.getObjectSize();
            if (size <= 0) {
                return null;
            }
            data = new NativeByteBuffer(size);
            object.serializeToStream(data);
            data.rewind();
            final byte[] out = new byte[data.buffer.remaining()];
            data.buffer.get(out);
            return out;
        } catch (Exception e) {
            FileLog.e("serialize failed", e);
            return null;
        } finally {
            if (data != null) {
                data.reuse();
            }
        }
    }

    public static byte[] serializeMultiple(ArrayList<? extends TLObject> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        NativeByteBuffer data = null;
        try {
            int total = 0;
            for (TLObject object : list) {
                if (object != null) {
                    total += object.getObjectSize();
                }
            }
            if (total <= 0) {
                return null;
            }
            data = new NativeByteBuffer(total);
            for (TLObject object : list) {
                if (object != null) {
                    object.serializeToStream(data);
                }
            }
            data.rewind();
            final byte[] out = new byte[data.remaining()];
            data.buffer.get(out);
            return out;
        } catch (Exception e) {
            FileLog.e("serialize failed", e);
            return null;
        } finally {
            if (data != null) {
                data.reuse();
            }
        }
    }
}
