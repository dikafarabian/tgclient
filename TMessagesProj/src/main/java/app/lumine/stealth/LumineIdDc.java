/*
 * luminegram - ID and data center text for the profile page.
 * Pure helpers, no UI. The profile hooks call these.
 */
package app.lumine.stealth;

import java.util.Locale;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

public final class LumineIdDc {

    private LumineIdDc() {
    }

    public static String dcName(int dc) {
        switch (dc) {
            case 1:
                return "Pluto";
            case 2:
                return "Venus";
            case 3:
                return "Aurora";
            case 4:
                return "Vesta";
            case 5:
                return "Flora";
            default:
                return "Unknown";
        }
    }

    public static String dcLocation(int dc) {
        switch (dc) {
            case 1:
            case 3:
                return "Miami";
            case 2:
            case 4:
                return "Amsterdam";
            case 5:
                return "Singapore";
            default:
                return "Unknown";
        }
    }

    public static String dcLine(int dc) {
        if (dc == 0) {
            return "DC " + LocaleController.getString(R.string.NumberUnknown);
        }
        return String.format(Locale.US, "DC%d %s, %s", dc, dcName(dc), dcLocation(dc));
    }

    public static long displayId(long id, TLRPC.Chat chat) {
        if (chat != null && LumineStealth.idDcType() == LumineStealth.ID_BOT_API) {
            return ChatObject.isChannel(chat) ? -1000000000000L - chat.id : -chat.id;
        }
        return id;
    }
}
