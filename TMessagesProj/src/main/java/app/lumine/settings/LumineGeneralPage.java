/*
 * luminegram - General tab of the luminegram settings.
 * A list of sections; each section is a row that opens its own page.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

public class LumineGeneralPage extends ScrollView {

    private final LumineCards.Row notificationRow;

    public LumineGeneralPage(Context context, BaseFragment fragment) {
        super(context);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout card = LumineCards.card(context, list, null);
        notificationRow = LumineCards.row(context, card, LumineStrings.get(R.string.LumineNotification), v -> fragment.presentFragment(new LumineNotificationActivity()));

        refresh();
    }

    public void refresh() {
        final PushStatus.Health health = PushStatus.health(UserConfig.selectedAccount);
        LumineCards.setValue(notificationRow.value, PushStatus.typeName(PushConfig.type()) + " \u00b7 " + health.text, health.level);
    }
}
