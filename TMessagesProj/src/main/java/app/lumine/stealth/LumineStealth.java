/*
 * luminegram - Stealth options read by the hooks in the core files.
 * Every value is stored straight in LumineConfig.
 */
package app.lumine.stealth;

import app.lumine.LumineConfig;

public final class LumineStealth {

    public static final String KEY_RELATIVE_LAST_SEEN = "LumineRelativeLastSeen";

    private LumineStealth() {
    }

    public static boolean relativeLastSeen() {
        return LumineConfig.getBoolean(KEY_RELATIVE_LAST_SEEN, false);
    }

    public static void setRelativeLastSeen(boolean enabled) {
        LumineConfig.setBoolean(KEY_RELATIVE_LAST_SEEN, enabled);
    }
}
