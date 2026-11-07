/*
 * luminegram - settings screen of luminegram.
 * Content on top, bottom navigation bar with the tabs.
 */
package app.lumine.settings;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

public class LumineSettingsActivity extends BaseFragment {

    private static final int TAB_HOME = 0;
    private static final int TAB_COUNT = 5;

    private FrameLayout content;
    private LumineTabBar tabBar;
    private LumineHomePage homePage;
    private final View[] pages = new View[TAB_COUNT];
    private int current = -1;
    private boolean resumed;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.LumineSettings));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        content = new FrameLayout(context);
        root.addView(content, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        tabBar = new LumineTabBar(
                context,
                new int[]{
                        R.drawable.lumine_tab_home,
                        R.drawable.lumine_tab_general,
                        R.drawable.lumine_tab_appearance,
                        R.drawable.lumine_tab_stealth,
                        R.drawable.lumine_tab_experimental
                },
                new String[]{
                        LocaleController.getString(R.string.LumineTabHome),
                        LocaleController.getString(R.string.LumineTabGeneral),
                        LocaleController.getString(R.string.LumineTabAppearance),
                        LocaleController.getString(R.string.LumineTabStealth),
                        LocaleController.getString(R.string.LumineTabExperimental)
                }
        );
        tabBar.setListener(this::showPage);
        root.addView(tabBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tabBar.select(TAB_HOME, false);
        showPage(TAB_HOME);
        return fragmentView;
    }

    private View createPage(Context context, int index) {
        if (index == TAB_HOME) {
            homePage = new LumineHomePage(context, currentAccount);
            return homePage;
        }
        return new LumineComingSoonPage(context);
    }

    private void showPage(int index) {
        if (current == index || content == null) {
            return;
        }
        if (current == TAB_HOME && homePage != null) {
            homePage.stop();
        }
        current = index;
        if (pages[index] == null) {
            pages[index] = createPage(content.getContext(), index);
        }
        content.removeAllViews();
        content.addView(pages[index], new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        if (index == TAB_HOME && homePage != null && resumed) {
            homePage.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        if (current == TAB_HOME && homePage != null) {
            homePage.start();
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
