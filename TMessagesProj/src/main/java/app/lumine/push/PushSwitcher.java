/*
 * luminegram - switching between push delivery methods.
 * Leaving FCM or MicroG removes the token from Telegram's servers; In-app turns
 * the keep-alive service and the background connection on, leaving it turns them off.
 */
package app.lumine.push;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.tl.TL_account;

public final class PushSwitcher {

    private PushSwitcher() {
    }

    public static void apply(int newType) {
        final int oldType = PushConfig.type();
        if (oldType == newType) {
            return;
        }
        final boolean oldGoogle = oldType == PushConfig.TYPE_FCM || oldType == PushConfig.TYPE_MICROG;
        final boolean newGoogle = newType == PushConfig.TYPE_FCM || newType == PushConfig.TYPE_MICROG;
        LuminePushLog.event("push service " + PushStatus.typeName(oldType) + " -> " + PushStatus.typeName(newType));
        if (oldGoogle && !newGoogle) {
            unregisterCurrent();
        }
        PushConfig.setType(newType);
        if (oldType == PushConfig.TYPE_IN_APP || newType == PushConfig.TYPE_IN_APP) {
            setInAppService(newType == PushConfig.TYPE_IN_APP);
        }
    }

    private static void unregisterCurrent() {
        final String token = SharedConfig.pushString;
        final int pushType = SharedConfig.pushType;
        Utilities.stageQueue.postRunnable(() -> {
            ConnectionsManager.setRegId(null, pushType, SharedConfig.pushStringStatus);
            SharedConfig.pushString = "";
            SharedConfig.saveConfig();
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                final UserConfig config = UserConfig.getInstance(a);
                config.registeredForPush = false;
                config.saveConfig(false);
                if (!TextUtils.isEmpty(token) && config.isClientActivated()) {
                    final TL_account.unregisterDevice req = new TL_account.unregisterDevice();
                    req.token_type = pushType;
                    req.token = token;
                    final int account = a;
                    ConnectionsManager.getInstance(a).sendRequest(req, (response, error) ->
                            LuminePushLog.event("account " + account + (error == null ? " unregistered" : " unregister failed: " + error.code + ": " + error.text)));
                }
            }
        });
    }

    private static void setInAppService(boolean on) {
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("pushService", on).apply();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final SharedPreferences preferences = MessagesController.getNotificationsSettings(a);
            preferences.edit().putBoolean("pushConnection", on).apply();
            ConnectionsManager.getInstance(a).setPushConnectionEnabled(on);
        }
        AndroidUtilities.runOnUIThread(ApplicationLoader::startPushService);
    }
}
