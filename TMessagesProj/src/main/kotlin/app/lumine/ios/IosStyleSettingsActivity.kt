// ======================================================
// luminegram - iOS style settings page
// ======================================================

package app.lumine.ios

import android.content.Context
import android.os.Build
import android.view.View
import app.exteraless.appearance.AltSeekbar
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawableRenderNode
import kotlin.math.roundToInt

class IosStyleSettingsActivity : UniversalFragment() {

    private var angleSlider: AltSeekbar? = null
    private var intensitySlider: AltSeekbar? = null
    private var tintSlider: AltSeekbar? = null

    override fun getTitle(): CharSequence = LocaleController.getString(R.string.LumineIosStyle)

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter?) {
        val context = context ?: return
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineLiquidGlass)))
        items.add(UItem.asCustom(angleSlider(context), SLIDER_HEIGHT_DP))
        items.add(UItem.asCustom(intensitySlider(context), SLIDER_HEIGHT_DP))
        items.add(UItem.asCustom(tintSlider(context), SLIDER_HEIGHT_DP))
        items.add(UItem.asShadow())
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) = Unit

    override fun onLongClick(item: UItem, view: View, position: Int, x: Float, y: Float): Boolean = false

    private fun angleSlider(context: Context): AltSeekbar = angleSlider ?: DegreeSeekbar(
        context,
        { value ->
            IosStyleConfig.glassAngle.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 360,
        LocaleController.getString(R.string.LumineLiquidGlassAngle),
        "0°", "360°",
    ).also {
        it.setProgress(IosStyleConfig.glassAngle.Int().toFloat())
        angleSlider = it
    }

    private fun intensitySlider(context: Context): AltSeekbar = intensitySlider ?: PercentSeekbar(
        context,
        { value ->
            IosStyleConfig.glassIntensity.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 150,
        LocaleController.getString(R.string.LumineLiquidGlassIntensity),
        "0%", "150%",
    ).also {
        it.setProgress(IosStyleConfig.glassIntensity.Int().toFloat())
        intensitySlider = it
    }

    private fun tintSlider(context: Context): AltSeekbar = tintSlider ?: PercentSeekbar(
        context,
        { value ->
            IosStyleConfig.glassTint.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 100,
        LocaleController.getString(R.string.LumineLiquidGlassTint),
        "0%", "100%",
    ).also {
        it.setProgress(IosStyleConfig.glassTint.Int().toFloat())
        tintSlider = it
    }

    private fun refreshLiquidGlassEffects() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BlurredBackgroundDrawableRenderNode.invalidateLiquidGlassDrawables()
        }
    }

    private class PercentSeekbar(
        context: Context,
        onDrag: AltSeekbar.OnDrag,
        min: Int,
        max: Int,
        title: String,
        left: String,
        right: String,
    ) : AltSeekbar(context, onDrag, min, max, title, left, right) {
        override fun getTextForHeader(): CharSequence = "$roundedValue%"
    }

    private class DegreeSeekbar(
        context: Context,
        onDrag: AltSeekbar.OnDrag,
        min: Int,
        max: Int,
        title: String,
        left: String,
        right: String,
    ) : AltSeekbar(context, onDrag, min, max, title, left, right) {
        override fun getTextForHeader(): CharSequence = "$roundedValue°"
    }

    private companion object {
        const val SLIDER_HEIGHT_DP = 112
    }
}
