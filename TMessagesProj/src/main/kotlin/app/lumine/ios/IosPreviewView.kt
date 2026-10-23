// ======================================================
// luminegram - iOS style settings preview base
// ======================================================

package app.lumine.ios

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.View
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.blur3.GlassOutlineStyle
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable

enum class PreviewPanel { HEADER, INPUT }

abstract class IosPreviewView(context: Context) : View(context) {

    protected val box = RectF()
    protected val backdrop = RectF()

    private val scratch = RectF()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val icons = HashMap<Int, Drawable>()

    protected val sampleName: String
        get() {
            val user = UserConfig.getInstance(UserConfig.selectedAccount).currentUser
            return (if (user != null) UserObject.getUserName(user) else null).orEmpty().ifBlank { FALLBACK_NAME }
        }

    protected fun dp(value: Float): Float = AndroidUtilities.dpf2(value)

    protected fun layoutBackdrop() {
        val margin = dp(BACKDROP_MARGIN_DP)
        backdrop.set(margin, margin, width - margin, height - margin)
    }

    protected fun drawBackdrop(canvas: Canvas) {
        fillPaint.color = Theme.getColor(Theme.key_windowBackgroundGray)
        canvas.drawRoundRect(backdrop, dp(BACKDROP_RADIUS_DP), dp(BACKDROP_RADIUS_DP), fillPaint)
    }

    protected fun drawBubble(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, outgoing: Boolean) {
        scratch.set(left, top, left + width, top + height)
        fillPaint.color = Theme.getColor(if (outgoing) Theme.key_chat_outBubble else Theme.key_chat_inBubble)
        canvas.drawRoundRect(scratch, dp(BUBBLE_RADIUS_DP), dp(BUBBLE_RADIUS_DP), fillPaint)
    }

    protected fun drawFill(canvas: Canvas, area: RectF, color: Int) {
        fillPaint.color = color
        canvas.drawRect(area, fillPaint)
    }

    protected fun drawCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        fillPaint.color = color
        canvas.drawCircle(cx, cy, radius, fillPaint)
    }

    protected fun drawAvatar(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        drawCircle(canvas, cx, cy, size / 2f, Theme.getColor(Theme.key_chats_actionBackground))
        drawText(canvas, sampleName.take(1).uppercase(), cx, cy + dp(6f), 17f, Theme.getColor(Theme.key_chats_actionIcon), true, Paint.Align.CENTER)
    }

    protected fun drawGlass(canvas: Canvas, area: RectF, panel: PreviewPanel) {
        val dark = Theme.isCurrentThemeDark()
        val backgroundKey = if (panel == PreviewPanel.HEADER) Theme.key_chat_topPanelBackground else Theme.key_chat_messagePanelBackground
        val strokeTop = when {
            !dark -> WHITE
            panel == PreviewPanel.HEADER -> 0x20FFFFFF
            else -> 0x28FFFFFF
        }
        val strokeBottom = if (dark) 0x14FFFFFF else WHITE
        val radius = area.height() / 2f

        fillPaint.color = Theme.multAlpha(Theme.getColor(backgroundKey), GLASS_ALPHA)
        canvas.drawRoundRect(area, radius, radius, fillPaint)

        when (GlassOutlineStyle.current()) {
            GlassOutlineStyle.GLARE -> {
                strokePaint.strokeWidth = dp(1f)
                strokePaint.color = strokeTop
                BlurredBackgroundDrawable.drawStroke(canvas, area, radius, dp(1f), true, strokePaint)
                strokePaint.strokeWidth = dp(2f / 3f)
                strokePaint.color = strokeBottom
                BlurredBackgroundDrawable.drawStroke(canvas, area, radius, dp(2f / 3f), false, strokePaint)
            }

            GlassOutlineStyle.SOLID -> {
                scratch.set(area)
                scratch.inset(dp(0.5f), dp(0.5f))
                strokePaint.strokeWidth = dp(1f)
                strokePaint.color = Theme.getDividerColor(null)
                canvas.drawRoundRect(scratch, radius, radius, strokePaint)
            }

            else -> Unit
        }
    }

    protected fun drawIcon(canvas: Canvas, resId: Int, cx: Float, cy: Float, color: Int) {
        val icon = icons.getOrPut(resId) { context.getDrawable(resId)!!.mutate() }
        icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        val halfWidth = icon.intrinsicWidth / 2f
        val halfHeight = icon.intrinsicHeight / 2f
        icon.setBounds(
            (cx - halfWidth).toInt(),
            (cy - halfHeight).toInt(),
            (cx + halfWidth).toInt(),
            (cy + halfHeight).toInt(),
        )
        icon.draw(canvas)
    }

    protected fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        baseline: Float,
        size: Float,
        color: Int,
        bold: Boolean,
        align: Paint.Align,
    ) {
        applyText(size, color, bold, align)
        canvas.drawText(text, x, baseline, textPaint)
    }

    protected fun measureText(text: String, size: Float, bold: Boolean): Float {
        applyText(size, 0, bold, Paint.Align.LEFT)
        return textPaint.measureText(text)
    }

    private fun applyText(size: Float, color: Int, bold: Boolean, align: Paint.Align) {
        textPaint.textSize = dp(size)
        textPaint.color = color
        textPaint.typeface = if (bold) AndroidUtilities.bold() else Typeface.DEFAULT
        textPaint.textAlign = align
    }

    private companion object {
        const val FALLBACK_NAME = "Luminegram"
        const val WHITE = -1
        const val GLASS_ALPHA = 0.85f
        const val BACKDROP_MARGIN_DP = 12f
        const val BACKDROP_RADIUS_DP = 16f
        const val BUBBLE_RADIUS_DP = 14f
    }
}
