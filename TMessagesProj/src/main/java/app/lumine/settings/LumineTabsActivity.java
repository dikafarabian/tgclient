/*
 * luminegram - Customize Bottom Tabs page.
 * A preview of the bottom bar: tap a tab to show or hide it, long-press and drag to reorder.
 * The result is saved when the page closes.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import app.lumine.LumineStrings;
import app.lumine.tabs.LumineTabs;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.glass.GlassTabView;
import org.telegram.ui.MainTabsLayout;

import java.util.ArrayList;

public class LumineTabsActivity extends BaseFragment {

    private ArrayList<LumineTabs.State> states = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        states = LumineTabs.getAll();
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LumineStrings.get(R.string.LumineTabsTitle));
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

        final LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineTabsTitle));

        final FrameLayout host = new FrameLayout(context);
        host.setPadding(dp(8), dp(8), dp(8), dp(8));
        final int base = Theme.getColor(Theme.key_switchTrack);
        final GradientDrawable pill = new GradientDrawable();
        pill.setColor(Theme.multAlpha(base, 0.12f));
        pill.setStroke(Math.max(2, dp(1)), Theme.multAlpha(base, 0.25f));
        pill.setCornerRadius(dp(32));
        final FrameLayout pillHost = new FrameLayout(context);
        pillHost.setBackground(pill);
        host.addView(pillHost, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(64)));

        final Preview preview = new Preview(context);
        preview.bind();
        pillHost.addView(preview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        LumineCards.add(card, host);

        LumineCards.info(context, list, LumineStrings.get(R.string.LumineTabsInfo));
        return fragmentView;
    }

    @Override
    public void onFragmentDestroy() {
        LumineTabs.save(states);
        super.onFragmentDestroy();
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
            setPadding(dp(12), dp(6), dp(12), dp(6));
            setMaxWidth(dp(82 * LumineTabs.Type.values().length + 24));
        }

        void bind() {
            removeAllViews();
            for (int i = 0; i < states.size(); i++) {
                final int index = i;
                final LumineTabs.State state = states.get(i);
                final GlassTabView tab = createTab(getContext(), state.type);
                tab.setSelected(false, false);
                tab.setGestureSelectedOverride(0, false);
                tab.setTag(state);
                tab.setAlpha(state.enabled ? 1f : 0.4f);
                addTabToIgnoreClick(tab);
                tab.setOnClickListener(v -> toggle(index));
                tab.setOnLongClickListener(v -> startDrag(index, v));
                addView(tab);
                setViewVisible(tab, true, false);
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
            final float enabledAlpha = tag instanceof LumineTabs.State && !((LumineTabs.State) tag).enabled ? 0.4f : 1f;
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
            final int count = getChildCount();
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
