/*
 * luminegram - Ghost Mode options and state.
 * Every value is stored straight in LumineConfig; the request side lives in LumineGhostRequests.
 */
package app.lumine.ghost;

import app.lumine.LumineConfig;

public final class LumineGhost {

    public static final int READ_MESSAGES = 0;
    public static final int READ_STORIES = 1;
    public static final int ONLINE = 2;
    public static final int TYPING = 3;
    public static final int OFFLINE_AFTER_SEND = 4;
    public static final int COUNT = 5;
    public static final int MAX_LOCKED = 4;

    private static final String[] KEYS = {
            "LumineGhostNoRead",
            "LumineGhostNoStories",
            "LumineGhostNoOnline",
            "LumineGhostNoTyping",
            "LumineGhostOfflineAfterSend"
    };
    private static final String LOCK_SUFFIX = "Locked";
    private static final String KEY_MARK_READ_AFTER_SEND = "LumineGhostMarkReadAfterSend";
    private static final String KEY_ASK_BEFORE_STORY = "LumineGhostAskStory";

    public static volatile boolean storyGhostSession;

    private static final Object allowLock = new Object();
    private static int allowReadPasses;

    private LumineGhost() {
    }

    public static boolean get(int option) {
        return LumineConfig.getBoolean(KEYS[option], false);
    }

    public static void set(int option, boolean value) {
        LumineConfig.setBoolean(KEYS[option], value);
    }

    public static boolean locked(int option) {
        return LumineConfig.getBoolean(KEYS[option] + LOCK_SUFFIX, false);
    }

    public static void setLocked(int option, boolean value) {
        LumineConfig.setBoolean(KEYS[option] + LOCK_SUFFIX, value);
    }

    public static int lockedCount() {
        int count = 0;
        for (int i = 0; i < COUNT; i++) {
            if (locked(i)) {
                count++;
            }
        }
        return count;
    }

    public static int selectedCount() {
        int count = 0;
        for (int i = 0; i < COUNT; i++) {
            if (get(i)) {
                count++;
            }
        }
        return count;
    }

    public static boolean active() {
        for (int i = 0; i < COUNT; i++) {
            if (!locked(i) && !get(i)) {
                return false;
            }
        }
        return true;
    }

    public static void setActive(boolean enabled) {
        for (int i = 0; i < COUNT; i++) {
            if (!locked(i)) {
                set(i, enabled);
            }
        }
    }

    public static boolean markReadAfterSend() {
        return LumineConfig.getBoolean(KEY_MARK_READ_AFTER_SEND, true);
    }

    public static void setMarkReadAfterSend(boolean enabled) {
        LumineConfig.setBoolean(KEY_MARK_READ_AFTER_SEND, enabled);
    }

    public static boolean askBeforeStory() {
        return LumineConfig.getBoolean(KEY_ASK_BEFORE_STORY, false);
    }

    public static void setAskBeforeStory(boolean enabled) {
        LumineConfig.setBoolean(KEY_ASK_BEFORE_STORY, enabled);
    }

    static void allowNextRead() {
        synchronized (allowLock) {
            allowReadPasses++;
        }
    }

    static boolean consumeAllowedRead() {
        synchronized (allowLock) {
            if (allowReadPasses > 0) {
                allowReadPasses--;
                return true;
            }
            return false;
        }
    }

    public static void clearAllowedRead() {
        synchronized (allowLock) {
            allowReadPasses = 0;
        }
    }
}
