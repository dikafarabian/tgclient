/*
 * luminegram - row with a leading icon, a title and an optional subtitle.
 * Used by the Interface section of the Appearance tab.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.ui.ActionBar.Theme;

final class LumineIconRow {

    private LumineIconRow() {
    }

    static void add(Context context, LinearLayout card, int iconRes, String title, String subtitle, View.OnClickListener onClick) {
        final boolean segmented = LumineCards.segmented();
        final LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(segmented ? dp(16) : 0, dp(12), segmented ? dp(16) : 0, dp(12));

        final ImageView icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setImageResource(iconRes);
        icon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
        row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        final LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);

        final TextView name = new TextView(context);
        name.setTextSize(16);
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        name.setText(title);
        texts.addView(name, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (!TextUtils.isEmpty(subtitle)) {
            final TextView sub = new TextView(context);
            sub.setTextSize(13);
            sub.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            sub.setText(subtitle);
            final LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dp(2);
            texts.addView(sub, subParams);
        }
        final LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(20);
        row.addView(texts, textParams);

        if (!segmented) {
            row.setBackground(Theme.getSelectorDrawable(false));
        }
        row.setOnClickListener(onClick);
        LumineCards.add(card, row);
    }
}
