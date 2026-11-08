/*
 * luminegram - Drawer page.
 * Options of the side drawer: the master switch, the immersive animation and,
 * in later steps, the layout of its items.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.drawer.LumineDrawer;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

public class LumineDrawerActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LumineStrings.get(R.string.LumineDrawerTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        final ScrollView scroll = new ScrollView(context);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        root.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        final LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        scroll.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

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
        return fragmentView;
    }
}
