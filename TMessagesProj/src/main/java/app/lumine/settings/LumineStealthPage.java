/*
 * luminegram - Stealth tab of the luminegram settings.
 * Sections open their own pages; simple options sit directly in a card.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.ghost.LumineGhost;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

public class LumineStealthPage extends ScrollView {

    private final LumineCards.Row ghostRow;

    public LumineStealthPage(Context context, BaseFragment fragment) {
        super(context);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout ghost = LumineCards.card(context, list, LumineStrings.get(R.string.LumineStealthGhost));
        ghostRow = LumineCards.row(context, ghost, LumineStrings.get(R.string.LumineGhostMode), v -> fragment.presentFragment(new LumineGhostActivity()));
        LumineCards.toggleRow(context, ghost, LumineStrings.get(R.string.LumineGhostAskStory), null,
                LumineGhost.askBeforeStory(), LumineGhost::setAskBeforeStory);
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineGhostAskStoryInfo));

        refresh();
    }

    public void refresh() {
        LumineCards.setValue(ghostRow.value, LumineGhost.selectedCount() + "/" + LumineGhost.COUNT, 0);
    }
}
