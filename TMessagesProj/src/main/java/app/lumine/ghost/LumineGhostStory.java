/*
 * luminegram - ask before opening a story.
 * StoryViewer calls shouldAsk() before it opens and ask() to let the person pick ghost or normal.
 */
package app.lumine.ghost;

import android.content.Context;

import app.lumine.LumineStrings;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.tl.TL_stories;
import org.telegram.ui.ActionBar.AlertDialog;

import java.util.ArrayList;

public final class LumineGhostStory {

    public interface Choice {
        void onChosen(boolean ghost);
    }

    private LumineGhostStory() {
    }

    public static boolean shouldAsk(int account, TL_stories.StoryItem storyItem, ArrayList<Long> peerIds, int position) {
        if (!LumineGhost.askBeforeStory() || LumineGhost.get(LumineGhost.READ_STORIES)) {
            return false;
        }
        long dialogId = 0;
        if (storyItem != null && storyItem.dialogId != 0) {
            dialogId = storyItem.dialogId;
        } else if (peerIds != null && position >= 0 && position < peerIds.size()) {
            dialogId = peerIds.get(position);
        }
        return dialogId != UserConfig.getInstance(account).getClientUserId();
    }

    public static void ask(Context context, Choice choice, Runnable cancelled) {
        final boolean[] chosen = new boolean[1];
        new AlertDialog.Builder(context)
                .setTitle(LumineStrings.get(R.string.LumineAskStoryTitle))
                .setItems(new CharSequence[]{
                        LumineStrings.get(R.string.LumineAskStoryGhost),
                        LumineStrings.get(R.string.LumineAskStoryNormal)
                }, (dialog, which) -> {
                    chosen[0] = true;
                    choice.onChosen(which == 0);
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setOnDismissListener(dialog -> {
                    if (!chosen[0]) {
                        cancelled.run();
                    }
                })
                .show();
    }
}
