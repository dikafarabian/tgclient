// ======================================================
// luminegram - scrollable checkerboard rows for the style preview
// ======================================================

package app.lumine.ios

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.Components.RecyclerListView

class IosPreviewRowsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = ROW_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        RecyclerListView.Holder(CheckerRow(parent.context))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit

    private class CheckerRow(context: Context) : View(context) {

        private val checker = IosCheckerDrawable()

        init {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AndroidUtilities.dp(ROW_HEIGHT_DP.toFloat()),
            )
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(ROW_HEIGHT_DP.toFloat()))
        }

        override fun onDraw(canvas: Canvas) {
            checker.setBounds(0, 0, width, height)
            checker.draw(canvas)
        }
    }

    private companion object {
        const val ROW_COUNT = 40
        const val ROW_HEIGHT_DP = 96
    }
}
