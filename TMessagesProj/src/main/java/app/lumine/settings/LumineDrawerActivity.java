/*
 * luminegram - Drawer page.
 * Options of the side drawer: the master switch, the immersive animation and,
 * in later steps, the layout of its items.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import app.lumine.LumineStrings;
import app.lumine.restart.LumineRestart;
import app.lumine.drawer.LumineDrawer;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SectionsScrollView;
import org.telegram.ui.LaunchActivity;

public class LumineDrawerActivity extends BaseFragment {

    private SectionsScrollView scroll;

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Nullable
    @Override
    protected SectionsScrollView getScrollViewForSimpleGlass() {
        return scroll;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        simpleActionBar.setBackButtonImage(R.drawable.ic_ab_back);
        simpleActionBar.setTitle(LumineStrings.get(R.string.LumineDrawerTitle));
        LumineRestart.menu(this);

        final FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        final LinearLayout list = new SectionsScrollView.SectionsLinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll = new SectionsScrollView(context, list, resourceProvider);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
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
