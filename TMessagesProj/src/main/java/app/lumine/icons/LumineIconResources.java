/*
 * luminegram - Resources wrapper that asks the icon pack engine before the stock loader.
 * Based on exteraless/exteraless (tw.nekomimi.nekogram.ui.icons.IconsResources), rewritten.
 */
package app.lumine.icons;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;

@SuppressLint("UseCompatLoadingForDrawables")
@SuppressWarnings("deprecation")
public final class LumineIconResources extends Resources {

    private static volatile LumineIconResources current;
    private static AssetManager currentAssets;
    private static Configuration currentConfig;
    private static DisplayMetrics currentMetrics;

    public static Resources wrap(Resources base) {
        if (!LumineIconConfig.enabled()) {
            return base;
        }
        synchronized (LumineIconResources.class) {
            AssetManager assets = base.getAssets();
            Configuration config = base.getConfiguration();
            DisplayMetrics metrics = base.getDisplayMetrics();
            LumineIconResources wrapped = current;
            if (wrapped == null || assets != currentAssets || !config.equals(currentConfig) || !metrics.equals(currentMetrics)) {
                wrapped = new LumineIconResources(base);
                current = wrapped;
                currentAssets = assets;
                currentConfig = new Configuration(config);
                currentMetrics = new DisplayMetrics();
                currentMetrics.setTo(metrics);
            }
            return wrapped;
        }
    }

    private LumineIconResources(Resources base) {
        super(base.getAssets(), base.getDisplayMetrics(), base.getConfiguration());
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return getDrawableForDensity(id, 0, null);
    }

    @Override
    public Drawable getDrawable(int id, @Nullable Theme theme) throws NotFoundException {
        return getDrawableForDensity(id, 0, theme);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return getDrawableForDensity(id, density, null);
    }

    @Nullable
    @Override
    public Drawable getDrawableForDensity(int id, int density, @Nullable Theme theme) {
        Drawable replacement = LumineIconManager.get().drawable(id, density, theme);
        return replacement != null ? replacement : super.getDrawableForDensity(id, density, theme);
    }
}
