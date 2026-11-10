/*
 * luminegram - transparent helper that asks the system for one image and hands a cache copy back.
 * Lets an overlay that is not a fragment pick a file without touching stock activity code.
 */
package app.lumine.icons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

import java.io.File;
import java.io.InputStream;

public class LumineIconPickActivity extends Activity {

    public interface Listener {
        void onPicked(@Nullable File file);
    }

    private static final int REQUEST_PICK = 1;
    private static Listener pending;

    public static void start(Context context, Listener listener) {
        pending = listener;
        Intent intent = new Intent(context, LumineIconPickActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null || pending == null) {
            finish();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_PICK);
        } catch (Exception e) {
            FileLog.e(e);
            deliver(null);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri uri = requestCode == REQUEST_PICK && resultCode == RESULT_OK && data != null ? data.getData() : null;
        if (uri == null) {
            deliver(null);
            finish();
            return;
        }
        final Listener listener = pending;
        pending = null;
        Utilities.globalQueue.postRunnable(() -> {
            File temp = new File(ApplicationLoader.applicationContext.getCacheDir(), "lumine_icon_pick_" + System.currentTimeMillis());
            File result = null;
            try (InputStream in = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri)) {
                if (in != null && AndroidUtilities.copyFile(in, temp)) {
                    result = temp;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            final File picked = result;
            AndroidUtilities.runOnUIThread(() -> {
                if (listener != null) {
                    listener.onPicked(picked);
                } else if (picked != null) {
                    picked.delete();
                }
            });
        });
        finish();
    }

    private static void deliver(@Nullable File file) {
        Listener listener = pending;
        pending = null;
        if (listener != null) {
            listener.onPicked(file);
        }
    }
}
