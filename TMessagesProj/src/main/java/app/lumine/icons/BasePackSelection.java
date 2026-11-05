package app.lumine.icons;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import xyz.nextalone.nagram.NaConfig;

public final class BasePackSelection {
    public static final int BASE_DEFAULT = 0;
    public static final int BASE_SOLAR = 1;
    public static final int BASE_REMIX = 2;

    private BasePackSelection() {
    }

    public static int getSelected() {
        int value = NaConfig.INSTANCE.getIconReplacements().Int();
        return value == BASE_SOLAR || value == BASE_REMIX ? value : BASE_DEFAULT;
    }

    public static void setSelected(int type) {
        if (type != BASE_SOLAR && type != BASE_REMIX) {
            type = BASE_DEFAULT;
        }
        NaConfig.INSTANCE.getIconReplacements().setConfigInt(type);
    }

    public static String getName(int type) {
        if (type == BASE_SOLAR) {
            return "Solar Icon Set";
        }
        if (type == BASE_REMIX) {
            return "Remix Icon";
        }
        return LocaleController.getString(R.string.Default);
    }

    public static String getAuthor(int type) {
        if (type == BASE_SOLAR) {
            return "@Design480";
        }
        if (type == BASE_REMIX) {
            return "Remix-Design";
        }
        return "Telegram";
    }
}
