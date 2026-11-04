// ======================================================
// luminegram - Material Design 3 list items
// ======================================================

package app.lumine.md3

object Md3ListItems {

    @JvmStatic
    fun enabled(): Boolean = Md3Config.listItemsEnabled()

    @JvmStatic
    fun rowHeight(stockDp: Int): Int = if (stockDp == 50 && enabled()) 56 else stockDp

    @JvmStatic
    fun detailRowHeight(stockDp: Int): Int = if (stockDp == 64 && enabled()) 72 else stockDp

    @JvmStatic
    fun shadowHeight(nextIsHeader: Boolean, stockDp: Int): Int {
        if (!enabled()) return stockDp
        return if (nextIsHeader) 10 else 16
    }
}
