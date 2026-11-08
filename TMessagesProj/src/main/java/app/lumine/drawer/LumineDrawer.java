/*
 * luminegram - side drawer settings.
 * Master switch, immersive animation and the item layout. The layout is one
 * string "visible;hidden" of comma separated item ids; an empty string means
 * the user never changed it and the default layout is used.
 */
package app.lumine.drawer;

import android.text.TextUtils;

import app.lumine.LumineConfig;
import app.lumine.tabs.LumineTabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LumineDrawer {

    public interface Listener {
        void onDrawerChanged();
    }

    private static final String KEY_ENABLED = "drawer_enabled";
    private static final String KEY_IMMERSIVE = "drawer_immersive";
    private static final String KEY_LAYOUT = "drawer_layout";
    private static final String SECTION_SEPARATOR = ";";
    private static final String ID_SEPARATOR = ",";

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private LumineDrawer() {
    }

    public static void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public static boolean enabled() {
        return LumineConfig.getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        LumineConfig.setBoolean(KEY_ENABLED, enabled);
        notifyChanged();
    }

    public static boolean immersive() {
        return LumineConfig.getBoolean(KEY_IMMERSIVE, false);
    }

    public static void setImmersive(boolean enabled) {
        LumineConfig.setBoolean(KEY_IMMERSIVE, enabled);
        notifyChanged();
    }

    public static boolean isCustomized() {
        return !TextUtils.isEmpty(LumineConfig.getString(KEY_LAYOUT, ""));
    }

    public static List<Integer> getDefaultLayout() {
        final boolean barHidden = bottomBarHidden();
        final ArrayList<Integer> layout = new ArrayList<>();
        layout.add(LumineDrawerItem.ARCHIVE.getId());
        if (barHidden) {
            layout.add(LumineDrawerItem.PROFILE.getId());
        }
        layout.add(LumineDrawerItem.NEW_GROUP.getId());
        if (barHidden) {
            layout.add(LumineDrawerItem.CONTACTS.getId());
        }
        layout.add(LumineDrawerItem.SAVED.getId());
        layout.add(LumineDrawerItem.BOTS.getId());
        if (barHidden) {
            layout.add(LumineDrawerItem.SETTINGS.getId());
        }
        return layout;
    }

    public static List<Integer> getLayout() {
        return load()[0];
    }

    public static List<Integer> getHiddenItems() {
        return load()[1];
    }

    public static ArrayList<Integer> getLayoutMutable() {
        return new ArrayList<>(getLayout());
    }

    public static ArrayList<Integer> getHiddenItemsMutable() {
        return new ArrayList<>(getHiddenItems());
    }

    public static List<Integer> getAllItemIds() {
        final ArrayList<Integer> ids = new ArrayList<>();
        for (LumineDrawerItem item : LumineDrawerItem.values()) {
            if (item != LumineDrawerItem.DIVIDER) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    public static void save(List<Integer> layout, List<Integer> hidden) {
        final ArrayList<Integer> visible = new ArrayList<>(layout);
        final ArrayList<Integer> hiddenCopy = new ArrayList<>(hidden);
        ensureSettingsVisible(visible, hiddenCopy, bottomBarHidden());
        LumineConfig.setString(KEY_LAYOUT, serialize(visible, hiddenCopy));
        notifyChanged();
    }

    public static void reset() {
        LumineConfig.setString(KEY_LAYOUT, "");
        notifyChanged();
    }

    static List<Integer>[] parse(String raw, List<Integer> defaults, boolean barHidden) {
        final ArrayList<Integer> visible = new ArrayList<>();
        final ArrayList<Integer> hidden = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) {
            visible.addAll(defaults);
        } else {
            final String[] sections = raw.split(SECTION_SEPARATOR, -1);
            readIds(sections.length > 0 ? sections[0] : "", visible, true);
            readIds(sections.length > 1 ? sections[1] : "", hidden, false);
        }
        for (Integer id : getAllItemIds()) {
            if (!visible.contains(id) && !hidden.contains(id)) {
                hidden.add(id);
            }
        }
        ensureSettingsVisible(visible, hidden, barHidden);
        @SuppressWarnings("unchecked") final List<Integer>[] result = new List[]{
                Collections.unmodifiableList(visible),
                Collections.unmodifiableList(hidden)
        };
        return result;
    }

    static String serialize(List<Integer> visible, List<Integer> hidden) {
        return join(visible) + SECTION_SEPARATOR + join(hidden);
    }

    private static List<Integer>[] load() {
        return parse(LumineConfig.getString(KEY_LAYOUT, ""), getDefaultLayout(), bottomBarHidden());
    }

    private static boolean bottomBarHidden() {
        return LumineTabs.displayMode() == LumineTabs.DISPLAY_HIDE;
    }

    private static void readIds(String section, ArrayList<Integer> out, boolean allowDivider) {
        if (TextUtils.isEmpty(section)) {
            return;
        }
        for (String part : section.split(ID_SEPARATOR)) {
            final String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            final int id;
            try {
                id = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                continue;
            }
            if (id == LumineDrawerItem.DIVIDER.getId()) {
                if (allowDivider) {
                    out.add(id);
                }
                continue;
            }
            if (LumineDrawerItem.getById(id) != null && !out.contains(id)) {
                out.add(id);
            }
        }
    }

    private static void ensureSettingsVisible(List<Integer> visible, List<Integer> hidden, boolean barHidden) {
        if (!barHidden) {
            return;
        }
        final Integer settings = LumineDrawerItem.SETTINGS.getId();
        if (visible.contains(settings)) {
            return;
        }
        hidden.remove(settings);
        visible.add(settings);
    }

    private static String join(List<Integer> ids) {
        final StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            if (sb.length() > 0) {
                sb.append(ID_SEPARATOR);
            }
            sb.append(id);
        }
        return sb.toString();
    }

    private static void notifyChanged() {
        for (Listener listener : listeners) {
            listener.onDrawerChanged();
        }
    }
}
