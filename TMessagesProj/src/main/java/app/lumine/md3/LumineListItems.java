/*
 * luminegram - Material 3 list items.
 * Decisions used by the hooks in the settings cells, the shadow rows and the
 * section drawing of RecyclerListView. Everything returns the stock value
 * while the option is off.
 */
package app.lumine.md3;

public final class LumineListItems {

    private LumineListItems() {
    }

    public static boolean enabled() {
        return LumineMd3.listEnabled();
    }

    public static int rowHeight(int stockDp) {
        return stockDp == 50 && enabled() ? 56 : stockDp;
    }

    public static int detailRowHeight(int stockDp) {
        return stockDp == 64 && enabled() ? 72 : stockDp;
    }

    public static int shadowHeight(boolean nextIsHeader, int stockDp) {
        if (!enabled()) {
            return stockDp;
        }
        return nextIsHeader ? 10 : 16;
    }
}
