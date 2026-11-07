/*
 * luminegram - readable push state: type names, one-line status, OS checks.
 * Used by the Home card and the General settings page.
 */
package app.lumine.push;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;

import app.lumine.LumineStrings;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;

public final class PushStatus {

    private PushStatus() {
    }

    public static String typeName(int type) {
        switch (type) {
            case PushConfig.TYPE_IN_APP:
                return LumineStrings.get(R.string.LuminePushTypeInApp);
            case PushConfig.TYPE_MICROG:
                return LumineStrings.get(R.string.LuminePushTypeMicroG);
            case PushConfig.TYPE_UNIFIED:
                return LumineStrings.get(R.string.LuminePushTypeUnified);
            default:
                return LumineStrings.get(R.string.LuminePushTypeFcm);
        }
    }

    public static String summary() {
        final int type = PushConfig.type();
        final String name = typeName(type);
        if (!osNotificationsEnabled()) {
            return name + " \u00b7 " + LumineStrings.get(R.string.LuminePushStatusBlocked);
        }
        if (type == PushConfig.TYPE_IN_APP) {
            final boolean running = MessagesController.getGlobalNotificationsSettings().getBoolean("pushService", false);
            return name + " \u00b7 " + LumineStrings.get(running ? R.string.LuminePushStatusServiceOn : R.string.LuminePushStatusServiceOff);
        }
        if (TextUtils.isEmpty(SharedConfig.pushString)) {
            return name + " \u00b7 " + LumineStrings.get(R.string.LuminePushStatusNoToken);
        }
        if (!UserConfig.getInstance(UserConfig.selectedAccount).registeredForPush) {
            return name + " \u00b7 " + LumineStrings.get(R.string.LuminePushStatusNotRegistered);
        }
        final long last = LuminePushLog.lastPushTime();
        if (last <= 0) {
            return name + " \u00b7 " + LumineStrings.get(R.string.LuminePushStatusNothing);
        }
        return name + " \u00b7 " + LocaleController.formatDateTime(last / 1000L, true);
    }

    public static boolean osNotificationsEnabled() {
        try {
            return NotificationManagerCompat.from(ApplicationLoader.applicationContext).areNotificationsEnabled();
        } catch (Throwable e) {
            return true;
        }
    }

    public static boolean batteryUnrestricted() {
        try {
            final Context context = ApplicationLoader.applicationContext;
            final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
        } catch (Throwable e) {
            return false;
        }
    }
}
