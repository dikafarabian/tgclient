/*
 * luminegram - avatar placement of the chat header pill.
 * Stock already centers the glass pill; these options move the avatar:
 * into the menu slot (tap profile, hold menu, or swapped), static beside
 * the back button, or to the right end of the pill, plus hide the avatar
 * and hide the pill background. Every number lives here.
 */
package app.lumine.chat;

import android.view.View;

import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.ChatAvatarContainer;

public final class LumineChatPill {

    public static final int MODE_STOCK = 0;
    public static final int MODE_SLOT = 1;
    public static final int MODE_STATIC = 2;
    public static final int MODE_RIGHT = 3;

    /* The slot mirrors the back button one: 6dp padding + 46dp button, center 29dp from the edge. */
    public static final int SLOT_CENTER_DP = 29;
    public static final int SLOT_RESERVE_DP = 29 + 23 + 6;
    /* The static avatar sits right of the back button pill (58dp) plus a gap. */
    public static final int STATIC_LEFT_DP = 64;
    public static final int STATIC_RESERVE_DP = 58;

    private LumineChatPill() {
    }

    public static boolean pillVisible() {
        return LumineChatConfig.getChatPill();
    }

    public static boolean hideAvatar() {
        return LumineChatConfig.getChatPillHideAvatar();
    }

    public static boolean swapSlotGestures() {
        return LumineChatConfig.getChatPillAvatarSwap();
    }

    public static int avatarMode(ChatAvatarContainer container) {
        if (container == null || !container.hasVisibleAvatar()) {
            return MODE_STOCK;
        }
        if (LumineChatConfig.getChatPillAvatarSlot()) {
            return MODE_SLOT;
        }
        if (LumineChatConfig.getChatPillAvatarStatic()) {
            return MODE_STATIC;
        }
        if (LumineChatConfig.getChatPillAvatarRight()) {
            return MODE_RIGHT;
        }
        return MODE_STOCK;
    }

    /* Slot and static take the avatar out of the pill, so the pill width drops its room. */
    public static boolean avatarInsidePill(ChatAvatarContainer container) {
        final int mode = avatarMode(container);
        return mode == MODE_STOCK || mode == MODE_RIGHT;
    }

    /*
     * The menu item keeps its touch area and becomes the proxy for the avatar
     * drawn on top of it. Tap opens the profile and holding opens the menu
     * (the item's 200ms hold runnable), or the two swap.
     */
    public static void applyMenuSlot(final ChatAvatarContainer container, ActionBarMenuItem item) {
        if (item == null || avatarMode(container) != MODE_SLOT) {
            return;
        }
        final View icon = item.getIconView();
        if (icon != null) {
            icon.setVisibility(View.INVISIBLE);
        }
        if (swapSlotGestures()) {
            item.setOnLongClickListener(v -> {
                container.openProfile(true);
                return true;
            });
        } else {
            item.setLongClickEnabled(true);
            item.setOnClickListener(v -> container.openProfile(true));
        }
    }
}
