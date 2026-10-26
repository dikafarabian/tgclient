// ======================================================
// luminegram - full width text with icons on their own row
// ======================================================

package app.lumine.ios

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import org.telegram.messenger.AndroidUtilities
import java.util.WeakHashMap

object IosFieldRows {
    private const val ROW_EXTRA_DP = 36f
    private const val RIGHT_MARGIN_DP = 20f
    private const val CURSOR_SLACK_DP = 2f

    private const val DEBUG_OVERLAY = true
    private val debugText = Paint().apply { color = Color.WHITE }
    private val debugBg = Paint().apply { color = 0xCCB00020.toInt() }

    private val multi = WeakHashMap<TextView, Boolean>()
    private val widened = WeakHashMap<TextView, Int>()

    @JvmStatic
    fun isMulti(view: TextView): Boolean = multi[view] == true

    @JvmStatic
    fun extraBottom(view: TextView): Int = if (isMulti(view)) AndroidUtilities.dp(ROW_EXTRA_DP) else 0

    @JvmStatic
    fun measureSpec(view: EditText, spec: Int, enabled: Boolean): Int {
        if (!enabled) {
            multi.remove(view)
            widened.remove(view)
        }
        val on = isMulti(view)
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (!on || lp == null) {
            widened.remove(view)
            return spec
        }
        val extra = maxOf(0, lp.rightMargin - AndroidUtilities.dp(RIGHT_MARGIN_DP))
        widened[view] = extra
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(spec) + extra, View.MeasureSpec.EXACTLY)
    }

    @JvmStatic
    fun updateMode(view: EditText, enabled: Boolean): Boolean {
        if (!enabled) return false
        val layout = view.layout ?: return false
        val text = view.text ?: return false
        if (!isMulti(view)) {
            if (layout.lineCount < 2) return false
            multi[view] = true
            return true
        }
        if (layout.lineCount != 1 || text.indexOf('\n') >= 0) return false
        val avail = view.measuredWidth - (widened[view] ?: 0) - IosFieldIndent.indentOf(view) - AndroidUtilities.dp(CURSOR_SLACK_DP)
        if (layout.getLineWidth(0) > avail) return false
        multi.remove(view)
        return true
    }

    @JvmStatic
    fun drawDebug(canvas: Canvas, view: EditText) {
        if (!DEBUG_OVERLAY) return
        val layout = view.layout
        val a = "m=${isMulti(view)} ln=${layout?.lineCount} h=${view.height} lh=${layout?.height}"
        val b = "pt=${view.extendedPaddingTop} pb=${view.extendedPaddingBottom} sy=${view.scrollY} len=${view.text?.length} sel=${view.selectionStart}"
        debugText.textSize = AndroidUtilities.dp(9f).toFloat()
        val x = view.left + AndroidUtilities.dp(8f).toFloat()
        val y = view.top + AndroidUtilities.dp(12f).toFloat()
        val w = maxOf(debugText.measureText(a), debugText.measureText(b)) + AndroidUtilities.dp(6f)
        canvas.drawRect(x - AndroidUtilities.dp(3f), y - AndroidUtilities.dp(10f), x + w, y + AndroidUtilities.dp(14f), debugBg)
        canvas.drawText(a, x, y, debugText)
        canvas.drawText(b, x, y + AndroidUtilities.dp(11f), debugText)
    }
}
