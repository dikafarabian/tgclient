// ======================================================
// luminegram - iOS style input bar helpers
// ======================================================

package app.lumine.ios

import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
import java.util.WeakHashMap

object IosInputHelper {
    private const val BUBBLE_INSET_DP = 1
    private const val TOP_VIEW_GAP_DP = 8

    private val slowModePill = WeakHashMap<ChatActivityEnterView, Int>()

    @JvmStatic
    fun bubbleInsetPx(): Int = AndroidUtilities.dp(BUBBLE_INSET_DP.toFloat())

    @JvmStatic
    fun isButtonPlacement(): Boolean = IosStyleConfig.inputButtonPlacement.Bool()

    @JvmStatic
    fun isAppearance(): Boolean = IosStyleConfig.inputAppearance.Bool()

    @JvmStatic
    fun getTopViewGapDp(): Int = if (isAppearance()) TOP_VIEW_GAP_DP else 0

    @JvmStatic
    fun drawBubble(canvas: Canvas, bubble: BlurredBackgroundDrawable?, view: View?) {
        if (bubble == null || view == null || view.visibility != View.VISIBLE || view.alpha <= 0) return
        drawFollowing(canvas, bubble, view, view.left, view.top, view.right, view.bottom)
    }

    @JvmStatic
    fun drawBubbleSquare(canvas: Canvas, bubble: BlurredBackgroundDrawable?, view: View?, sizeDp: Int) {
        if (bubble == null || view == null || view.visibility != View.VISIBLE || view.alpha <= 0) return
        val sizePx = AndroidUtilities.dp(sizeDp.toFloat())
        drawFollowing(canvas, bubble, view, view.right - sizePx, view.bottom - sizePx, view.right, view.bottom)
    }

    private fun drawFollowing(canvas: Canvas, bubble: BlurredBackgroundDrawable, view: View, l: Int, t: Int, r: Int, b: Int) {
        val moved = view.translationX != 0f || view.translationY != 0f || view.scaleX != 1f || view.scaleY != 1f
        if (moved) {
            canvas.save()
            canvas.translate(view.translationX, view.translationY)
            if (view.scaleX != 1f || view.scaleY != 1f) {
                canvas.scale(view.scaleX, view.scaleY, (l + r) / 2f, (t + b) / 2f)
            }
        }
        val inset = bubbleInsetPx()
        bubble.setBounds(l + inset, t + inset, r - inset, b - inset)
        bubble.alpha = (255 * view.alpha).toInt()
        bubble.draw(canvas)
        if (moved) canvas.restore()
    }

    @JvmStatic
    fun updateSlowModeContainer(enterView: ChatActivityEnterView, visible: Boolean, isPremiumMode: Boolean) {
        val container = enterView.messageEditTextContainer ?: return
        val lp = container.layoutParams as? FrameLayout.LayoutParams ?: return
        val targetMarginPx = if (visible && (isAppearance() || isButtonPlacement())) {
            val wanted = AndroidUtilities.dp(14f) + enterView.slowModePillWidth
            val stable = maxOf(wanted, slowModePill[enterView] ?: 0)
            slowModePill[enterView] = stable
            stable
        } else {
            slowModePill.remove(enterView)
            AndroidUtilities.dp(ChatActivityEnterView.DEFAULT_HEIGHT.toFloat())
        }
        if (lp.rightMargin != targetMarginPx) {
            lp.rightMargin = targetMarginPx
            container.layoutParams = lp
            enterView.textFieldContainer?.invalidate()
        }
    }
}
