/*
 * luminegram - built-in icon sets (Solar and Remix).
 * Reads assets/lumine/icon_sets.txt (set letter, stock icon name, set drawable name) and answers
 * with the resource id that replaces a stock icon. Names are resolved at run time, so an entry
 * whose drawable is missing is skipped instead of breaking the build.
 * Based on exteraless/exteraless (app.exteraless.icons.BaseIconPacks), rewritten. See docs/ICON_SETS_NOTICE.md.
 */
package app.lumine.icons;

import android.content.res.Resources;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LumineIconBase {

    public static final int DEFAULT = 0;
    public static final int SOLAR = 1;
    public static final int REMIX = 2;

    private static final Object lock = new Object();
    private static volatile Map<String, String> solar;
    private static volatile Map<String, String> remix;
    private static final ConcurrentHashMap<Integer, Integer> mapped = new ConcurrentHashMap<>();

    private LumineIconBase() {
    }

    public static void invalidate() {
        mapped.clear();
    }

    public static int mapped(int resId) {
        int set = LumineIconConfig.baseSet();
        if (set == DEFAULT || resId == 0) {
            return 0;
        }
        Integer cached = mapped.get(resId);
        if (cached != null) {
            return cached;
        }
        int result = 0;
        try {
            String name = LumineIconManager.get().resourceName(resId);
            Map<String, String> table = table(set);
            String target = name == null || table == null ? null : table.get(name);
            if (target != null) {
                Resources app = ApplicationLoader.applicationContext.getResources();
                result = app.getIdentifier(target, "drawable", ApplicationLoader.applicationContext.getPackageName());
            }
        } catch (Throwable t) {
            FileLog.e("lumine icons: built-in set lookup failed", t);
        }
        mapped.put(resId, result);
        return result;
    }

    private static Map<String, String> table(int set) {
        if (solar == null || remix == null) {
            load();
        }
        return set == SOLAR ? solar : set == REMIX ? remix : null;
    }

    private static void load() {
        synchronized (lock) {
            if (solar != null && remix != null) {
                return;
            }
            Map<String, String> s = new HashMap<>();
            Map<String, String> r = new HashMap<>();
            try (InputStream in = ApplicationLoader.applicationContext.getAssets().open("lumine/icon_sets.txt");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (parts.length != 3) {
                        continue;
                    }
                    if ("s".equals(parts[0])) {
                        s.put(parts[1], parts[2]);
                    } else if ("r".equals(parts[0])) {
                        r.put(parts[1], parts[2]);
                    }
                }
            } catch (Throwable t) {
                FileLog.e("lumine icons: cannot read built-in sets", t);
            }
            solar = s;
            remix = r;
        }
    }
}
