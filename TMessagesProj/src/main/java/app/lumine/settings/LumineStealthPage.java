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
    }
}
