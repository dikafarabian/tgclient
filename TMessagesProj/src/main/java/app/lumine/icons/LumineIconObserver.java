/*
 * luminegram - remembers which icons each screen asked for while a pack is being edited.
 * Based on exteraless/exteraless (app.exteraless.icons.picker.IconObserver), rewritten.
 */
package app.lumine.icons;

import android.app.Activity;
import android.os.Looper;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.LaunchActivity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;

public final class LumineIconObserver {

    private static final WeakHashMap<BaseFragment, Set<Integer>> sources = new WeakHashMap<>();
    private static volatile boolean suspended;

    private LumineIconObserver() {
    }

    public static void setSuspended(boolean value) {
        suspended = value;
    }

    public static void log(int resId) {
        if (suspended || resId == 0 || LumineIconConfig.editingPackId() == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return;
        }
        BaseFragment fragment = LaunchActivity.getSafeLastFragment();
        if (fragment == null) {
            return;
        }
        Activity activity = fragment.getParentActivity();
        if (activity instanceof LaunchActivity) {
            LumineIconPicker.show((LaunchActivity) activity);
        }
        synchronized (sources) {
            Set<Integer> set = sources.get(fragment);
            if (set == null) {
                set = new LinkedHashSet<>();
                sources.put(fragment, set);
            }
            set.add(resId);
        }
    }

    public static Set<Integer> used() {
        BaseFragment fragment = LaunchActivity.getSafeLastFragment();
        if (fragment == null) {
            return Collections.emptySet();
        }
        synchronized (sources) {
            Set<Integer> set = sources.get(fragment);
            return set == null ? Collections.emptySet() : new LinkedHashSet<>(set);
        }
    }

    public static void clear() {
        synchronized (sources) {
            sources.clear();
        }
    }
}
