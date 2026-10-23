// ======================================================
// luminegram - iOS style chat input preview
// ======================================================

package app.lumine.ios

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme

class IosChatInputPreview(context: Context) : IosPreviewView(context) {

    private val clip = Path()

    override fun onDraw(canvas: Canvas) {
        layoutBackdrop()
        drawBackdrop(canvas)

        val capsules = IosInputHelper.isAppearance()
        val attachOutside = IosInputHelper.isButtonPlacement()
        val hint = LocaleController.getString(R.string.TypeMessage)
        val hintColor = Theme.getColor(Theme.key_chat_messagePanelHint)
        val iconColor = Theme.getColor(Theme.key_chat_messagePanelIcons)

        val bubbleHeight = dp(30f)
        val bubbleWidth = backdrop.width() * 0.55f
        val bubbleTop = backdrop.top + dp(12f)
        drawBubble(canvas, backdrop.left + dp(10f), bubbleTop, bubbleWidth, bubbleHeight, false)
        drawBubble(canvas, backdrop.right - dp(10f) - bubbleWidth, bubbleTop + bubbleHeight + dp(8f), bubbleWidth, bubbleHeight, true)

        val leftIcon = if (attachOutside) R.drawable.input_attach else R.drawable.input_smile
        val rightIcon = if (attachOutside) R.drawable.input_smile else R.drawable.input_attach

        if (capsules) drawCapsules(canvas, leftIcon, rightIcon, hint, hintColor, iconColor)
        else drawFlatBar(canvas, leftIcon, rightIcon, hint, hintColor, iconColor)
    }

    private fun drawCapsules(canvas: Canvas, leftIcon: Int, rightIcon: Int, hint: String, hintColor: Int, iconColor: Int) {
        val size = dp(BUTTON_DP)
        val gap = dp(GAP_DP)
        val rowBottom = backdrop.bottom - dp(EDGE_DP)
        val rowTop = rowBottom - size
        val centerY = (rowTop + rowBottom) / 2f

        val leftLeft = backdrop.left + dp(EDGE_DP)
        box.set(leftLeft, rowTop, leftLeft + size, rowBottom)
        drawGlass(canvas, box, PreviewPanel.INPUT)
        drawIcon(canvas, leftIcon, leftLeft + size / 2f, centerY, iconColor)

        val micRight = backdrop.right - dp(EDGE_DP)
        box.set(micRight - size, rowTop, micRight, rowBottom)
        drawGlass(canvas, box, PreviewPanel.INPUT)
        drawIcon(canvas, R.drawable.input_mic, micRight - size / 2f, centerY, iconColor)

        val fieldLeft = leftLeft + size + gap
        val fieldRight = micRight - size - gap
        box.set(fieldLeft, rowTop, fieldRight, rowBottom)
        drawGlass(canvas, box, PreviewPanel.INPUT)
        drawText(canvas, hint, fieldLeft + dp(16f), centerY + dp(6f), HINT_SP, hintColor, false, Paint.Align.LEFT)
        drawIcon(canvas, rightIcon, fieldRight - dp(24f), centerY, iconColor)
    }

    private fun drawFlatBar(canvas: Canvas, leftIcon: Int, rightIcon: Int, hint: String, hintColor: Int, iconColor: Int) {
        val barHeight = dp(BAR_DP)
        val barTop = backdrop.bottom - barHeight
        val centerY = barTop + barHeight / 2f

        canvas.save()
        clip.rewind()
        clip.addRoundRect(backdrop, dp(RADIUS_DP), dp(RADIUS_DP), Path.Direction.CW)
        canvas.clipPath(clip)
        box.set(backdrop.left, barTop, backdrop.right, backdrop.bottom)
        drawFill(canvas, box, Theme.getColor(Theme.key_chat_messagePanelBackground))
        canvas.restore()

        drawIcon(canvas, leftIcon, backdrop.left + dp(30f), centerY, iconColor)
        drawText(canvas, hint, backdrop.left + dp(60f), centerY + dp(6f), HINT_SP, hintColor, false, Paint.Align.LEFT)
        drawIcon(canvas, rightIcon, backdrop.right - dp(78f), centerY, iconColor)
        drawIcon(canvas, R.drawable.input_mic, backdrop.right - dp(30f), centerY, iconColor)
    }

    private companion object {
        const val BUTTON_DP = 44f
        const val GAP_DP = 8f
        const val EDGE_DP = 8f
        const val BAR_DP = 48f
        const val RADIUS_DP = 16f
        const val HINT_SP = 16f
    }
}
