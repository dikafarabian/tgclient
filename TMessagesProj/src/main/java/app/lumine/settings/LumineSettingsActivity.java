/*
 * luminegram - settings screen of luminegram.
 * One glass header with a title that follows the tab, the page of the selected tab in a single scroll view,
 * and the floating tab bar over the bottom.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import app.lumine.LumineStrings;
import app.lumine.restart.LumineRestart;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SectionsScrollView;

public class LumineSettingsActivity extends BaseFragment {

    private static final int TAB_HOME = 0;
    private static final int TAB_GENERAL = 1;
    private static final int TAB_APPEARANCE = 2;
    private static final int TAB_STEALTH = 3;
    private static final int TAB_COUNT = 5;
    private static final int TAB_BAR_DP = 72;

    private SectionsScrollView scroll;
    private LinearLayout content;
    private LumineTabBar tabBar;
    private LumineHomePage homePage;
    private LumineGeneralPage generalPage;
    private LumineStealthPage stealthPage;
    private String[] titles;
    private final View[] pages = new View[TAB_COUNT];
    private final int[] scrollPositions = new int[TAB_COUNT];
    private int current = -1;
    private boolean resumed;

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
        LumineRestart.menu(this);

        titles = new String[]{
                LumineStrings.get(R.string.LumineTabHome),
                LumineStrings.get(R.string.LumineTabGeneral),
                LumineStrings.get(R.string.LumineTabAppearance),
                LumineStrings.get(R.string.LumineTabStealth),
                LumineStrings.get(R.string.LumineTabExperimental)
        };

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        content = new SectionsScrollView.SectionsLinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll = new SectionsScrollView(context, content, resourceProvider);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        scroll.addView(content, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop(), content.getPaddingRight(), dp(12 + TAB_BAR_DP));

        tabBar = new LumineTabBar(
                context,
                new int[]{
                        R.drawable.lumine_tab_home,
                        R.drawable.lumine_tab_general,
                        R.drawable.lumine_tab_appearance,
                        R.drawable.lumine_tab_stealth,
                        R.drawable.lumine_tab_experimental
                },
                new int[]{
                        R.drawable.lumine_tab_home_outline,
                        R.drawable.lumine_tab_general_outline,
                        R.drawable.lumine_tab_appearance_outline,
                        R.drawable.lumine_tab_stealth_outline,
                        R.drawable.lumine_tab_experimental_outline
                },
                titles
        );
        tabBar.setListener(this::showPage);
        root.addView(tabBar, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(TAB_BAR_DP), Gravity.BOTTOM));
        applyBottomInset();

        tabBar.select(TAB_HOME, false);
        showPage(TAB_HOME);
        return fragmentView;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        super.onInsets(left, top, right, bottom);
        applyBottomInset();
    }

    private void applyBottomInset() {
        if (tabBar == null) {
            return;
        }
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tabBar.getLayoutParams();
        if (params.bottomMargin != mSystemInsets.bottom) {
            params.bottomMargin = mSystemInsets.bottom;
            tabBar.setLayoutParams(params);
        }
    }

    private View createPage(Context context, int index) {
        if (index == TAB_HOME) {
            homePage = new LumineHomePage(context, currentAccount, () -> presentFragment(new LumineNotificationActivity()), () -> presentFragment(new LumineGhostActivity()));
            return homePage;
        }
        if (index == TAB_GENERAL) {
            generalPage = new LumineGeneralPage(context, this);
            return generalPage;
        }
        if (index == TAB_APPEARANCE) {
            return new LumineAppearancePage(context, this);
        }
        if (index == TAB_STEALTH) {
            stealthPage = new LumineStealthPage(context, this);
            return stealthPage;
        }
        return new LumineComingSoonPage(context);
    }

    private void showPage(int index) {
        if (current == index || content == null) {
            return;
        }
        if (current >= 0) {
            scrollPositions[current] = scroll.getScrollY();
        }
        if (current == TAB_HOME && homePage != null) {
            homePage.stop();
        }
        current = index;
        if (pages[index] == null) {
            pages[index] = createPage(content.getContext(), index);
        }
        simpleActionBar.setTitle(titles[index]);
        content.removeAllViews();
        content.addView(pages[index], new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        final int position = scrollPositions[index];
        scroll.scrollTo(0, 0);
        scroll.post(() -> scroll.scrollTo(0, position));
        if (index == TAB_HOME && homePage != null && resumed) {
            homePage.start();
        }
        if (index == TAB_GENERAL && generalPage != null) {
            generalPage.refresh();
        }
        if (index == TAB_STEALTH && stealthPage != null) {
            stealthPage.refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        if (current == TAB_HOME && homePage != null) {
            homePage.start();
        }
        if (current == TAB_GENERAL && generalPage != null) {
            generalPage.refresh();
        }
        if (current == TAB_STEALTH && stealthPage != null) {
            stealthPage.refresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
        if (homePage != null) {
            homePage.stop();
        }
    }

    @Override
    public void onFragmentDestroy() {
        if (homePage != null) {
            homePage.stop();
        }
        super.onFragmentDestroy();
    }
}
