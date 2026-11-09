/*
 * luminegram - Drawer page.
 * Options of the side drawer: the master switch, the immersive animation and,
 * in later steps, the layout of its items.
 */
package app.lumine.settings;

import android.content.Context;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.drawer.LumineDrawer;
import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

public class LumineDrawerActivity extends LumineSubPage {

    @Override
    protected int titleRes() {
        return R.string.LumineDrawerTitle;
    }

    @Override
    protected void build(Context context, LinearLayout list) {
        final LinearLayout main = LumineCards.card(context, list, null);
        LumineCards.toggleRow(context, main, LumineStrings.get(R.string.LumineDrawerEnable), null, LumineDrawer.enabled(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineDrawer.setEnabled(enabled);
                if (LaunchActivity.instance != null) {
                    LaunchActivity.instance.syncDrawerContainerEnabled();
                }
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineDrawerEnableInfo));

        final LinearLayout animation = LumineCards.card(context, list, null);
        LumineCards.toggleRow(context, animation, LumineStrings.get(R.string.LumineDrawerImmersive), null, LumineDrawer.immersive(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineDrawer.setImmersive(enabled);
            }
        });
    }
}
