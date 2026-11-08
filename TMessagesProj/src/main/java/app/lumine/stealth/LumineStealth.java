/*
 * luminegram - Stealth options read by the hooks in the core files.
 * Every value is stored straight in LumineConfig.
 */
package app.lumine.stealth;

import app.lumine.LumineConfig;

public final class LumineStealth {

    public static final String KEY_RELATIVE_LAST_SEEN = "LumineRelativeLastSeen";
    public static final String KEY_HIDE_PHONE = "LumineHidePhone";
    public static final String KEY_ID_DC_TYPE = "LumineIdDcType";

    public static final int ID_HIDDEN = 0;
    public static final int ID_API = 1;
    public static final int ID_BOT_API = 2;

    private LumineStealth() {
    }

    public static boolean relativeLastSeen() {
        return LumineConfig.getBoolean(KEY_RELATIVE_LAST_SEEN, false);
    }

    public static void setRelativeLastSeen(boolean enabled) {
        LumineConfig.setBoolean(KEY_RELATIVE_LAST_SEEN, enabled);
    }

    public static boolean hidePhone() {
        return LumineConfig.getBoolean(KEY_HIDE_PHONE, false);
    }

    public static void setHidePhone(boolean enabled) {
        LumineConfig.setBoolean(KEY_HIDE_PHONE, enabled);
    }

    public static int idDcType() {
        final int type = LumineConfig.getInt(KEY_ID_DC_TYPE, ID_API);
        return type < ID_HIDDEN || type > ID_BOT_API ? ID_API : type;
    }

    public static void setIdDcType(int type) {
        LumineConfig.setInt(KEY_ID_DC_TYPE, type);
    }
}
