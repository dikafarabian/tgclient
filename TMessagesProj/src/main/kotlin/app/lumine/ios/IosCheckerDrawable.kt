// ======================================================
// luminegram - checkerboard background for the style preview
// ======================================================

package app.lumine.ios

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.BitmapShader
import android.graphics.Shader
import android.graphics.drawable.Drawable
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.Theme

class IosCheckerDrawable : Drawable() {

    private val paint = Paint().apply {
        val cell = AndroidUtilities.dp(CELL_DP.toFloat())
        val dark = Theme.isCurrentThemeDark()
        val first = if (dark) DARK_FIRST else LIGHT_FIRST
        val second = if (dark) DARK_SECOND else LIGHT_SECOND
        val tile = Bitmap.createBitmap(cell * 2, cell * 2, Bitmap.Config.ARGB_8888)
        val tileCanvas = Canvas(tile)
        val fill = Paint()
        fill.color = first
        tileCanvas.drawRect(0f, 0f, cell * 2f, cell * 2f, fill)
        fill.color = second
        tileCanvas.drawRect(0f, 0f, cell.toFloat(), cell.toFloat(), fill)
        tileCanvas.drawRect(cell.toFloat(), cell.toFloat(), cell * 2f, cell * 2f, fill)
        shader = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.OPAQUE

    private companion object {
        const val CELL_DP = 24
        const val DARK_FIRST = 0xFF1B1B1F.toInt()
        const val DARK_SECOND = 0xFF2C2C32.toInt()
        const val LIGHT_FIRST = 0xFFF4F4F7.toInt()
        const val LIGHT_SECOND = 0xFFDEDEE4.toInt()
    }
}
