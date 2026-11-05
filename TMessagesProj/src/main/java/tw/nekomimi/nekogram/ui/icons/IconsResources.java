package tw.nekomimi.nekogram.ui.icons;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import app.lumine.icons.IconPackManager;
import xyz.nextalone.nagram.NaConfig;

@SuppressLint("UseCompatLoadingForDrawables")
public class IconsResources extends Resources {

    public static final int ICON_REPLACE_SOLAR = 1;
    public static final int ICON_REPLACE_REMIX = 2;
    private int _iconsType = -1;

    public IconsResources(Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        Drawable fromPack = IconPackManager.getInstance().getDrawable(this, id, 0, null);
        if (fromPack != null) {
            return fromPack;
        }
        try {
            return super.getDrawable(getConversion(id), null);
        } catch (NotFoundException e) {
            return super.getDrawable(id, null);
        }
    }

    @Override
    public Drawable getDrawable(int id, @Nullable Theme theme) throws NotFoundException {
        Drawable fromPack = IconPackManager.getInstance().getDrawable(this, id, 0, theme);
        if (fromPack != null) {
            return fromPack;
        }
        try {
            return super.getDrawable(getConversion(id), theme);
        } catch (NotFoundException e) {
            return super.getDrawable(id, theme);
        }
    }

    @Nullable
    @Override
    public Drawable getDrawableForDensity(int id, int density, @Nullable Theme theme) {
        Drawable fromPack = IconPackManager.getInstance().getDrawable(this, id, density, theme);
        if (fromPack != null) {
            return fromPack;
        }
        return getOriginalDrawableForDensity(id, density, theme);
    }

    @Nullable
    public Drawable getOriginalDrawableForDensity(int id, int density, @Nullable Theme theme) {
        try {
            return super.getDrawableForDensity(getConversion(id), density, theme);
        } catch (NotFoundException e) {
            return super.getDrawableForDensity(id, density, theme);
        }
    }

    @Nullable
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return getDrawableForDensity(id, density, null);
    }

    private int getConversion(int icon) {
        return getConversion(icon, -1);
    }

    private int getConversion(int icon, int forcedIconsType) {
        if (_iconsType == -1) {
            _iconsType = NaConfig.INSTANCE.getIconReplacements().Int();
        }

        int consideredIconsType = forcedIconsType == -1 ? _iconsType : forcedIconsType;

        return BaseIconPacks.getConversion(icon, consideredIconsType);
    }
}
