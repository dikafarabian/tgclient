/*
 * luminegram - Appearance tab of the luminegram settings.
 * Card "Material Design 3" with one toggle per global style. The toggles are
 * real switches and the slider is a real seek bar, so they show the style
 * that is currently selected.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import app.lumine.LumineStrings;
import app.lumine.md3.LumineMd3;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

public class LumineAppearancePage extends ScrollView {

    private final Switch switchToggle;
    private final Switch sliderToggle;
    private final Switch listToggle;
    private final SeekBarView sliderPreview;

    public LumineAppearancePage(Context context) {
        super(context);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineMd3Title));

        switchToggle = toggleRow(context, card, LumineStrings.get(R.string.LumineMd3Switches), LumineMd3.switchEnabled(), enabled -> {
            LumineMd3.setSwitchEnabled(enabled);
            refresh();
        });
        sliderToggle = toggleRow(context, card, LumineStrings.get(R.string.LumineMd3Sliders), LumineMd3.sliderEnabled(), enabled -> {
            LumineMd3.setSliderEnabled(enabled);
            refresh();
        });

        listToggle = toggleRow(context, card, LumineStrings.get(R.string.LumineMd3ListItems), LumineMd3.listEnabled(), enabled -> {
            LumineMd3.setListEnabled(enabled);
            Theme.applyCommonTheme();
            refresh();
        });

        sliderPreview = new SeekBarView(context);
        sliderPreview.setDelegate((stop, progress) -> {
        });
        sliderPreview.setProgress(0.6f);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30));
        previewParams.topMargin = dp(4);
        previewParams.bottomMargin = dp(8);
        card.addView(sliderPreview, previewParams);

        LumineCards.info(context, list, LumineStrings.get(R.string.LumineMd3Info));
    }

    private interface OnToggle {
        void onToggle(boolean enabled);
    }

    private static Switch toggleRow(Context context, LinearLayout card, String label, boolean checked, OnToggle onToggle) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setBackground(Theme.getSelectorDrawable(false));

        TextView name = new TextView(context);
        name.setTextSize(16);
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        name.setText(label);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch toggle = new Switch(context);
        toggle.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        toggle.setChecked(checked, false);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(dp(37), dp(20));
        switchParams.leftMargin = dp(16);
        row.addView(toggle, switchParams);

        row.setOnClickListener((View v) -> {
            final boolean enabled = !toggle.isChecked();
            toggle.setChecked(enabled, true);
            onToggle.onToggle(enabled);
        });
        card.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return toggle;
    }

    private void refresh() {
        switchToggle.invalidate();
        sliderToggle.invalidate();
        listToggle.invalidate();
        sliderPreview.invalidate();
    }
}
