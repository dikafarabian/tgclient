/*
 * luminegram - message saving page.
 * Rows that depend on another option are rebuilt when that option changes.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.saving.LumineSaving;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

public class LumineSavingActivity extends BaseFragment {

    private LinearLayout list;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LumineStrings.get(R.string.LumineSaving));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        final ScrollView scroll = new ScrollView(context);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        root.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        scroll.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        fill();
        return fragmentView;
    }

    private void refill() {
        AndroidUtilities.runOnUIThread(this::fill, 160);
    }

    private void fill() {
        if (list == null) {
            return;
        }
        final Context context = list.getContext();
        list.removeAllViews();

        final LinearLayout saving = LumineCards.card(context, list, LumineStrings.get(R.string.LumineSavingSectionSaving));
        LumineCards.toggleRow(context, saving, LumineStrings.get(R.string.LumineSavingDeleted), null, LumineSaving.deleted(), value -> {
            LumineSaving.setDeleted(value);
            refill();
        });
        LumineCards.toggleRow(context, saving, LumineStrings.get(R.string.LumineSavingEdits), null, LumineSaving.edits(), LumineSaving::setEdits);
        LumineCards.toggleRow(context, saving, LumineStrings.get(R.string.LumineSavingArchived), null, LumineSaving.archived(), LumineSaving::setArchived);

        if (LumineSaving.deleted()) {
            LumineCards.toggleRow(context, saving, LumineStrings.get(R.string.LumineSavingMedia), null, LumineSaving.media(), value -> {
                LumineSaving.setMedia(value);
                refill();
            });
            LumineCards.info(context, list, LumineStrings.get(R.string.LumineSavingMediaInfo));

            if (LumineSaving.media()) {
                final LinearLayout media = LumineCards.card(context, list, null);
                LumineCards.toggleRow(context, media, LumineStrings.get(R.string.LumineSavingMediaPrivateChats), null, LumineSaving.mediaPrivateChats(), LumineSaving::setMediaPrivateChats);
                LumineCards.toggleRow(context, media, LumineStrings.get(R.string.LumineSavingMediaPublicChannels), null, LumineSaving.mediaPublicChannels(), LumineSaving::setMediaPublicChannels);
                LumineCards.toggleRow(context, media, LumineStrings.get(R.string.LumineSavingMediaPrivateChannels), null, LumineSaving.mediaPrivateChannels(), LumineSaving::setMediaPrivateChannels);
                LumineCards.toggleRow(context, media, LumineStrings.get(R.string.LumineSavingMediaPublicGroups), null, LumineSaving.mediaPublicGroups(), LumineSaving::setMediaPublicGroups);
                LumineCards.toggleRow(context, media, LumineStrings.get(R.string.LumineSavingMediaPrivateGroups), null, LumineSaving.mediaPrivateGroups(), LumineSaving::setMediaPrivateGroups);
            }

            final LinearLayout where = LumineCards.card(context, list, LumineStrings.get(R.string.LumineSavingSectionIn));
            LumineCards.toggleRow(context, where, LumineStrings.get(R.string.LumineSavingInPrivate), null, LumineSaving.inPrivateChats(), LumineSaving::setInPrivateChats);
            LumineCards.toggleRow(context, where, LumineStrings.get(R.string.LumineSavingInGroups), null, LumineSaving.inGroups(), LumineSaving::setInGroups);
            LumineCards.toggleRow(context, where, LumineStrings.get(R.string.LumineSavingInChannels), null, LumineSaving.inChannels(), LumineSaving::setInChannels);
            LumineCards.toggleRow(context, where, LumineStrings.get(R.string.LumineSavingBotUser), null, LumineSaving.botUser(), value -> {
                LumineSaving.setBotUser(value);
                refill();
            });
            if (LumineSaving.botUser()) {
                LumineCards.toggleRow(context, where, LumineStrings.get(R.string.LumineSavingBotChat), null, LumineSaving.botChat(), LumineSaving::setBotChat);
            }
        }

        final LinearLayout presence = LumineCards.card(context, list, LumineStrings.get(R.string.LumineSavingSectionPresence));
        LumineCards.toggleRow(context, presence, LumineStrings.get(R.string.LumineSavingLastSeen), null, LumineSaving.lastSeen(), LumineSaving::setLastSeen);
        LumineCards.toggleRow(context, presence, LumineStrings.get(R.string.LumineSavingReadDate), null, LumineSaving.readDate(), LumineSaving::setReadDate);
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineSavingReadDateInfo));
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineSavingInert));
    }
}
