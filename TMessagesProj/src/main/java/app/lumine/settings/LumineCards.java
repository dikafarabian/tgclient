/*
 * luminegram - card and row builders shared by the settings pages.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.lumine.push.PushStatus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

final class LumineCards {

    static final class Row {
        final LinearLayout view;
        final TextView value;

        Row(LinearLayout view, TextView value) {
            this.view = view;
            this.value = value;
        }
    }

    private LumineCards() {
    }

    static LinearLayout card(Context context, LinearLayout parent, String title) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite)));
        card.setPadding(dp(16), title == null ? dp(4) : dp(14), dp(16), title == null ? dp(4) : dp(10));

        if (title != null) {
            TextView header = new TextView(context);
            header.setTextSize(14);
            header.setTypeface(AndroidUtilities.bold());
            header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            header.setText(title);
            card.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        parent.addView(card, params);
        return card;
    }

    static Row row(Context context, LinearLayout card, String label, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView name = new TextView(context);
        name.setTextSize(14);
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        name.setText(label);
        row.addView(name, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView value = new TextView(context);
        value.setTextSize(14);
        value.setGravity(Gravity.END);
        value.setSingleLine(true);
        value.setEllipsize(TextUtils.TruncateAt.END);
        value.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        valueParams.leftMargin = dp(16);
        row.addView(value, valueParams);

        if (onClick != null) {
            row.setBackground(Theme.getSelectorDrawable(false));
            row.setOnClickListener(onClick);
        }
        card.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return new Row(row, value);
    }

    static void setValue(TextView view, CharSequence text, int level) {
        int key = Theme.key_windowBackgroundWhiteBlackText;
        if (level == PushStatus.Health.GOOD) {
            key = Theme.key_windowBackgroundWhiteGreenText2;
        } else if (level == PushStatus.Health.BAD) {
            key = Theme.key_text_RedRegular;
        }
        view.setText(text);
        view.setTextColor(Theme.getColor(key));
    }

    static void info(Context context, LinearLayout parent, String text) {
        TextView info = new TextView(context);
        info.setTextSize(13);
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        info.setText(text);
        info.setPadding(dp(8), 0, dp(8), 0);
        parent.addView(info, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
