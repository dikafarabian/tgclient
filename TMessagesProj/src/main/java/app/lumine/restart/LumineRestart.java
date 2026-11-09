/*
 * luminegram - app restart and the restart hint.
 * Every setting that needs a restart to take effect must call hint(fragment) after it is changed.
 * The hint is always the same bulletin: the text and a "Restart now" button. Do not show a Toast or build another popup.
 */
package app.lumine.restart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import androidx.annotation.Nullable;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleActionBar;
import org.telegram.ui.ActionBar.SimpleActionBarMenuItem;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import app.lumine.LumineStrings;

public final class LumineRestart {

    static final String EXTRA_LAUNCH = "lumine_restart_launch";
    static final String EXTRA_PID = "lumine_restart_pid";
    private static final int MENU_ITEM = 1;

    private LumineRestart() {
    }

    public static void menu(BaseFragment fragment) {
        final SimpleActionBar bar = fragment.getSimpleActionBar();
        if (bar == null) {
            return;
        }
        final SimpleActionBarMenuItem item = bar.createMenu().addItem(0, R.drawable.ic_ab_other);
        item.setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));
        item.addSubItem(MENU_ITEM, R.drawable.msg_reset, LumineStrings.get(R.string.LumineRestartApp));
        bar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    fragment.finishFragment();
                } else if (id == MENU_ITEM) {
                    confirm(fragment);
                }
            }
        });
    }

    public static void hint(@Nullable BaseFragment fragment) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        final Activity activity = fragment.getParentActivity();
        BulletinFactory.of(fragment).createSimpleBulletin(
                R.raw.info,
                LumineStrings.get(R.string.LumineRestartNeeded),
                LumineStrings.get(R.string.LumineRestartNow),
                () -> restart(activity)
        ).show();
    }

    public static void confirm(@Nullable BaseFragment fragment) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        final Activity activity = fragment.getParentActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(LumineStrings.get(R.string.LumineRestartApp));
        builder.setMessage(LumineStrings.get(R.string.LumineRestartConfirm));
        builder.setPositiveButton(LumineStrings.get(R.string.LumineRestartNow), (dialog, which) -> restart(activity));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        fragment.showDialog(builder.create());
    }

    public static void restart(Context context) {
        final Intent launch = new Intent(context, LaunchActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final Intent helper = new Intent(context, LumineRestartActivity.class);
        helper.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        helper.putExtra(EXTRA_LAUNCH, launch);
        helper.putExtra(EXTRA_PID, Process.myPid());
        context.startActivity(helper);
    }
}
