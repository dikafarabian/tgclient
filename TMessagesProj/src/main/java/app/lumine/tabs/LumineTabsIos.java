/*
 * luminegram - iOS style wide bottom bar.
 * The floating pill becomes a full-width bar: tabs stretch over the whole
 * width, the pill grows 4dp taller inside the same envelope (background
 * padding gives 2dp back on each side), inner padding follows entinyGram.
 * The floating search button cannot sit on a full-width bar, so it stays
 * hidden while this mode is on.
 */
package app.lumine.tabs;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.view.View;

import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;

public final class LumineTabsIos {

    public static final int INNER_PADDING = 6;
    public static final int STOCK_INNER_PADDING = 4;
    public static final int SIDE_PADDING_EXTRA = 8;
    public static final int PILL_EXTRA = 4;

    private LumineTabsIos() {
    }

    public static boolean enabled() {
        return LumineTabs.iosBar();
    }

    public static int innerPaddingDp() {
        return org.telegram.ui.DialogsActivity.MAIN_TABS_MARGIN + (enabled() ? INNER_PADDING : STOCK_INNER_PADDING);
    }

    public static void applyTabsLayout(org.telegram.ui.MainTabsLayout tabsView) {
        if (!enabled()) {
            return;
        }
        tabsView.setEqualWidth(true);
        tabsView.setMaxWidth(0);
    }

    public static void applyBarBackground(BlurredBackgroundDrawable background) {
        if (!enabled()) {
            return;
        }
        background.setRadius(dp((org.telegram.ui.DialogsActivity.MAIN_TABS_HEIGHT + PILL_EXTRA) / 2f));
        background.setPadding(dp(org.telegram.ui.DialogsActivity.MAIN_TABS_MARGIN - 0.334f - PILL_EXTRA / 2f));
    }

    public static void applyWrapperPadding(View wrapper, int left, int right, int navigationBarHeight) {
        if (!enabled()) {
            return;
        }
        wrapper.setPadding(left + dp(SIDE_PADDING_EXTRA), 0, right + dp(SIDE_PADDING_EXTRA), navigationBarHeight);
    }
}
