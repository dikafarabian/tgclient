/*
 * luminegram - Material 3 list items.
 * Decisions used by the hooks in the settings cells, the shadow rows and the
 * section drawing of RecyclerListView. Everything returns the stock value
 * while the option is off.
 */
package app.lumine.md3;

import androidx.core.graphics.ColorUtils;

public final class LumineListItems {

    public static final int OUTER_RADIUS_DP = 16;
    public static final int INNER_RADIUS_DP = 4;
    public static final int GAP_DP = 2;
    public static final int ROW_DP = 56;
    public static final int DETAIL_ROW_DP = 72;

    private LumineListItems() {
    }

    public static boolean enabled() {
        return LumineMd3.listEnabled();
    }

    public static boolean forcesLegacy(boolean explicitBitmap) {
        return enabled();
    }

    public static int rowHeight(int stockDp) {
        return stockDp == 50 && enabled() ? ROW_DP : stockDp;
    }

    public static int detailRowHeight(int stockDp) {
        return stockDp == 64 && enabled() ? DETAIL_ROW_DP : stockDp;
    }

    public static int tonalBackground(int top, int bottom) {
        return tone(top, bottom, 0.82f);
    }

    public static int tonalForeground(int top, int bottom) {
        return tone(top, bottom, 0.32f);
    }

    private static int tone(int top, int bottom, float lightness) {
        final float[] hsl = new float[3];
        ColorUtils.colorToHSL(ColorUtils.blendARGB(top, bottom, 0.5f), hsl);
        hsl[1] = Math.max(0.55f, Math.min(1f, hsl[1]));
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int shadowHeight(boolean nextIsHeader, int stockDp) {
        if (!enabled()) {
            return stockDp;
        }
        return nextIsHeader ? 10 : 16;
    }
}
