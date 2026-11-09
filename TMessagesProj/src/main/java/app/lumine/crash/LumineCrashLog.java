/*
 * luminegram - crash capture.
 * An uncaught exception is written to files/lumine_last_crash.txt and shown by LumineCrashActivity,
 * which runs in its own process so it still opens when the main process dies during startup.
 */
package app.lumine.crash;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;

import org.telegram.messenger.BuildVars;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class LumineCrashLog {

    static final String EXTRA_REPORT = "report";
    private static final String CRASH_PROCESS_SUFFIX = ":lumine_crash";
    private static final String RESTART_PROCESS_SUFFIX = ":lumine_restart";
    private static final String FILE_NAME = "lumine_last_crash.txt";
    private static final int MAX_LENGTH = 48 * 1024;

    private static volatile boolean handling;

    private LumineCrashLog() {
    }

    public static boolean isCrashProcess() {
        try (FileInputStream in = new FileInputStream("/proc/self/cmdline")) {
            final byte[] buffer = new byte[256];
            final int length = in.read(buffer);
            int end = 0;
            while (end < length && buffer[end] != 0) {
                end++;
            }
            final String name = new String(buffer, 0, end, StandardCharsets.UTF_8);
            return name.endsWith(CRASH_PROCESS_SUFFIX) || name.endsWith(RESTART_PROCESS_SUFFIX);
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static void install(Application application) {
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            if (!handling) {
                handling = true;
                try {
                    final String report = build(application, thread, error);
                    write(application, report);
                    launch(application, report);
                } catch (Throwable ignore) {
                }
            }
            if (previous != null) {
                previous.uncaughtException(thread, error);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        });
    }

    static String readSaved(Context context) {
        try (FileInputStream in = new FileInputStream(new File(context.getFilesDir(), FILE_NAME))) {
            final byte[] data = new byte[MAX_LENGTH];
            final int length = in.read(data);
            return length > 0 ? new String(data, 0, length, StandardCharsets.UTF_8) : null;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static String build(Context context, Thread thread, Throwable error) {
        final StringBuilder report = new StringBuilder();
        String version = "?";
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName + " (" + info.versionCode + ")";
        } catch (Throwable ignore) {
        }
        String commit = "?";
        try {
            commit = BuildVars.BUILD_COMMIT;
        } catch (Throwable ignore) {
        }
        report.append("luminegram ").append(version).append(' ').append(commit).append('\n');
        report.append("Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append(' ').append(Build.SUPPORTED_ABIS[0]).append('\n');
        report.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append('\n');
        if (Build.VERSION.SDK_INT >= 24) {
            final long seconds = (SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()) / 1000L;
            report.append("process uptime: ").append(seconds).append(" s\n");
        }
        if (thread != null) {
            report.append("thread: ").append(thread.getName()).append('\n');
        }
        final StringWriter stack = new StringWriter();
        final PrintWriter printer = new PrintWriter(stack);
        error.printStackTrace(printer);
        printer.flush();
        report.append('\n').append(stack);
        final String text = report.toString();
        return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
    }

    private static void write(Context context, String report) {
        try (FileOutputStream out = new FileOutputStream(new File(context.getFilesDir(), FILE_NAME))) {
            out.write(report.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable ignore) {
        }
    }

    private static void launch(Context context, String report) {
        final Intent intent = new Intent(context, LumineCrashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(EXTRA_REPORT, report);
        context.startActivity(intent);
    }
}
