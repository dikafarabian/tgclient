/*
 * luminegram - Ghost Mode page: master switch and the packet options.
 * Opened from the Stealth tab and from the Ghost Mode card on Home.
 */
package app.lumine.settings;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.ghost.LumineGhost;
import app.lumine.ghost.LumineGhostRequests;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Components.Switch;

public class LumineGhostActivity extends LumineSubPage {

    private static final int[] LABELS = {
            R.string.LumineGhostNoRead,
            R.string.LumineGhostNoStories,
            R.string.LumineGhostNoOnline,
            R.string.LumineGhostNoTyping,
            R.string.LumineGhostOfflineAfterSend
    };

    private Switch master;
    private LumineCards.Row countRow;
    private final Switch[] toggles = new Switch[LumineGhost.COUNT];
    private final View[] rows = new View[LumineGhost.COUNT];

    @Override
    protected int titleRes() {
        return R.string.LumineGhostMode;
    }

    @Override
    protected void build(Context context, LinearLayout list) {
        LinearLayout main = LumineCards.card(context, list, null);
        master = LumineCards.toggleRow(context, main, LumineStrings.get(R.string.LumineGhostMode), null, LumineGhost.active(), this::onMaster);
        countRow = LumineCards.row(context, main, LumineStrings.get(R.string.LumineGhostActiveFeatures), null);

        LinearLayout packets = LumineCards.card(context, list, LumineStrings.get(R.string.LumineGhostPackets));
        for (int i = 0; i < LumineGhost.COUNT; i++) {
            final int option = i;
            final Switch toggle = LumineCards.toggleRow(context, packets, LumineStrings.get(LABELS[i]), null, LumineGhost.get(i), enabled -> {
            });
            final View row = (View) toggle.getParent();
            row.setOnClickListener(v -> onOption(option));
            row.setOnLongClickListener(v -> onLock(option));
            toggles[i] = toggle;
            rows[i] = row;
        }
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineGhostLockInfo));

        LinearLayout after = LumineCards.card(context, list, null);
        LumineCards.toggleRow(context, after, LumineStrings.get(R.string.LumineGhostMarkReadAfterSend), null,
                LumineGhost.markReadAfterSend(), LumineGhost::setMarkReadAfterSend);
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineGhostMarkReadAfterSendInfo));

        refresh();
    }

    private void onMaster(boolean enabled) {
        LumineGhost.setActive(enabled);
        LumineGhost.clearAllowedRead();
        LumineGhostRequests.sendStatus(currentAccount, enabled);
        refresh();
    }

    private void onOption(int option) {
        if (LumineGhost.locked(option)) {
            AndroidUtilities.shakeViewSpring(rows[option], -4);
            return;
        }
        LumineGhost.set(option, !LumineGhost.get(option));
        if (option == LumineGhost.READ_MESSAGES) {
            LumineGhost.clearAllowedRead();
        }
        refresh();
    }

    private boolean onLock(int option) {
        final boolean locked = LumineGhost.locked(option);
        if (!locked && LumineGhost.lockedCount() >= LumineGhost.MAX_LOCKED) {
            AndroidUtilities.shakeViewSpring(rows[option], -4);
            return true;
        }
        LumineGhost.setLocked(option, !locked);
        refresh();
        return true;
    }

    private void refresh() {
        master.setChecked(LumineGhost.active(), true);
        for (int i = 0; i < LumineGhost.COUNT; i++) {
            toggles[i].setChecked(LumineGhost.get(i), true);
            rows[i].setAlpha(LumineGhost.locked(i) ? 0.5f : 1f);
        }
        LumineCards.setValue(countRow.value, LumineGhost.selectedCount() + "/" + LumineGhost.COUNT, 0);
    }
}
