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

import app.lumine.md3.LumineListItems;
import app.lumine.push.PushStatus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.Switch;

final class LumineCards {

    static final class Row {
        final LinearLayout view;
        final TextView value;

        Row(LinearLayout view, TextView value) {
            this.view = view;
            this.value = value;
        }
    }

    private static final class Mark {
        @Override
        public boolean equals(Object other) {
            return other instanceof Integer && (Integer) other == RecyclerListView.TAG_NOT_SECTION;
        }

        @Override
        public int hashCode() {
            return RecyclerListView.TAG_NOT_SECTION;
        }
    }

    private static final Object SEGMENT = new Mark();
    private static final Object PLAIN = new Mark();
    private static final int OUTER_RADIUS_DP = 16;
    private static final int INNER_RADIUS_DP = 4;
    private static final int SEGMENT_GAP_DP = 2;
    private static final int SEGMENT_HEIGHT_DP = 56;
    private static final int SECTION_GAP_DP = 12;
    private static final int INFO_TOP_DP = 6;
    private static final int INFO_BOTTOM_DP = 8;

    private LumineCards() {
    }

    static void plain(View view) {
        view.setTag(PLAIN);
    }

    static boolean segmented() {
        return LumineListItems.enabled();
    }

    static void add(LinearLayout card, View view) {
        if (!segmented()) {
            view.setTag(PLAIN);
            card.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            return;
        }
        int count = 0;
        for (int i = 0; i < card.getChildCount(); i++) {
            if (card.getChildAt(i).getTag() == SEGMENT) {
                count++;
            }
        }
        view.setTag(SEGMENT);
        view.setMinimumHeight(dp(SEGMENT_HEIGHT_DP));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = count == 0 ? 0 : dp(SEGMENT_GAP_DP);
        card.addView(view, params);
        restyle(card);
    }

    private static void restyle(LinearLayout card) {
        int total = 0;
        for (int i = 0; i < card.getChildCount(); i++) {
            if (card.getChildAt(i).getTag() == SEGMENT) {
                total++;
            }
        }
        int index = 0;
        for (int i = 0; i < card.getChildCount(); i++) {
            final View view = card.getChildAt(i);
            if (view.getTag() != SEGMENT) {
                continue;
            }
            final int topDp = index == 0 ? OUTER_RADIUS_DP : INNER_RADIUS_DP;
            final int bottomDp = index == total - 1 ? OUTER_RADIUS_DP : INNER_RADIUS_DP;
            view.setBackground(Theme.createRoundRectDrawable(dp(topDp), dp(topDp), dp(bottomDp), dp(bottomDp), Theme.getColor(Theme.key_windowBackgroundWhite)));
            if (view.hasOnClickListeners()) {
                view.setForeground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector), topDp, topDp, bottomDp, bottomDp));
            }
            index++;
        }
    }

    static LinearLayout card(Context context, LinearLayout parent, String title) {
        final boolean segmented = segmented();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        if (!segmented) {
            card.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite)));
            card.setPadding(dp(16), title == null ? dp(4) : dp(14), dp(16), title == null ? dp(4) : dp(10));
        }

        if (title != null) {
            TextView header = new TextView(context);
            header.setTextSize(14);
            header.setTypeface(AndroidUtilities.bold());
            header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            header.setText(title);
            header.setTag(PLAIN);
            if (segmented) {
                header.setPadding(dp(16), dp(4), dp(16), dp(8));
            }
            card.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = parent.getChildCount() == 0 ? 0 : dp(SECTION_GAP_DP);
        parent.addView(card, params);
        return card;
    }

    static Row row(Context context, LinearLayout card, String label, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        final boolean segmented = segmented();
        row.setPadding(segmented ? dp(16) : 0, dp(10), segmented ? dp(16) : 0, dp(10));

        TextView name = new TextView(context);
        name.setTextSize(segmented ? 16 : 14);
        name.setTextColor(Theme.getColor(segmented ? Theme.key_windowBackgroundWhiteBlackText : Theme.key_windowBackgroundWhiteGrayText));
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
            if (!segmented) {
                row.setBackground(Theme.getSelectorDrawable(false));
            }
            row.setOnClickListener(onClick);
        }
        add(card, row);
        return new Row(row, value);
    }

    interface OnToggle {
        void onToggle(boolean enabled);
    }

    static Switch toggleRow(Context context, LinearLayout card, String label, CharSequence subtitle, boolean checked, OnToggle onToggle) {
        final boolean segmented = segmented();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(segmented ? dp(16) : 0, dp(12), segmented ? dp(16) : 0, dp(12));
        if (!segmented) {
            row.setBackground(Theme.getSelectorDrawable(false));
        }

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(context);
        name.setTextSize(16);
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        name.setText(label);
        texts.addView(name, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (!TextUtils.isEmpty(subtitle)) {
            TextView sub = new TextView(context);
            sub.setTextSize(13);
            sub.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            sub.setText(subtitle);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dp(2);
            texts.addView(sub, subParams);
        }
        row.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch toggle = new Switch(context);
        toggle.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        toggle.setChecked(checked, false);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(dp(37), dp(20));
        switchParams.leftMargin = dp(16);
        row.addView(toggle, switchParams);

        row.setOnClickListener(v -> {
            final boolean enabled = !toggle.isChecked();
            toggle.setChecked(enabled, true);
            onToggle.onToggle(enabled);
        });
        add(card, row);
        return toggle;
    }

    static void setValue(TextView view, CharSequence text, int level) {
        int key = segmented() ? Theme.key_windowBackgroundWhiteGrayText : Theme.key_windowBackgroundWhiteBlackText;
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
        info.setTag(PLAIN);
        info.setPadding(dp(8), 0, dp(8), dp(INFO_BOTTOM_DP));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(INFO_TOP_DP);
        parent.addView(info, params);
    }
}
