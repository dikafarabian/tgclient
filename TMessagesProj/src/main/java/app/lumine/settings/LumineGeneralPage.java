/*
 * luminegram - General tab of the luminegram settings.
 * Notifications: push service selection, push status and battery optimization.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import app.lumine.LumineStrings;
import app.lumine.push.LuminePushLog;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import app.lumine.push.PushSwitcher;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

public class LumineGeneralPage extends ScrollView {

    private final BaseFragment fragment;
    private final LumineCards.Row serviceRow;
    private final LumineCards.Row statusRow;
    private final LumineCards.Row batteryRow;

    public LumineGeneralPage(Context context, BaseFragment fragment) {
        super(context);
        this.fragment = fragment;
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineGeneralNotifications));
        serviceRow = LumineCards.row(context, card, LumineStrings.get(R.string.LuminePushService), v -> showServiceDialog());
        statusRow = LumineCards.row(context, card, LumineStrings.get(R.string.LuminePushStatus), v -> copyReport());
        batteryRow = LumineCards.row(context, card, LumineStrings.get(R.string.LumineBatteryOptimization), v -> openBatterySettings());
        LumineCards.info(context, list, LumineStrings.get(R.string.LuminePushInfo));

        refresh();
    }

    public void refresh() {
        serviceRow.value.setText(PushStatus.typeName(PushConfig.type()));
        statusRow.value.setText(PushStatus.summary());
        batteryRow.value.setText(LumineStrings.get(PushStatus.batteryUnrestricted() ? R.string.LumineBatteryOff : R.string.LumineBatteryOn));
    }

    private void showServiceDialog() {
        if (fragment.getParentActivity() == null) {
            return;
        }
        final int[] types = {PushConfig.TYPE_IN_APP, PushConfig.TYPE_FCM, PushConfig.TYPE_MICROG};
        final CharSequence[] names = new CharSequence[types.length];
        for (int a = 0; a < types.length; a++) {
            names[a] = PushStatus.typeName(types[a]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LuminePushService));
        builder.setItems(names, (dialog, which) -> {
            if (types[which] == PushConfig.type()) {
                return;
            }
            PushSwitcher.apply(types[which]);
            refresh();
            Toast.makeText(getContext(), LumineStrings.get(R.string.LuminePushRestartHint), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        fragment.showDialog(builder.create());
    }

    private void copyReport() {
        AndroidUtilities.addToClipboard(LuminePushLog.report());
        Toast.makeText(getContext(), LumineStrings.get(R.string.LuminePushReportCopied), Toast.LENGTH_SHORT).show();
    }

    private void openBatterySettings() {
        if (fragment.getParentActivity() == null) {
            return;
        }
        try {
            fragment.getParentActivity().startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            return;
        } catch (Exception ignore) {
        }
        try {
            fragment.getParentActivity().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName())));
        } catch (Exception e) {
            Toast.makeText(getContext(), LumineStrings.get(R.string.LumineBatteryUnavailable), Toast.LENGTH_SHORT).show();
        }
    }
}
