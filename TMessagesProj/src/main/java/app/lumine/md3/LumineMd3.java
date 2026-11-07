/*
 * luminegram - Material 3 foundation for the luminegram screens.
 * Role colors for light and dark, derived from the brand seed with an HSL
 * tone mapping, plus shape and spacing constants. Own code, no upstream hooks.
 */
package app.lumine.md3;

import androidx.core.graphics.ColorUtils;

import org.telegram.ui.ActionBar.Theme;

public final class LumineMd3 {

    public static final int SEED = 0xFFFA456C;

    public static final int HERO_RADIUS_DP = 28;
    public static final int TILE_RADIUS_DP = 22;
    public static final int TILE_MIN_HEIGHT_DP = 106;
    public static final int TILE_PADDING_DP = 14;
    public static final int GAP_DP = 12;
    public static final int HERO_ICON_DP = 48;
    public static final int TILE_ICON_DP = 38;

    public static final class Roles {
        public final int primary;
        public final int onPrimary;
        public final int primaryContainer;
        public final int onPrimaryContainer;
        public final int secondaryContainer;
        public final int onSecondaryContainer;
        public final int tertiaryContainer;
        public final int onTertiaryContainer;
        public final int error;
        public final int errorContainer;
        public final int onErrorContainer;
        public final int surface;
        public final int surfaceContainer;
        public final int surfaceContainerHigh;
        public final int surfaceBright;
        public final int onSurface;
        public final int onSurfaceVariant;
        public final int outline;
        public final int outlineVariant;

        private Roles(boolean dark) {
            final float[] hsl = new float[3];
            ColorUtils.colorToHSL(SEED, hsl);
            final float hue = hsl[0];
            final float primarySat = Math.min(hsl[1], 0.85f);
            final float tertiaryHue = (hue + 60f) % 360f;

            if (dark) {
                primary = tone(hue, primarySat, 80);
                onPrimary = tone(hue, primarySat, 20);
                primaryContainer = tone(hue, primarySat, 30);
                onPrimaryContainer = tone(hue, primarySat, 90);
                secondaryContainer = tone(hue, 0.25f, 30);
                onSecondaryContainer = tone(hue, 0.25f, 90);
                tertiaryContainer = tone(tertiaryHue, 0.45f, 30);
                onTertiaryContainer = tone(tertiaryHue, 0.45f, 90);
                error = 0xFFF2B8B5;
                errorContainer = 0xFF8C1D18;
                onErrorContainer = 0xFFF9DEDC;
                surface = tone(hue, 0.05f, 6);
                surfaceContainer = tone(hue, 0.05f, 12);
                surfaceContainerHigh = tone(hue, 0.05f, 17);
                surfaceBright = tone(hue, 0.05f, 24);
                onSurface = tone(hue, 0.05f, 90);
                onSurfaceVariant = tone(hue, 0.10f, 80);
                outline = tone(hue, 0.10f, 60);
                outlineVariant = tone(hue, 0.10f, 30);
            } else {
                primary = tone(hue, primarySat, 40);
                onPrimary = 0xFFFFFFFF;
                primaryContainer = tone(hue, primarySat, 90);
                onPrimaryContainer = tone(hue, primarySat, 10);
                secondaryContainer = tone(hue, 0.25f, 90);
                onSecondaryContainer = tone(hue, 0.25f, 10);
                tertiaryContainer = tone(tertiaryHue, 0.45f, 90);
                onTertiaryContainer = tone(tertiaryHue, 0.45f, 10);
                error = 0xFFB3261E;
                errorContainer = 0xFFF9DEDC;
                onErrorContainer = 0xFF410E0B;
                surface = tone(hue, 0.05f, 98);
                surfaceContainer = tone(hue, 0.05f, 94);
                surfaceContainerHigh = tone(hue, 0.05f, 92);
                surfaceBright = tone(hue, 0.05f, 98);
                onSurface = tone(hue, 0.05f, 10);
                onSurfaceVariant = tone(hue, 0.10f, 30);
                outline = tone(hue, 0.10f, 50);
                outlineVariant = tone(hue, 0.10f, 80);
            }
        }
    }

    private static final Roles LIGHT = new Roles(false);
    private static final Roles DARK = new Roles(true);

    private LumineMd3() {
    }

    public static Roles roles() {
        return Theme.isCurrentThemeDark() ? DARK : LIGHT;
    }

    private static int tone(float hue, float saturation, int tone) {
        return ColorUtils.HSLToColor(new float[]{hue, saturation, tone / 100f});
    }
}
