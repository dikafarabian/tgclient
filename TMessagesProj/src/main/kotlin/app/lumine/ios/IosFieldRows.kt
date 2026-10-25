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
    private const val BASE_BOTTOM_DP = 10f
    private const val ROW_EXTRA_DP = 30f
    private const val RIGHT_MARGIN_DP = 20f
    private const val CURSOR_SLACK_DP = 2f

    private val multi = WeakHashMap<TextView, Boolean>()
    private val widened = WeakHashMap<TextView, Int>()

    @JvmStatic
    fun isMulti(view: TextView): Boolean = multi[view] == true

    @JvmStatic
    fun measureSpec(view: EditText, spec: Int, enabled: Boolean): Int {
        if (!enabled) {
            multi.remove(view)
            widened.remove(view)
        }
        val on = isMulti(view)
        val bottom = AndroidUtilities.dp(BASE_BOTTOM_DP) + if (on) AndroidUtilities.dp(ROW_EXTRA_DP) else 0
        if (view.paddingBottom != bottom) {
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
        }
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
}
