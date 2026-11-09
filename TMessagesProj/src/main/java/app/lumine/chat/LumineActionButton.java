/*
 * luminegram - action button style.
 * Colors of the send button in the chat input: accent (stock), neutral or
 * white. The style lives in LumineChatConfig.
 */
package app.lumine.chat;

import android.graphics.Color;

import org.telegram.ui.ActionBar.Theme;

public final class LumineActionButton {

    public static final int ACCENT = 0;
    public static final int NEUTRAL = 1;
    public static final int WHITE = 2;

    private LumineActionButton() {
    }

    public static int background(Theme.ResourcesProvider resourcesProvider) {
        final int style = LumineChatConfig.getActionButtonStyle();
        if (style == NEUTRAL) {
            return Theme.getColor(Theme.key_chat_messagePanelBackground, resourcesProvider);
        } else if (style == WHITE) {
            return Color.WHITE;
        }
        return Theme.getColor(Theme.key_chat_messagePanelSend, resourcesProvider);
    }

    public static int icon(Theme.ResourcesProvider resourcesProvider) {
        final int style = LumineChatConfig.getActionButtonStyle();
        if (style == NEUTRAL) {
            return Theme.getColor(Theme.key_glass_defaultIcon, resourcesProvider);
        } else if (style == WHITE) {
            return Theme.getColor(Theme.key_chat_messagePanelSend, resourcesProvider);
        }
        return Color.WHITE;
    }
}
