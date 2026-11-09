/*
 * luminegram - Notification page: push service, state, battery and the push details.
 * Opened from the General tab row and from the Notification card on Home.
 */
package app.lumine.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import app.lumine.LumineStrings;
import app.lumine.restart.LumineRestart;
import app.lumine.push.LuminePushLog;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import app.lumine.push.PushSwitcher;
import app.lumine.restart.LumineRestart;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SectionsScrollView;

public class LumineNotificationActivity extends BaseFragment {

    private static final long REFRESH_MS = 2000;

    private final Runnable refreshRunnable = this::tick;
    private boolean running;

    private LumineCards.Row serviceRow;
    private LumineCards.Row stateRow;
    private LumineCards.Row batteryRow;
    private LinearLayout detailsCard;
    private LumineCards.Row servicesRow;
    private LumineCards.Row tokenRow;
    private LumineCards.Row registrationRow;
    private LumineCards.Row lastPushRow;
    private LumineCards.Row lastErrorRow;

    private SectionsScrollView scroll;

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Nullable
    @Override
    protected SectionsScrollView getScrollViewForSimpleGlass() {
        return scroll;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        simpleActionBar.setBackButtonImage(R.drawable.ic_ab_back);
        simpleActionBar.setTitle(LumineStrings.get(R.string.LumineNotification));
        LumineRestart.menu(this);

        final FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        LinearLayout list = new SectionsScrollView.SectionsLinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll = new SectionsScrollView(context, list, resourceProvider);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        scroll.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineNotification));
        serviceRow = LumineCards.row(context, card, LumineStrings.get(R.string.LuminePushService), v -> showServiceDialog());
        stateRow = LumineCards.row(context, card, LumineStrings.get(R.string.LumineState), v -> copyReport());
        batteryRow = LumineCards.row(context, card, LumineStrings.get(R.string.LumineBatteryOptimization), v -> openBatterySettings());
        LumineCards.info(context, list, LumineStrings.get(R.string.LuminePushInfo));

        detailsCard = LumineCards.card(context, list, LumineStrings.get(R.string.LumineCardDetails));
        servicesRow = LumineCards.row(context, detailsCard, LumineStrings.get(R.string.LumineGooglePlayServices), null);
        tokenRow = LumineCards.row(context, detailsCard, LumineStrings.get(R.string.LumineFcmToken), null);
        registrationRow = LumineCards.row(context, detailsCard, LumineStrings.get(R.string.LumineServerRegistration), null);
        lastPushRow = LumineCards.row(context, detailsCard, LumineStrings.get(R.string.LumineLastPush), null);
        lastErrorRow = LumineCards.row(context, detailsCard, LumineStrings.get(R.string.LumineLastError), null);

        refresh();
        return fragmentView;
    }

    private void refresh() {
        if (serviceRow == null) {
            return;
        }
        serviceRow.value.setText(PushStatus.typeName(PushConfig.type()));
        final PushStatus.Health health = PushStatus.health(currentAccount);
        LumineCards.setValue(stateRow.value, health.text, health.level);
        batteryRow.value.setText(LumineStrings.get(PushStatus.batteryUnrestricted() ? R.string.LumineBatteryOff : R.string.LumineBatteryOn));

        final boolean google = PushConfig.usesGoogle();
        detailsCard.setVisibility(google ? View.VISIBLE : View.GONE);
        if (!google) {
            return;
        }
        final PushStatus.Health services = PushStatus.services();
        LumineCards.setValue(servicesRow.value, services.text, services.level);
        final PushStatus.Health token = PushStatus.token();
        LumineCards.setValue(tokenRow.value, token.text, token.level);
        final PushStatus.Health registration = PushStatus.registration(currentAccount);
        LumineCards.setValue(registrationRow.value, registration.text, registration.level);

        final long lastPush = LuminePushLog.lastPushTime();
        if (lastPush <= 0) {
            LumineCards.setValue(lastPushRow.value, LumineStrings.get(R.string.LumineNever), PushStatus.Health.NEUTRAL);
        } else {
            LumineCards.setValue(lastPushRow.value, LocaleController.formatDateTime(lastPush / 1000L, true), PushStatus.Health.GOOD);
        }
        final String error = LuminePushLog.lastError();
        if (error == null || error.isEmpty()) {
            LumineCards.setValue(lastErrorRow.value, LumineStrings.get(R.string.LumineNone), PushStatus.Health.GOOD);
        } else {
            LumineCards.setValue(lastErrorRow.value, error, PushStatus.Health.BAD);
        }
    }

    private void tick() {
        if (!running) {
            return;
        }
        refresh();
        AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_MS);
    }

    private void showServiceDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final int[] types = {PushConfig.TYPE_IN_APP, PushConfig.TYPE_FCM, PushConfig.TYPE_MICROG};
        final CharSequence[] names = new CharSequence[types.length];
        for (int a = 0; a < types.length; a++) {
            names[a] = PushStatus.typeName(types[a]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LuminePushService));
        builder.setItems(names, (dialog, which) -> {
            if (types[which] == PushConfig.type()) {
                return;
            }
            PushSwitcher.apply(types[which]);
            refresh();
            LumineRestart.hint(this);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void copyReport() {
        AndroidUtilities.addToClipboard(LuminePushLog.report());
        if (getParentActivity() != null) {
            Toast.makeText(getParentActivity(), LumineStrings.get(R.string.LuminePushReportCopied), Toast.LENGTH_SHORT).show();
        }
    }

    private void openBatterySettings() {
        if (getParentActivity() == null) {
            return;
        }
        try {
            getParentActivity().startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            return;
        } catch (Exception ignore) {
        }
        try {
            getParentActivity().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName())));
        } catch (Exception e) {
            Toast.makeText(getParentActivity(), LumineStrings.get(R.string.LumineBatteryUnavailable), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        refresh();
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
        AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
    }

    @Override
    public void onFragmentDestroy() {
        running = false;
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
        super.onFragmentDestroy();
    }
}
