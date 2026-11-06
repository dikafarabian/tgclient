// ======================================================
// luminegram - action button style
// ======================================================

package app.lumine.chats;

import android.graphics.Color;

import org.telegram.ui.ActionBar.Theme;

import app.lumine.ios.IosStyleConfig;

public final class ActionButtonStyle {

    public static final int ACCENT = 0;
    public static final int NEUTRAL = 1;
    public static final int WHITE = 2;

    private ActionButtonStyle() {
    }

    public static int getCurrentStyle() {
        int style = IosStyleConfig.actionButtonStyle.Int();
        return style == NEUTRAL || style == WHITE ? style : ACCENT;
    }

    public static int resolveBackgroundColor(Theme.ResourcesProvider resourcesProvider) {
        int style = getCurrentStyle();
        if (style == NEUTRAL) {
            return Theme.getColor(Theme.key_chat_messagePanelBackground, resourcesProvider);
        } else if (style == WHITE) {
            return Color.WHITE;
        }
        return Theme.getColor(Theme.key_chat_messagePanelSend, resourcesProvider);
    }

    public static int resolveIconColor(Theme.ResourcesProvider resourcesProvider) {
        int style = getCurrentStyle();
        if (style == NEUTRAL) {
            return Theme.getColor(Theme.key_glass_defaultIcon, resourcesProvider);
        } else if (style == WHITE) {
            return Theme.getColor(Theme.key_chat_messagePanelSend, resourcesProvider);
        }
        return Color.WHITE;
    }
}
