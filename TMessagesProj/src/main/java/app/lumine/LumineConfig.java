/*
 * luminegram - own settings storage.
 * Every feature of this fork keeps its toggle here, never in upstream classes.
 */
package app.lumine;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public final class LumineConfig {

    private static final String PREFS_NAME = "lumine_config";

    private static SharedPreferences prefs;

    private LumineConfig() {
    }

    private static synchronized SharedPreferences prefs() {
        if (prefs == null) {
            prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return prefs;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return prefs().getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        prefs().edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key, int defaultValue) {
        return prefs().getInt(key, defaultValue);
    }

    public static void setInt(String key, int value) {
        prefs().edit().putInt(key, value).apply();
    }

    public static String getString(String key, String defaultValue) {
        return prefs().getString(key, defaultValue);
    }

    public static void setString(String key, String value) {
        prefs().edit().putString(key, value).apply();
    }
}
