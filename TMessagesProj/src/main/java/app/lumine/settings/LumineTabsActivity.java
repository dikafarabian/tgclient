/*
 * luminegram - Customize Bottom Tabs page.
 * A preview of the bottom bar: tap a tab to show or hide it, long-press and drag to reorder.
 * The result is saved when the page closes.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.ClipData;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import app.lumine.LumineStrings;
import app.lumine.tabs.LumineTabs;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SectionsScrollView;
import org.telegram.ui.Components.glass.GlassTabView;
import org.telegram.ui.MainTabsLayout;


import java.util.ArrayList;

public class LumineTabsActivity extends BaseFragment {

    private ArrayList<LumineTabs.State> states = new ArrayList<>();
    private Preview preview;
    private TextView titlesValue;
    private TextView displayValue;

    @Override
    public boolean onFragmentCreate() {
        states = LumineTabs.getAll();
        return super.onFragmentCreate();
    }

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
        simpleActionBar.setTitle(LumineStrings.get(R.string.LumineTabsTitle));
        simpleActionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        final FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        final LinearLayout list = new SectionsScrollView.SectionsLinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll = new SectionsScrollView(context, list, resourceProvider);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        scroll.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        final LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineTabsTitle));

        final FrameLayout host = new FrameLayout(context);
        host.setPadding(dp(8), dp(8), dp(8), dp(8));
        final int base = Theme.getColor(Theme.key_switchTrack);

        final LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.HORIZONTAL);
        host.addView(group, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(56)));

        final FrameLayout barHost = new FrameLayout(context);
        barHost.setBackground(glass(base));
        preview = new Preview(context);
        preview.bind();
        barHost.addView(preview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        group.addView(barHost, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        final FrameLayout searchCircle = new FrameLayout(context);
        searchCircle.setBackground(glass(base));
        searchCircle.setAlpha(LumineTabs.searchButton() ? 1f : 0.4f);
        final ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.outline_header_search);
        searchIcon.setPadding(dp(14), dp(14), dp(14), dp(14));
        searchIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_glass_tabUnselected), PorterDuff.Mode.SRC_IN));
        searchCircle.addView(searchIcon, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        searchCircle.setOnClickListener(v -> {
            final boolean enabled = !LumineTabs.searchButton();
            LumineTabs.setSearchButton(enabled);
            searchCircle.setAlpha(enabled ? 1f : 0.4f);
        });
        final LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        searchParams.leftMargin = dp(6);
        group.addView(searchCircle, searchParams);
        LumineCards.add(card, host);

        LumineCards.info(context, list, LumineStrings.get(R.string.LumineTabsInfo));

        final LinearLayout options = LumineCards.card(context, list, null);
        final LumineCards.Row titlesRow = LumineCards.row(context, options, LumineStrings.get(R.string.LumineTabTitles), v -> showTitlesDialog());
        titlesValue = titlesRow.value;
        final LumineCards.Row displayRow = LumineCards.row(context, options, LumineStrings.get(R.string.LumineTabDisplayMode), v -> showDisplayDialog());
        displayValue = displayRow.value;
        LumineCards.toggleRow(context, options, LumineStrings.get(R.string.LumineTabForceChats), null, LumineTabs.forceOpenChats(), LumineTabs::setForceOpenChats);
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineTabForceChatsInfo));
        updateValues();
        return fragmentView;
    }

    private void updateValues() {
        LumineCards.setValue(titlesValue, LumineStrings.get(LumineTabs.titlesVisible() ? R.string.LumineTabShow : R.string.LumineTabHide), 0);
        LumineCards.setValue(displayValue, displayName(LumineTabs.displayMode()), 0);
    }

    private String displayName(int mode) {
        if (mode == LumineTabs.DISPLAY_HIDE) {
            return LumineStrings.get(R.string.LumineTabHide);
        } else if (mode == LumineTabs.DISPLAY_FLOATING) {
            return LumineStrings.get(R.string.LumineTabFloating);
        }
        return LumineStrings.get(R.string.LumineTabShow);
    }

    private void showTitlesDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final CharSequence[] names = {LumineStrings.get(R.string.LumineTabShow), LumineStrings.get(R.string.LumineTabHide)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LumineTabTitles));
        builder.setItems(names, (dialog, which) -> {
            LumineTabs.setTitlesVisible(which == 0);
            preview.refreshTitles();
            updateValues();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showDisplayDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final int[] modes = {LumineTabs.DISPLAY_SHOW, LumineTabs.DISPLAY_HIDE, LumineTabs.DISPLAY_FLOATING};
        final CharSequence[] names = new CharSequence[modes.length];
        for (int a = 0; a < modes.length; a++) {
            names[a] = displayName(modes[a]);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LumineTabDisplayMode));
        builder.setItems(names, (dialog, which) -> {
            LumineTabs.setDisplayMode(modes[which]);
            updateValues();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    public void onFragmentDestroy() {
        LumineTabs.save(states);
        super.onFragmentDestroy();
    }

    private static GradientDrawable glass(int base) {
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.multAlpha(base, 0.12f));
        drawable.setStroke(Math.max(2, dp(1)), Theme.multAlpha(base, 0.25f));
        drawable.setCornerRadius(dp(28));
        return drawable;
    }

    private GlassTabView createTab(Context context, LumineTabs.Type type) {
        switch (type) {
            case CONTACTS:
                return GlassTabView.createMainTab(context, getResourceProvider(), GlassTabView.TabAnimation.CONTACTS, R.string.MainTabsContacts);
            case SETTINGS:
                return GlassTabView.createMainTab(context, getResourceProvider(), GlassTabView.TabAnimation.SETTINGS, R.string.Settings);
            case CALLS:
                return GlassTabView.createMainTab(context, getResourceProvider(), GlassTabView.TabAnimation.CALLS, R.string.MainTabsCalls);
            case PROFILE:
                return GlassTabView.createAvatar(context, getResourceProvider(), currentAccount, R.string.MainTabsProfile);
            default:
                return GlassTabView.createMainTab(context, getResourceProvider(), GlassTabView.TabAnimation.CHATS, R.string.MainTabsChats);
        }
    }

    private final class Preview extends MainTabsLayout {

        private View dragging;

        Preview(Context context) {
            super(context, getResourceProvider());
            setPadding(dp(4), dp(4), dp(4), dp(4));
            setEqualWidth(!LumineTabs.titlesVisible());
        }

        void bind() {
            removeAllViews();
            for (int i = 0; i < states.size(); i++) {
                final int index = i;
                final LumineTabs.State state = states.get(i);
                final GlassTabView tab = createTab(getContext(), state.type);
                tab.setTitleVisible(LumineTabs.titlesVisible());
                tab.setSelected(false, false);
                tab.setGestureSelectedOverride(0, false);
                tab.setTag(state);
                tab.setAlpha(state.enabled ? 1f : 0.4f);
                addTabToIgnoreClick(tab);
                tab.setOnClickListener(v -> toggle(index));
                tab.setOnLongClickListener(v -> startDrag(index, v));
                addView(tab);
                setViewVisible(tab, true, false);
                if (state.type == LumineTabs.Type.CHATS) {
                    tab.setSelected(true, false);
                    setTabSelected(tab, false);
                }
            }
            requestLayout();
        }

        void refreshTitles() {
            setEqualWidth(!LumineTabs.titlesVisible());
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (child instanceof GlassTabView) {
                    ((GlassTabView) child).setTitleVisible(LumineTabs.titlesVisible());
                }
            }
            requestLayout();
        }

        private void toggle(int index) {
            final LumineTabs.State state = states.get(index);
            if (state.type == LumineTabs.Type.CHATS) {
                return;
            }
            if (state.enabled && enabledCount() <= 1) {
                return;
            }
            state.enabled = !state.enabled;
            final View tab = getChildAt(index);
            if (tab != null) {
                tab.setAlpha(state.enabled ? 1f : 0.4f);
            }
        }

        private int enabledCount() {
            int count = 0;
            for (LumineTabs.State state : states) {
                if (state.enabled) {
                    count++;
                }
            }
            return count;
        }

        @Override
        protected void setChildVisibilityFactor(View view, float factor) {
            final Object tag = view.getTag();
            final boolean off = tag instanceof LumineTabs.State ? !((LumineTabs.State) tag).enabled : tag instanceof Boolean && !((Boolean) tag);
            final float enabledAlpha = off ? 0.4f : 1f;
            final float scale = 0.7f + 0.3f * factor;
            view.setAlpha(factor * enabledAlpha);
            view.setScaleX(scale);
            view.setScaleY(scale);
        }

        private boolean startDrag(int index, View view) {
            dragging = view;
            final ClipData data = ClipData.newPlainText("tab_index", String.valueOf(index));
            final View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadow, null, 0);
            } else {
                view.startDrag(data, shadow, null, 0);
            }
            return true;
        }

        @Override
        public boolean onDragEvent(DragEvent event) {
            final int action = event.getAction();
            if (action == DragEvent.ACTION_DRAG_STARTED) {
                if (dragging != null) {
                    dragging.setVisibility(INVISIBLE);
                }
            } else if (action == DragEvent.ACTION_DROP) {
                try {
                    final int from = Integer.parseInt(String.valueOf(event.getClipData().getItemAt(0).getText()));
                    move(from, dropIndex(event.getX()));
                } catch (Exception ignore) {
                }
            } else if (action == DragEvent.ACTION_DRAG_ENDED) {
                if (dragging != null) {
                    dragging.setVisibility(VISIBLE);
                    dragging = null;
                }
            }
            return true;
        }

        private int dropIndex(float x) {
            final int count = states.size();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (x < child.getX() + child.getWidth()) {
                    return i;
                }
            }
            return Math.max(0, count - 1);
        }

        private void move(int from, int to) {
            if (from < 0 || from >= states.size() || to < 0 || to >= states.size() || from == to) {
                return;
            }
            states.add(to, states.remove(from));
            bind();
        }
    }
}
