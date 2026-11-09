/*
 * luminegram - message saving page.
 * Rows that depend on another option are rebuilt when that option changes.
 */
package app.lumine.settings;

import android.content.Context;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.saving.LumineSaving;
import org.telegram.messenger.R;

public class LumineSavingActivity extends LumineSubPage {

    @Override
    protected int titleRes() {
        return R.string.LumineSaving;
    }

    private void refill() {
        rebuild(160);
    }

    @Override
    protected void build(Context context, LinearLayout list) {
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
