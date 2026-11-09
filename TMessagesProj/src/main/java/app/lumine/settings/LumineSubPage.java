/*
 * luminegram - base class of the luminegram sub pages.
 * Owns the glass header, the scroll container and the restart menu. A page
 * names its title and fills the list in build(); rebuild() clears and fills it
 * again when an option changes the rows that follow it.
 */
package app.lumine.settings;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import app.lumine.LumineStrings;
import app.lumine.restart.LumineRestart;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SectionsScrollView;

public abstract class LumineSubPage extends BaseFragment {

    private LinearLayout list;
    private SectionsScrollView scroll;

    protected abstract int titleRes();

    protected abstract void build(Context context, LinearLayout list);

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
        simpleActionBar.setTitle(LumineStrings.get(titleRes()));
        LumineRestart.menu(this);

        final FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        list = new SectionsScrollView.SectionsLinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll = new SectionsScrollView(context, list, resourceProvider);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        scroll.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        build(context, list);
        return fragmentView;
    }

    protected final void rebuild(long delayMs) {
        AndroidUtilities.runOnUIThread(() -> {
            if (list == null) {
                return;
            }
            list.removeAllViews();
            build(list.getContext(), list);
        }, delayMs);
    }
}
