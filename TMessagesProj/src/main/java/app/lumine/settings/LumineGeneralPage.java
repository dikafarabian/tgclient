/*
 * luminegram - General tab of the luminegram settings.
 * A list of sections; each section is a row that opens its own page, plus the Profile card.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import app.lumine.stealth.LumineStealth;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

public class LumineGeneralPage extends LinearLayout {

    private final LumineCards.Row notificationRow;

    public LumineGeneralPage(Context context, BaseFragment fragment) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(0, 0, 0, 0);
        final LinearLayout list = this;

        LinearLayout card = LumineCards.card(context, list, null);
        notificationRow = LumineCards.row(context, card, LumineStrings.get(R.string.LumineNotification), v -> fragment.presentFragment(new LumineNotificationActivity()));

        LinearLayout profile = LumineCards.card(context, list, LumineStrings.get(R.string.LumineProfile));
        LumineCards.toggleRow(context, profile, LumineStrings.get(R.string.LumineRelativeLastSeen),
                LocaleController.formatString(R.string.LastSeenDateFormatted, LocaleController.formatRelativeDate(5 * 60)),
                LumineStealth.relativeLastSeen(), LumineStealth::setRelativeLastSeen);
        LumineCards.toggleRow(context, profile, LumineStrings.get(R.string.LumineHidePhoneNumber), null,
                LumineStealth.hidePhone(), LumineStealth::setHidePhone);

        final CharSequence[] options = {
                LocaleController.getString(R.string.Hide),
                LumineStrings.get(R.string.LumineIdTypeApi),
                LumineStrings.get(R.string.LumineIdTypeBotApi)
        };
        final LumineCards.Row idRow = LumineCards.row(context, profile, LumineStrings.get(R.string.LumineShowIdAndDc), null);
        LumineCards.setValue(idRow.value, options[LumineStealth.idDcType()], 0);
        idRow.view.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LumineStrings.get(R.string.LumineShowIdAndDc));
            builder.setItems(options, (dialog, which) -> {
                LumineStealth.setIdDcType(which);
                LumineCards.setValue(idRow.value, options[which], 0);
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            builder.show();
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineShowIdAndDcInfo));

        refresh();
    }

    public void refresh() {
        final PushStatus.Health health = PushStatus.health(UserConfig.selectedAccount);
        LumineCards.setValue(notificationRow.value, PushStatus.typeName(PushConfig.type()) + " \u00b7 " + health.text, health.level);
    }
}
