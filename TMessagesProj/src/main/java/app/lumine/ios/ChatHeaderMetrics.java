// ======================================================
// luminegram - chat header metrics
// ======================================================

package app.lumine.ios;

import org.telegram.messenger.AndroidUtilities;

public final class ChatHeaderMetrics {

    private static final int AVATAR_SIZE_DP = 42;
    private static final int AVATAR_INSET_PX = 1;

    private ChatHeaderMetrics() {
    }

    public static boolean isMaterial3ChatHeaderStyle() {
        return false;
    }

    public static int getAvatarInsetPx() {
        return AVATAR_INSET_PX;
    }

    public static int getAvatarSizePx(int sizeDp) {
        return AndroidUtilities.dp(sizeDp) - getAvatarInsetPx() * 2;
    }

    public static int getChatAvatarSizeDp() {
        return AVATAR_SIZE_DP;
    }

    public static int getChatAvatarRadius(int sizeDp, boolean forum, boolean hasStories) {
        double value = AndroidUtilities.dp(sizeDp) / 2.0;
        if (hasStories) {
            value -= AndroidUtilities.dpf2(2.5f);
        }
        if (forum) {
            value = (int) value * 42.0 / 64.0;
        }
        if (value <= 0) {
            return 0;
        }
        return (int) Math.ceil(value);
    }
}
