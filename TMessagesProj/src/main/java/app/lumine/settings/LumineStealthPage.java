/*
 * luminegram - Stealth tab of the luminegram settings.
 * Profile section for now: relative last seen and hide phone number.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.stealth.LumineStealth;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;

public class LumineStealthPage extends ScrollView {

    public LumineStealthPage(Context context) {
        super(context);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

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
    }
}
