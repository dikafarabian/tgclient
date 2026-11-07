/*
 * luminegram - Material 3 foundation for the luminegram screens.
 * Role colors derived from the active theme (accent plus theme surface and
 * text colors, HSL tone mapping for the tonal roles), plus shape and spacing
 * constants. Own code, no upstream hooks.
 */
package app.lumine.md3;

import androidx.core.graphics.ColorUtils;

import app.lumine.LumineConfig;
import org.telegram.ui.ActionBar.Theme;

public final class LumineMd3 {

    public static final String KEY_SWITCH = "LumineMd3Switch";
    public static final String KEY_SLIDER = "LumineMd3Slider";
    public static final String KEY_LIST = "LumineMd3ListItems";

    private static volatile int switchState = -1;
    private static volatile int sliderState = -1;
    private static volatile int listState = -1;

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

        private final int cacheAccent;
        private final boolean cacheDark;
        private final int cacheBackground;
        private final int cacheContainer;
        private final int cacheText;

        private Roles(boolean dark, int accent, int background, int container, int text, int textVariant, int divider) {
            cacheAccent = accent;
            cacheDark = dark;
            cacheBackground = background;
            cacheContainer = container;
            cacheText = text;

            final float[] hsl = new float[3];
            ColorUtils.colorToHSL(accent, hsl);
            final float hue = hsl[0];
            final float primarySat = Math.min(hsl[1], 0.85f);
            final float tertiaryHue = (hue + 60f) % 360f;

            surface = background;
            surfaceContainer = container;
            surfaceContainerHigh = ColorUtils.blendARGB(container, text, 0.04f);
            surfaceBright = container;
            onSurface = text;
            onSurfaceVariant = textVariant;
            outline = ColorUtils.blendARGB(textVariant, container, 0.3f);
            outlineVariant = divider;

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
            }
        }

        private boolean matches(boolean dark, int accent, int background, int container, int text) {
            return cacheDark == dark && cacheAccent == accent && cacheBackground == background && cacheContainer == container && cacheText == text;
        }
    }

    private static Roles cached;

    private LumineMd3() {
    }

    public static Roles roles() {
        final boolean dark = Theme.isCurrentThemeDark();
        final int accent = Theme.getColor(Theme.key_chats_actionBackground) | 0xFF000000;
        final int background = Theme.getColor(Theme.key_windowBackgroundGray);
        final int container = Theme.getColor(Theme.key_windowBackgroundWhite);
        final int text = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
        Roles roles = cached;
        if (roles == null || !roles.matches(dark, accent, background, container, text)) {
            roles = new Roles(dark, accent, background, container, text, Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), Theme.getColor(Theme.key_divider));
            cached = roles;
        }
        return roles;
    }

    public static boolean switchEnabled() {
        int state = switchState;
        if (state < 0) {
            state = LumineConfig.getBoolean(KEY_SWITCH, false) ? 1 : 0;
            switchState = state;
        }
        return state == 1;
    }

    public static void setSwitchEnabled(boolean enabled) {
        LumineConfig.setBoolean(KEY_SWITCH, enabled);
        switchState = enabled ? 1 : 0;
    }

    public static boolean sliderEnabled() {
        int state = sliderState;
        if (state < 0) {
            state = LumineConfig.getBoolean(KEY_SLIDER, false) ? 1 : 0;
            sliderState = state;
        }
        return state == 1;
    }

    public static void setSliderEnabled(boolean enabled) {
        LumineConfig.setBoolean(KEY_SLIDER, enabled);
        sliderState = enabled ? 1 : 0;
    }

    public static boolean listEnabled() {
        int state = listState;
        if (state < 0) {
            state = LumineConfig.getBoolean(KEY_LIST, false) ? 1 : 0;
            listState = state;
        }
        return state == 1;
    }

    public static void setListEnabled(boolean enabled) {
        LumineConfig.setBoolean(KEY_LIST, enabled);
        listState = enabled ? 1 : 0;
    }

    private static int tone(float hue, float saturation, int tone) {
        return ColorUtils.HSLToColor(new float[]{hue, saturation, tone / 100f});
    }
}
