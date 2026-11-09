/*
 * luminegram - local store for saved messages.
 * Table and column names follow the exteraless schema version 27, so a database exported from it can be read as is.
 */
package app.lumine.saving;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.messenger.ApplicationLoader;

public final class LumineSaveDb extends SQLiteOpenHelper {

    public static final String NAME = "lumine_saved.db";
    public static final int VERSION = 27;

    public static final String EDITED = "EditedMessage";
    public static final String DELETED = "DeletedMessage";
    public static final String DELETED_REACTION = "DeletedMessageReaction";
    public static final String LAST_SEEN = "LastSeenEntity";
    public static final String SPY_READ = "SpyMessageRead";
    public static final String SPY_CONTENTS_READ = "SpyMessageContentsRead";
    public static final String DELETED_DIALOG = "DeletedDialog";

    private static final String[] CREATE = {
            "CREATE TABLE IF NOT EXISTS `EditedMessage` (`fakeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `dialogId` INTEGER NOT NULL, `groupedId` INTEGER NOT NULL, `peerId` INTEGER NOT NULL, `fromId` INTEGER NOT NULL, `topicId` INTEGER NOT NULL, `messageId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `flags` INTEGER NOT NULL, `editDate` INTEGER NOT NULL, `views` INTEGER NOT NULL, `forwards` INTEGER NOT NULL, `fwdFlags` INTEGER NOT NULL, `fwdFromId` INTEGER NOT NULL, `fwdName` TEXT, `fwdDate` INTEGER NOT NULL, `fwdPostAuthor` TEXT, `replyFlags` INTEGER NOT NULL, `replyMessageId` INTEGER NOT NULL, `replyPeerId` INTEGER NOT NULL, `replyTopId` INTEGER NOT NULL, `replyForumTopic` INTEGER NOT NULL, `replyQuote` INTEGER NOT NULL, `replyQuoteText` TEXT, `replyQuoteEntities` BLOB, `replyFromSerialized` BLOB, `entityCreateDate` INTEGER NOT NULL, `text` TEXT, `textEntities` BLOB, `mediaPath` TEXT, `hqThumbPath` TEXT, `documentType` INTEGER NOT NULL, `documentSerialized` BLOB, `thumbsSerialized` BLOB, `documentAttributesSerialized` BLOB, `mimeType` TEXT, `replyMarkupSerialized` BLOB)",
            "CREATE INDEX IF NOT EXISTS `index_EditedMessage_userId_dialogId_messageId_entityCreateDate` ON `EditedMessage` (`userId`, `dialogId`, `messageId`, `entityCreateDate`)",
            "CREATE INDEX IF NOT EXISTS `index_EditedMessage_userId_entityCreateDate` ON `EditedMessage` (`userId`, `entityCreateDate`)",
            "CREATE INDEX IF NOT EXISTS `index_EditedMessage_dialogId_messageId` ON `EditedMessage` (`dialogId`, `messageId`)",
            "CREATE TABLE IF NOT EXISTS `DeletedMessage` (`fakeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `dialogId` INTEGER NOT NULL, `groupedId` INTEGER NOT NULL, `peerId` INTEGER NOT NULL, `fromId` INTEGER NOT NULL, `topicId` INTEGER NOT NULL, `messageId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `flags` INTEGER NOT NULL, `editDate` INTEGER NOT NULL, `views` INTEGER NOT NULL, `forwards` INTEGER NOT NULL, `fwdFlags` INTEGER NOT NULL, `fwdFromId` INTEGER NOT NULL, `fwdName` TEXT, `fwdDate` INTEGER NOT NULL, `fwdPostAuthor` TEXT, `replyFlags` INTEGER NOT NULL, `replyMessageId` INTEGER NOT NULL, `replyPeerId` INTEGER NOT NULL, `replyTopId` INTEGER NOT NULL, `replyForumTopic` INTEGER NOT NULL, `replyQuote` INTEGER NOT NULL, `replyQuoteText` TEXT, `replyQuoteEntities` BLOB, `replyFromSerialized` BLOB, `entityCreateDate` INTEGER NOT NULL, `text` TEXT, `textEntities` BLOB, `mediaPath` TEXT, `hqThumbPath` TEXT, `documentType` INTEGER NOT NULL, `documentSerialized` BLOB, `thumbsSerialized` BLOB, `documentAttributesSerialized` BLOB, `mimeType` TEXT, `replyMarkupSerialized` BLOB)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessage_userId_dialogId_messageId` ON `DeletedMessage` (`userId`, `dialogId`, `messageId`)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessage_userId_dialogId_topicId_messageId` ON `DeletedMessage` (`userId`, `dialogId`, `topicId`, `messageId`)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessage_userId_dialogId_replyMessageId_messageId` ON `DeletedMessage` (`userId`, `dialogId`, `replyMessageId`, `messageId`)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessage_userId_dialogId_groupedId_messageId` ON `DeletedMessage` (`userId`, `dialogId`, `groupedId`, `messageId`)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessage_dialogId` ON `DeletedMessage` (`dialogId`)",
            "CREATE TABLE IF NOT EXISTS `DeletedMessageReaction` (`fakeReactionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deletedMessageId` INTEGER NOT NULL, `emoticon` TEXT, `documentId` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL, `count` INTEGER NOT NULL, `selfSelected` INTEGER NOT NULL)",
            "CREATE INDEX IF NOT EXISTS `index_DeletedMessageReaction_deletedMessageId` ON `DeletedMessageReaction` (`deletedMessageId`)",
            "CREATE TABLE IF NOT EXISTS `LastSeenEntity` (`userId` INTEGER NOT NULL, `lastSeen` INTEGER NOT NULL, PRIMARY KEY(`userId`))",
            "CREATE TABLE IF NOT EXISTS `SpyMessageRead` (`userId` INTEGER NOT NULL, `dialogId` INTEGER NOT NULL, `messageId` INTEGER NOT NULL, `date` INTEGER NOT NULL, PRIMARY KEY(`userId`, `dialogId`, `messageId`))",
            "CREATE TABLE IF NOT EXISTS `SpyMessageContentsRead` (`userId` INTEGER NOT NULL, `dialogId` INTEGER NOT NULL, `messageId` INTEGER NOT NULL, `date` INTEGER NOT NULL, PRIMARY KEY(`userId`, `dialogId`, `messageId`))",
            "CREATE TABLE IF NOT EXISTS `DeletedDialog` (`userId` INTEGER NOT NULL, `dialogId` INTEGER NOT NULL, `folderId` INTEGER NOT NULL, `topMessage` INTEGER NOT NULL, `lastMessageDate` INTEGER NOT NULL, `entityCreateDate` INTEGER NOT NULL, PRIMARY KEY(`userId`, `dialogId`))"
    };

    private static volatile LumineSaveDb instance;

    private LumineSaveDb(Context context) {
        super(context, NAME, null, VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    public static LumineSaveDb get() {
        LumineSaveDb local = instance;
        if (local == null) {
            synchronized (LumineSaveDb.class) {
                local = instance;
                if (local == null) {
                    local = new LumineSaveDb(ApplicationLoader.applicationContext);
                    instance = local;
                }
            }
        }
        return local;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String statement : CREATE) {
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
