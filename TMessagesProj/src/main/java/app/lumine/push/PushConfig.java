/*
 * luminegram - push service selection.
 * Holds which delivery method is active: in-app service, FCM, UnifiedPush or MicroG.
 * FCM is the default, so a fresh install behaves like stock Telegram.
 */
package app.lumine.push;

import app.lumine.LumineConfig;
import org.telegram.messenger.PushListenerController;

public final class PushConfig {

    public static final int TYPE_IN_APP = 0;
    public static final int TYPE_FCM = 1;
    public static final int TYPE_UNIFIED = 2;
    public static final int TYPE_MICROG = 3;

    private static final String KEY_TYPE = "push_service_type";

    private PushConfig() {
    }

    public static int type() {
        final int type = LumineConfig.getInt(KEY_TYPE, TYPE_FCM);
        return type < TYPE_IN_APP || type > TYPE_MICROG ? TYPE_FCM : type;
    }

    public static void setType(int type) {
        LumineConfig.setInt(KEY_TYPE, type);
    }

    public static boolean usesGoogle() {
        final int type = type();
        return type == TYPE_FCM || type == TYPE_MICROG;
    }

    public static PushListenerController.IPushListenerServiceProvider createProvider() {
        if (usesGoogle()) {
            return PushListenerController.GooglePushListenerServiceProvider.INSTANCE;
        }
        return new InAppProvider();
    }

    private static final class InAppProvider implements PushListenerController.IPushListenerServiceProvider {

        @Override
        public boolean hasServices() {
            return false;
        }

        @Override
        public String getLogTitle() {
            return "In-app";
        }

        @Override
        public void onRequestPushToken() {
        }

        @Override
        public int getPushType() {
            return PushListenerController.PUSH_TYPE_FIREBASE;
        }
    }
}
