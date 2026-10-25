// ======================================================
// luminegram - indent only the last line of the chat input
// ======================================================

package app.lumine.ios

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.UpdateLayout
import android.widget.EditText
import android.widget.TextView
import java.util.WeakHashMap

object IosFieldIndent {
    private val indents = WeakHashMap<TextView, Int>()

    private class LastLineSpan(
        private val indentPx: Int,
        private val upperLines: Int
    ) : LeadingMarginSpan.LeadingMarginSpan2, UpdateLayout {

        fun matches(indentPx: Int, upperLines: Int): Boolean =
            this.indentPx == indentPx && this.upperLines == upperLines

        override fun getLeadingMargin(first: Boolean): Int =
            if (upperLines == 0 || !first) indentPx else 0

        override fun getLeadingMarginLineCount(): Int = upperLines

        override fun drawLeadingMargin(
            canvas: Canvas?, paint: Paint?, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
            text: CharSequence?, start: Int, end: Int, first: Boolean, layout: Layout?
        ) = Unit
    }

    @JvmStatic
    fun setIndent(view: TextView, indentPx: Int) {
        val current = indents[view] ?: 0
        if (current == indentPx) return
        if (indentPx > 0) indents[view] = indentPx else indents.remove(view)
        view.requestLayout()
    }

    @JvmStatic
    fun emptyFieldPadding(view: TextView): Int {
        val text = view.text
        return if (text != null && text.isEmpty()) indents[view] ?: 0 else 0
    }

    @JvmStatic
    fun sync(view: EditText): Boolean {
        val text = view.text ?: return false
        val layout = view.layout ?: return false
        val indentPx = indents[view] ?: 0
        val current = text.getSpans(0, text.length, LastLineSpan::class.java)
        if (indentPx <= 0 || text.isEmpty()) {
            current.forEach { text.removeSpan(it) }
            return current.isNotEmpty()
        }
        val paragraphStart = text.lastIndexOf('\n') + 1
        val upperLines = layout.lineCount - 1 - layout.getLineForOffset(paragraphStart)
        val existing = current.singleOrNull()
        if (existing != null
            && existing.matches(indentPx, upperLines)
            && text.getSpanStart(existing) == paragraphStart
            && text.getSpanEnd(existing) == text.length
        ) return false
        current.forEach { text.removeSpan(it) }
        text.setSpan(LastLineSpan(indentPx, upperLines), paragraphStart, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return true
    }
}
