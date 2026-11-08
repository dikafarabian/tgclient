/*
 * luminegram - configurable bottom tabs.
 * One ordered list of tabs, each enabled or not. MainTabsActivity asks this
 * class for the number of pages, the page of a tab and the tab of a page.
 */
package app.lumine.tabs;

import android.text.TextUtils;

import app.lumine.LumineConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LumineTabs {

    public enum Type {
        CHATS,
        CONTACTS,
        SETTINGS,
        CALLS,
        PROFILE
    }

    public static final class State {
        public final Type type;
        public boolean enabled;

        public State(Type type, boolean enabled) {
            this.type = type;
            this.enabled = enabled;
        }
    }

    public interface Listener {
        void onTabsChanged();
    }

    public static final int DISPLAY_SHOW = 0;
    public static final int DISPLAY_HIDE = 1;
    public static final int DISPLAY_FLOATING = 2;

    private static final String KEY_ORDER = "tabs_order";
    private static final String KEY_TITLES = "tabs_titles";
    private static final String KEY_SEARCH = "tabs_search";
    private static final String KEY_DISPLAY = "tabs_display";
    private static final String KEY_FORCE_CHATS = "tabs_force_chats";
    private static final String DEFAULT_ORDER = "CHATS,CONTACTS,SETTINGS,!CALLS,PROFILE";
    private static final String CALLS_ORDER = "CHATS,CONTACTS,!SETTINGS,CALLS,PROFILE";

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private LumineTabs() {
    }

    public static void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public static boolean titlesVisible() {
        return LumineConfig.getBoolean(KEY_TITLES, true);
    }

    public static void setTitlesVisible(boolean visible) {
        LumineConfig.setBoolean(KEY_TITLES, visible);
        notifyChanged();
    }

    public static boolean searchButton() {
        return LumineConfig.getBoolean(KEY_SEARCH, false);
    }

    public static void setSearchButton(boolean enabled) {
        LumineConfig.setBoolean(KEY_SEARCH, enabled);
        notifyChanged();
    }

    public static int displayMode() {
        final int mode = LumineConfig.getInt(KEY_DISPLAY, DISPLAY_SHOW);
        return mode == DISPLAY_HIDE || mode == DISPLAY_FLOATING ? mode : DISPLAY_SHOW;
    }

    public static void setDisplayMode(int mode) {
        LumineConfig.setInt(KEY_DISPLAY, mode);
        notifyChanged();
    }

    public static boolean forceOpenChats() {
        return LumineConfig.getBoolean(KEY_FORCE_CHATS, false);
    }

    public static void setForceOpenChats(boolean enabled) {
        LumineConfig.setBoolean(KEY_FORCE_CHATS, enabled);
    }

    public static ArrayList<State> getAll() {
        String raw = LumineConfig.getString(KEY_ORDER, null);
        if (TextUtils.isEmpty(raw)) {
            raw = UserConfig.getInstance(UserConfig.selectedAccount).showCallsTab ? CALLS_ORDER : DEFAULT_ORDER;
        }
        final ArrayList<State> result = new ArrayList<>();
        final EnumSet<Type> missing = EnumSet.allOf(Type.class);
        for (String part : raw.split(",")) {
            final String name = part.trim();
            if (name.isEmpty()) {
                continue;
            }
            final boolean enabled = !name.startsWith("!");
            try {
                final Type type = Type.valueOf(enabled ? name : name.substring(1));
                if (missing.remove(type)) {
                    result.add(new State(type, enabled));
                }
            } catch (IllegalArgumentException ignore) {
            }
        }
        for (Type type : Type.values()) {
            if (missing.contains(type)) {
                result.add(new State(type, type != Type.CALLS));
            }
        }
        normalize(result);
        return result;
    }

    public static ArrayList<Type> getEnabled() {
        final ArrayList<Type> result = new ArrayList<>();
        for (State state : getAll()) {
            if (state.enabled) {
                result.add(state.type);
            }
        }
        return result;
    }

    public static int count() {
        return getEnabled().size();
    }

    public static int position(Type type) {
        return getEnabled().indexOf(type);
    }

    public static Type typeAt(int position) {
        final ArrayList<Type> enabled = getEnabled();
        if (position < 0 || position >= enabled.size()) {
            return Type.CHATS;
        }
        return enabled.get(position);
    }

    public static boolean isEnabled(Type type) {
        return position(type) >= 0;
    }

    public static ArrayList<State> copy(List<State> source) {
        final ArrayList<State> result = new ArrayList<>();
        for (State state : source) {
            result.add(new State(state.type, state.enabled));
        }
        return result;
    }

    public static boolean save(List<State> states) {
        final ArrayList<State> copy = copy(states);
        normalize(copy);
        final String old = serialize(getAll());
        final String value = serialize(copy);
        if (TextUtils.equals(old, value)) {
            return false;
        }
        LumineConfig.setString(KEY_ORDER, value);
        mirrorCallsTab(copy);
        notifyChanged();
        return true;
    }

    public static void applyCallsTabToggle(boolean show) {
        final ArrayList<State> states = getAll();
        for (State state : states) {
            if (state.type == Type.CALLS) {
                state.enabled = show;
            } else if (state.type == Type.SETTINGS) {
                state.enabled = !show;
            }
        }
        save(states);
    }

    private static void mirrorCallsTab(List<State> states) {
        boolean calls = false;
        for (State state : states) {
            if (state.type == Type.CALLS) {
                calls = state.enabled;
            }
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final UserConfig config = UserConfig.getInstance(a);
            if (config.isClientActivated()) {
                config.setShowCallsTab(calls);
            }
        }
    }

    private static void normalize(List<State> states) {
        boolean any = false;
        for (State state : states) {
            if (state.type == Type.CHATS) {
                state.enabled = true;
            }
            any |= state.enabled;
        }
        if (!any && !states.isEmpty()) {
            states.get(0).enabled = true;
        }
    }

    private static String serialize(List<State> states) {
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            final State state = states.get(i);
            if (i > 0) {
                out.append(',');
            }
            if (!state.enabled) {
                out.append('!');
            }
            out.append(state.type.name());
        }
        return out.toString();
    }

    private static void notifyChanged() {
        AndroidUtilities.runOnUIThread(() -> {
            for (Listener listener : listeners) {
                listener.onTabsChanged();
            }
        });
    }
}
