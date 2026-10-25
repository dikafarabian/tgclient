// ======================================================
// luminegram - full width text with icons on their own row
// ======================================================

package app.lumine.ios

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

    private const val SINGLE = 0
    private const val SHORT = 1
    private const val LONG = 2

    private val states = WeakHashMap<TextView, Int>()
    private val widened = WeakHashMap<TextView, Int>()
    private val active = WeakHashMap<TextView, Boolean>()

    private fun stateOf(view: TextView): Int = states[view] ?: SINGLE

    @JvmStatic
    fun hasIconRow(view: TextView): Boolean = stateOf(view) == LONG

    @JvmStatic
    fun extraBottom(view: TextView): Int = if (hasIconRow(view)) AndroidUtilities.dp(ROW_EXTRA_DP) else 0

    @JvmStatic
    fun measureSpec(view: EditText, spec: Int, enabled: Boolean): Int {
        if (enabled) {
            active[view] = true
        } else {
            active.remove(view)
            states.remove(view)
            widened.remove(view)
        }
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (stateOf(view) == SINGLE || lp == null) {
            widened.remove(view)
            return spec
        }
        val extra = maxOf(0, lp.rightMargin - AndroidUtilities.dp(RIGHT_MARGIN_DP))
        widened[view] = extra
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(spec) + extra, View.MeasureSpec.EXACTLY)
    }

    @JvmStatic
    fun refresh(view: EditText) {
        if (active[view] != true) return
        if (updateMode(view, true)) view.requestLayout()
    }

    @JvmStatic
    fun updateMode(view: EditText, enabled: Boolean): Boolean {
        if (!enabled) return false
        val layout = view.layout ?: return false
        val text = view.text ?: return false
        val state = stateOf(view)
        val lines = layout.lineCount
        if (state == SINGLE) {
            if (lines < 2) return false
            states[view] = SHORT
            return true
        }
        val baseWidth = view.measuredWidth - (widened[view] ?: 0) - AndroidUtilities.dp(CURSOR_SLACK_DP)
        if (lines == 1 && text.indexOf('\n') < 0 && layout.getLineWidth(0) <= baseWidth - IosFieldIndent.indentOf(view)) {
            states.remove(view)
            return true
        }
        if (state == SHORT) {
            if (layout.getLineMax(lines - 1) <= baseWidth) return false
            states[view] = LONG
            return true
        }
        if (text.isEmpty() || text[text.length - 1] == '\n') {
            states[view] = SHORT
            return true
        }
        return false
    }
}
