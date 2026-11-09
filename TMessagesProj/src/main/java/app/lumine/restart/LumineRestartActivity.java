/*
 * luminegram - restart helper.
 * Runs in the :lumine_restart process and uses no Telegram classes: it stops the main process and starts the app again.
 */
package app.lumine.restart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import androidx.core.content.IntentCompat;

public class LumineRestartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent source = getIntent();
        final int pid = source.getIntExtra(LumineRestart.EXTRA_PID, -1);
        final Intent launch = IntentCompat.getParcelableExtra(source, LumineRestart.EXTRA_LAUNCH, Intent.class);
        if (pid > 0 && pid != Process.myPid()) {
            Process.killProcess(pid);
        }
        if (launch != null) {
            startActivity(launch);
        }
        finish();
        Runtime.getRuntime().exit(0);
    }
}
