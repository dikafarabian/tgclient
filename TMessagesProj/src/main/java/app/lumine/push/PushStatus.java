/*
 * luminegram - readable push state: type names, one-line status, OS checks.
 * Used by the Home card and the General settings page.
 */
package app.lumine.push;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

    public static final class Health {
        public static final int NEUTRAL = 0;
        public static final int GOOD = 1;
        public static final int BAD = 2;

        public final String text;
        public final int level;

        Health(int res, int level) {
            this.text = LumineStrings.get(res);
            this.level = level;
        }
    }

    public static Health health(int account) {
        if (!osNotificationsEnabled()) {
            return new Health(R.string.LumineHealthBlocked, Health.BAD);
        }
        if (!PushConfig.usesGoogle()) {
            final boolean running = MessagesController.getGlobalNotificationsSettings().getBoolean("pushService", false);
            return running ? new Health(R.string.LumineHealthRunning, Health.GOOD) : new Health(R.string.LumineHealthStopped, Health.BAD);
        }
        if (!playServicesAvailable()) {
            return new Health(R.string.LumineHealthNoServices, Health.BAD);
        }
        if (TextUtils.isEmpty(SharedConfig.pushString)) {
            final String status = SharedConfig.pushStringStatus == null ? "" : SharedConfig.pushStringStatus;
            if (!TextUtils.isEmpty(LuminePushLog.tokenError()) || status.startsWith("__FIREBASE_FAILED__") || status.startsWith("__NO_GOOGLE_PLAY_SERVICES__")) {
                return new Health(R.string.LumineHealthTokenFailed, Health.BAD);
            }
            return new Health(R.string.LumineHealthWaitingToken, Health.NEUTRAL);
        }
        if (!UserConfig.getInstance(account).registeredForPush) {
            if (!TextUtils.isEmpty(LuminePushLog.registerError())) {
                return new Health(R.string.LumineHealthRegisterFailed, Health.BAD);
            }
            return new Health(R.string.LumineHealthRegistering, Health.NEUTRAL);
        }
        return new Health(R.string.LumineHealthReady, Health.GOOD);
    }

    private static boolean playServicesAvailable() {
        try {
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ApplicationLoader.applicationContext) == ConnectionResult.SUCCESS;
        } catch (Throwable e) {
            return false;
        }
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
