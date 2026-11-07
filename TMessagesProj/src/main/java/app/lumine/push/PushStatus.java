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
            if (tokenFailed()) {
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
        return new Health(R.string.LumineHealthRunning, Health.GOOD);
    }

    public static Health services() {
        return playServicesAvailable() ? new Health(R.string.LumineAvailable, Health.GOOD) : new Health(R.string.LumineMissing, Health.BAD);
    }

    public static Health token() {
        if (!TextUtils.isEmpty(SharedConfig.pushString)) {
            return new Health(R.string.LumineTokenReady, Health.GOOD);
        }
        if (tokenFailed()) {
            return new Health(R.string.LumineTokenFailed, Health.BAD);
        }
        return new Health(R.string.LumineTokenWaiting, Health.NEUTRAL);
    }

    public static Health registration(int account) {
        return UserConfig.getInstance(account).registeredForPush ? new Health(R.string.LumineRegistered, Health.GOOD) : new Health(R.string.LumineNotRegistered, Health.BAD);
    }

    private static boolean tokenFailed() {
        final String status = SharedConfig.pushStringStatus == null ? "" : SharedConfig.pushStringStatus;
        return !TextUtils.isEmpty(LuminePushLog.tokenError()) || status.startsWith("__FIREBASE_FAILED__") || status.startsWith("__NO_GOOGLE_PLAY_SERVICES__");
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
