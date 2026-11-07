/*
 * luminegram - string lookup for our own resources.
 * LocaleController resolves ids through a generated table that only reads
 * values/strings.xml, so strings of ours are read straight from resources.
 */
package app.lumine;

import androidx.annotation.StringRes;

import org.telegram.messenger.ApplicationLoader;

public final class LumineStrings {

    private LumineStrings() {
    }

    public static String get(@StringRes int res) {
        return ApplicationLoader.applicationContext.getString(res);
    }
}
