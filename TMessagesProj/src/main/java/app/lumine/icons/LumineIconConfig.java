/*
 * luminegram - icon pack settings.
 * Keys live in LumineConfig. The master switch is off by default, so an untouched app uses stock icons.
 */
package app.lumine.icons;

import app.lumine.LumineConfig;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public final class LumineIconConfig {

    private static final String KEY_ENABLED = "LumineIconPacksEnabled";
    private static final String KEY_ACTIVE = "LumineIconPacksActive";
    private static final String KEY_NOTIFICATION = "LumineIconPacksNotification";

    private static volatile Boolean enabledCache;
    private static volatile String editingPackId;

    private LumineIconConfig() {
    }

    public static boolean enabled() {
        Boolean cached = enabledCache;
        if (cached == null) {
            cached = LumineConfig.getBoolean(KEY_ENABLED, false);
            enabledCache = cached;
        }
        return cached;
    }

    public static void setEnabled(boolean value) {
        enabledCache = value;
        LumineConfig.setBoolean(KEY_ENABLED, value);
    }

    public static boolean customNotificationIcon() {
        return LumineConfig.getBoolean(KEY_NOTIFICATION, false);
    }

    public static void setCustomNotificationIcon(boolean value) {
        LumineConfig.setBoolean(KEY_NOTIFICATION, value);
    }

    public static List<String> activePackIds() {
        List<String> ids = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(LumineConfig.getString(KEY_ACTIVE, "[]"));
            for (int i = 0; i < array.length(); i++) {
                String id = array.optString(i, null);
                if (id != null && !id.isEmpty() && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        } catch (Throwable ignore) {
        }
        return ids;
    }

    public static void setActivePackIds(List<String> ids) {
        JSONArray array = new JSONArray();
        for (String id : ids) {
            array.put(id);
        }
        LumineConfig.setString(KEY_ACTIVE, array.toString());
    }

    public static void forgetPack(String packId) {
        List<String> ids = activePackIds();
        if (ids.remove(packId)) {
            setActivePackIds(ids);
        }
    }

    public static boolean isPackActive(String packId) {
        return activePackIds().contains(packId);
    }

    public static boolean togglePack(String packId) {
        List<String> ids = activePackIds();
        boolean active;
        if (ids.remove(packId)) {
            active = false;
        } else {
            ids.add(0, packId);
            active = true;
        }
        setActivePackIds(ids);
        return active;
    }

    public static void movePack(String packId, int delta) {
        List<String> ids = activePackIds();
        int index = ids.indexOf(packId);
        int target = index + delta;
        if (index < 0 || target < 0 || target >= ids.size()) {
            return;
        }
        ids.remove(index);
        ids.add(target, packId);
        setActivePackIds(ids);
    }

    public static String editingPackId() {
        return editingPackId;
    }

    public static void setEditingPackId(String packId) {
        editingPackId = packId;
    }
}
