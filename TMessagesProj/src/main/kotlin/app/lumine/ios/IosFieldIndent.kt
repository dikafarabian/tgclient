// ======================================================
// luminegram - indent only the last line of the chat input
// ======================================================

package app.lumine.ios

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.LeadingMarginSpan
import android.text.style.UpdateLayout
import android.widget.EditText
import android.widget.TextView
import org.telegram.ui.Components.EditTextBoldCursor
import java.util.WeakHashMap

object IosFieldIndent {
    private const val MAX_SYNC_PASSES = 3
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
    fun attach(view: EditText) {
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                IosFieldRows.refresh(view)
                for (pass in 0 until MAX_SYNC_PASSES) {
                    if (!sync(view)) break
                }
            }
        })
    }

    @JvmStatic
    fun indentOf(view: TextView): Int = indents[view] ?: 0

    @JvmStatic
    fun setIndent(view: TextView, indentPx: Int) {
        val current = indents[view] ?: 0
        if (current == indentPx) return
        if (indentPx > 0) indents[view] = indentPx else indents.remove(view)
        (view as? EditTextBoldCursor)?.hintLayoutOffset = indentPx
        (view as? EditText)?.let { sync(it) }
        view.requestLayout()
    }

    @JvmStatic
    fun trailingCursorShift(view: TextView): Float {
        val indentPx = if (IosFieldRows.hasIconRow(view)) 0 else indents[view] ?: 0
        if (indentPx <= 0) return 0f
        val text = view.text ?: return 0f
        val atEmptyLastLine = (text.isEmpty() || text[text.length - 1] == '\n')
            && view.selectionStart == text.length && view.selectionEnd == text.length
        return if (atEmptyLastLine) indentPx.toFloat() else 0f
    }

    @JvmStatic
    fun sync(view: EditText): Boolean {
        val text = view.text ?: return false
        val layout = view.layout ?: return false
        val indentPx = if (IosFieldRows.hasIconRow(view)) 0 else indents[view] ?: 0
        val current = text.getSpans(0, text.length, LastLineSpan::class.java)
        val paragraphStart = text.lastIndexOf('\n') + 1
        if (indentPx <= 0 || paragraphStart >= text.length) {
            current.forEach { text.removeSpan(it) }
            return current.isNotEmpty()
        }
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
