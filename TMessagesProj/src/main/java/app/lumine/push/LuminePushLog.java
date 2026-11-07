/*
 * luminegram - push notification diagnostics.
 * Keeps the FCM token and registration outcome plus a short event log,
 * independent of the app log switch, so a push problem is visible on the Home page.
 */
package app.lumine.push;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.lumine.LumineConfig;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.PushListenerController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

public final class LuminePushLog {

    private static final String TAG = "lumine-push";
    private static final String KEY_LOG = "push_log";
    private static final String KEY_TOKEN_ERROR = "push_token_error";
    private static final String KEY_REGISTER_ERROR = "push_register_error";
    private static final String KEY_LAST_PUSH = "push_last_received";
    private static final int MAX_LINES = 40;

    private LuminePushLog() {
    }

    public static synchronized void event(String text) {
        Log.d(TAG, text);
        final String line = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date()) + " " + text;
        final String old = LumineConfig.getString(KEY_LOG, "");
        final String[] lines = TextUtils.isEmpty(old) ? new String[0] : old.split("\n");
        final StringBuilder out = new StringBuilder();
        final int from = Math.max(0, lines.length - (MAX_LINES - 1));
        for (int a = from; a < lines.length; a++) {
            out.append(lines[a]).append('\n');
        }
        out.append(line);
        LumineConfig.setString(KEY_LOG, out.toString());
    }

    public static void tokenRequested() {
        event("token request started");
    }

    public static void tokenReceived(String token) {
        LumineConfig.setString(KEY_TOKEN_ERROR, "");
        event("token received (" + (token == null ? 0 : token.length()) + " chars)");
    }

    public static void tokenFailed(Throwable error) {
        final String text = error == null ? "unknown" : error.getClass().getSimpleName() + ": " + error.getMessage();
        LumineConfig.setString(KEY_TOKEN_ERROR, text);
        event("token failed: " + text);
    }

    public static void registerResult(int account, TLRPC.TL_error error) {
        if (error == null) {
            LumineConfig.setString(KEY_REGISTER_ERROR, "");
            event("account " + account + " registered");
        } else {
            final String text = error.code + ": " + error.text;
            LumineConfig.setString(KEY_REGISTER_ERROR, text);
            event("account " + account + " register failed: " + text);
        }
    }

    public static void pushReceived() {
        LumineConfig.setString(KEY_LAST_PUSH, String.valueOf(System.currentTimeMillis()));
        event("push received");
    }

    public static long lastPushTime() {
        try {
            return Long.parseLong(LumineConfig.getString(KEY_LAST_PUSH, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String tokenError() {
        return LumineConfig.getString(KEY_TOKEN_ERROR, "");
    }

    public static String registerError() {
        return LumineConfig.getString(KEY_REGISTER_ERROR, "");
    }

    public static String lastError() {
        final String register = registerError();
        return TextUtils.isEmpty(register) ? tokenError() : register;
    }

    public static String report() {
        final Context context = ApplicationLoader.applicationContext;
        final StringBuilder out = new StringBuilder();
        final boolean hasToken = !TextUtils.isEmpty(SharedConfig.pushString);
        out.append("token: ").append(hasToken ? SharedConfig.pushString.length() + " chars" : "none").append('\n');
        out.append("token status: ").append(SharedConfig.pushStringStatus).append('\n');
        out.append("token error: ").append(emptyAs(tokenError(), "none")).append('\n');
        out.append("token fetch ms: ").append(SharedConfig.pushStringGetTimeEnd - SharedConfig.pushStringGetTimeStart).append('\n');
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final UserConfig config = UserConfig.getInstance(a);
            if (config.getClientUserId() != 0) {
                out.append("account ").append(a).append(" registered: ").append(config.registeredForPush).append('\n');
            }
        }
        out.append("register error: ").append(emptyAs(registerError(), "none")).append('\n');
        final long last = lastPushTime();
        out.append("last push: ").append(last <= 0 ? "never" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(last))).append('\n');
        out.append("play services: ").append(PushListenerController.GooglePushListenerServiceProvider.INSTANCE.hasServices()).append('\n');
        out.append("gms version: ").append(gmsVersion(context)).append('\n');
        out.append("firebase config: ").append(firebaseConfig(context)).append('\n');
        out.append("push connection: ").append(ConnectionsManager.getInstance(UserConfig.selectedAccount).isPushConnectionEnabled()).append('\n');
        out.append("os notifications: ").append(NotificationManagerCompat.from(context).areNotificationsEnabled()).append('\n');
        out.append("battery unrestricted: ").append(batteryUnrestricted(context)).append("\n\n");
        out.append(emptyAs(LumineConfig.getString(KEY_LOG, ""), "no events"));
        return out.toString();
    }

    private static String firebaseConfig(Context context) {
        try {
            final int id = context.getResources().getIdentifier("google_app_id", "string", context.getPackageName());
            final boolean options = com.google.firebase.FirebaseOptions.fromResource(context) != null;
            final int apps = com.google.firebase.FirebaseApp.getApps(context).size();
            return "google_app_id res " + (id != 0) + ", options " + options + ", apps " + apps;
        } catch (Throwable e) {
            return e.getClass().getSimpleName();
        }
    }

    private static String emptyAs(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private static String gmsVersion(Context context) {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo("com.google.android.gms", 0);
            return info.versionName;
        } catch (Throwable e) {
            return "n/a";
        }
    }

    private static String batteryUnrestricted(Context context) {
        try {
            final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return String.valueOf(pm.isIgnoringBatteryOptimizations(context.getPackageName()));
        } catch (Throwable e) {
            return "n/a";
        }
    }
}
