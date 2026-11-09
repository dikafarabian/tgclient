/*
 * luminegram - message saving options.
 * Every value is stored in LumineConfig; the capture hooks and the viewers read them from here.
 */
package app.lumine.saving;

import app.lumine.LumineConfig;

public final class LumineSaving {

    private static final String DELETED = "LumineSaveDeleted";
    private static final String EDITS = "LumineSaveEdits";
    private static final String ARCHIVED = "LumineSaveArchived";
    private static final String MEDIA = "LumineSaveMedia";
    private static final String MEDIA_PRIVATE_CHATS = "LumineSaveMediaPrivateChats";
    private static final String MEDIA_PUBLIC_CHANNELS = "LumineSaveMediaPublicChannels";
    private static final String MEDIA_PRIVATE_CHANNELS = "LumineSaveMediaPrivateChannels";
    private static final String MEDIA_PUBLIC_GROUPS = "LumineSaveMediaPublicGroups";
    private static final String MEDIA_PRIVATE_GROUPS = "LumineSaveMediaPrivateGroups";
    private static final String IN_PRIVATE = "LumineSaveInPrivate";
    private static final String IN_GROUPS = "LumineSaveInGroups";
    private static final String IN_CHANNELS = "LumineSaveInChannels";
    private static final String BOT_USER = "LumineSaveBotUser";
    private static final String BOT_CHAT = "LumineSaveBotChat";
    private static final String LAST_SEEN = "LumineSaveLastSeen";
    private static final String READ_DATE = "LumineSaveReadDate";

    private LumineSaving() {
    }

    public static boolean deleted() {
        return LumineConfig.getBoolean(DELETED, false);
    }

    public static void setDeleted(boolean value) {
        LumineConfig.setBoolean(DELETED, value);
    }

    public static boolean edits() {
        return LumineConfig.getBoolean(EDITS, false);
    }

    public static void setEdits(boolean value) {
        LumineConfig.setBoolean(EDITS, value);
    }

    public static boolean archived() {
        return LumineConfig.getBoolean(ARCHIVED, true);
    }

    public static void setArchived(boolean value) {
        LumineConfig.setBoolean(ARCHIVED, value);
    }

    public static boolean media() {
        return LumineConfig.getBoolean(MEDIA, true);
    }

    public static void setMedia(boolean value) {
        LumineConfig.setBoolean(MEDIA, value);
    }

    public static boolean mediaPrivateChats() {
        return LumineConfig.getBoolean(MEDIA_PRIVATE_CHATS, true);
    }

    public static void setMediaPrivateChats(boolean value) {
        LumineConfig.setBoolean(MEDIA_PRIVATE_CHATS, value);
    }

    public static boolean mediaPublicChannels() {
        return LumineConfig.getBoolean(MEDIA_PUBLIC_CHANNELS, true);
    }

    public static void setMediaPublicChannels(boolean value) {
        LumineConfig.setBoolean(MEDIA_PUBLIC_CHANNELS, value);
    }

    public static boolean mediaPrivateChannels() {
        return LumineConfig.getBoolean(MEDIA_PRIVATE_CHANNELS, true);
    }

    public static void setMediaPrivateChannels(boolean value) {
        LumineConfig.setBoolean(MEDIA_PRIVATE_CHANNELS, value);
    }

    public static boolean mediaPublicGroups() {
        return LumineConfig.getBoolean(MEDIA_PUBLIC_GROUPS, true);
    }

    public static void setMediaPublicGroups(boolean value) {
        LumineConfig.setBoolean(MEDIA_PUBLIC_GROUPS, value);
    }

    public static boolean mediaPrivateGroups() {
        return LumineConfig.getBoolean(MEDIA_PRIVATE_GROUPS, true);
    }

    public static void setMediaPrivateGroups(boolean value) {
        LumineConfig.setBoolean(MEDIA_PRIVATE_GROUPS, value);
    }

    public static boolean inPrivateChats() {
        return LumineConfig.getBoolean(IN_PRIVATE, true);
    }

    public static void setInPrivateChats(boolean value) {
        LumineConfig.setBoolean(IN_PRIVATE, value);
    }

    public static boolean inGroups() {
        return LumineConfig.getBoolean(IN_GROUPS, true);
    }

    public static void setInGroups(boolean value) {
        LumineConfig.setBoolean(IN_GROUPS, value);
    }

    public static boolean inChannels() {
        return LumineConfig.getBoolean(IN_CHANNELS, true);
    }

    public static void setInChannels(boolean value) {
        LumineConfig.setBoolean(IN_CHANNELS, value);
    }

    public static boolean botUser() {
        return LumineConfig.getBoolean(BOT_USER, false);
    }

    public static void setBotUser(boolean value) {
        LumineConfig.setBoolean(BOT_USER, value);
    }

    public static boolean botChat() {
        return LumineConfig.getBoolean(BOT_CHAT, false);
    }

    public static void setBotChat(boolean value) {
        LumineConfig.setBoolean(BOT_CHAT, value);
    }

    public static boolean lastSeen() {
        return LumineConfig.getBoolean(LAST_SEEN, false);
    }

    public static void setLastSeen(boolean value) {
        LumineConfig.setBoolean(LAST_SEEN, value);
    }

    public static boolean readDate() {
        return LumineConfig.getBoolean(READ_DATE, false);
    }

    public static void setReadDate(boolean value) {
        LumineConfig.setBoolean(READ_DATE, value);
    }

    public static int enabledCount() {
        int count = 0;
        if (deleted()) {
            count++;
        }
        if (edits()) {
            count++;
        }
        if (lastSeen()) {
            count++;
        }
        if (readDate()) {
            count++;
        }
        return count;
    }
}
