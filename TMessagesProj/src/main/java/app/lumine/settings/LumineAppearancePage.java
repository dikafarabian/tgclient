/*
 * luminegram - Appearance tab of the luminegram settings.
 * Card "Material Design 3" with one toggle per global style. The toggles are
 * real switches and the slider is a real seek bar, so they show the style
 * that is currently selected.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.md3.LumineMd3;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

public class LumineAppearancePage extends ScrollView {

    private final Switch switchToggle;
    private final Switch sliderToggle;
    private final Switch listToggle;
    private final SeekBarView sliderPreview;

    public LumineAppearancePage(Context context, BaseFragment fragment) {
        super(context);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout interfaceCard = LumineCards.card(context, list, LumineStrings.get(R.string.LumineInterface));
        LumineIconRow.add(context, interfaceCard, R.drawable.lumine_drawer, LumineStrings.get(R.string.LumineDrawerTitle), null, v -> fragment.presentFragment(new LumineDrawerActivity()));
        LumineIconRow.add(context, interfaceCard, R.drawable.tabs_reorder, LumineStrings.get(R.string.LumineTabsTitle), null, v -> fragment.presentFragment(new LumineTabsActivity()));

        LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineMd3Title));

        switchToggle = LumineCards.toggleRow(context, card, LumineStrings.get(R.string.LumineMd3Switches), null, LumineMd3.switchEnabled(), enabled -> {
            LumineMd3.setSwitchEnabled(enabled);
            refresh();
        });
        sliderToggle = LumineCards.toggleRow(context, card, LumineStrings.get(R.string.LumineMd3Sliders), null, LumineMd3.sliderEnabled(), enabled -> {
            LumineMd3.setSliderEnabled(enabled);
            refresh();
        });

        listToggle = LumineCards.toggleRow(context, card, LumineStrings.get(R.string.LumineMd3ListItems), null, LumineMd3.listEnabled(), enabled -> {
            LumineMd3.setListEnabled(enabled);
            Theme.applyCommonTheme();
            refresh();
        });

        sliderPreview = new SeekBarView(context);
        sliderPreview.setDelegate((stop, progress) -> {
        });
        sliderPreview.setProgress(0.6f);
        if (LumineCards.segmented()) {
            FrameLayout wrap = new FrameLayout(context);
            wrap.setPadding(dp(16), dp(12), dp(16), dp(12));
            wrap.addView(sliderPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(30)));
            LumineCards.add(card, wrap);
        } else {
            LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30));
            previewParams.topMargin = dp(4);
            previewParams.bottomMargin = dp(8);
            card.addView(sliderPreview, previewParams);
        }

        LumineCards.info(context, list, LumineStrings.get(R.string.LumineMd3Info));
    }

    private void refresh() {
        switchToggle.invalidate();
        sliderToggle.invalidate();
        listToggle.invalidate();
        sliderPreview.invalidate();
    }
}
