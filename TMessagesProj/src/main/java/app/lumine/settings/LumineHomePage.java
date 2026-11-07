/*
 * luminegram - Home tab of the luminegram settings.
 * Status cards: app build, push notifications, account.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import app.lumine.LumineStrings;
import app.lumine.push.LuminePushLog;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class LumineHomePage extends ScrollView {

    private static final int NEUTRAL = 0;
    private static final int GOOD = 1;
    private static final int BAD = 2;
    private static final long REFRESH_MS = 2000;

    private final int account;
    private final Runnable refreshRunnable = this::tick;
    private boolean running;

    private TextView versionValue;
    private TextView buildTypeValue;
    private TextView abiValue;
    private TextView serviceValue;
    private TextView statusValue;
    private TextView lastPushValue;
    private TextView lastErrorValue;
    private TextView nameValue;
    private TextView idValue;
    private TextView connectionValue;

    public LumineHomePage(Context context, int account) {
        super(context);
        this.account = account;
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout appCard = addCard(list, R.string.LumineCardApp);
        versionValue = addRow(appCard, R.string.LumineVersion);
        buildTypeValue = addRow(appCard, R.string.LumineBuildType);
        abiValue = addRow(appCard, R.string.LumineAbi);

        LinearLayout pushCard = addCard(list, R.string.LumineCardPush);
        serviceValue = addRow(pushCard, R.string.LuminePushService);
        statusValue = addRow(pushCard, R.string.LumineStatus);
        lastPushValue = addRow(pushCard, R.string.LumineLastPush);
        lastErrorValue = addRow(pushCard, R.string.LumineLastError);
        pushCard.setOnClickListener(v -> {
            AndroidUtilities.addToClipboard(LuminePushLog.report());
            Toast.makeText(getContext(), LumineStrings.get(R.string.LuminePushReportCopied), Toast.LENGTH_SHORT).show();
        });

        LinearLayout accountCard = addCard(list, R.string.LumineCardAccount);
        nameValue = addRow(accountCard, R.string.LumineAccountName);
        idValue = addRow(accountCard, R.string.LumineAccountId);
        connectionValue = addRow(accountCard, R.string.LumineConnection);

        refresh();
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        refresh();
        AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_MS);
    }

    public void stop() {
        running = false;
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
    }

    private void tick() {
        if (!running) {
            return;
        }
        refresh();
        AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_MS);
    }

    private LinearLayout addCard(LinearLayout parent, int titleRes) {
        final Context context = getContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite)));
        card.setPadding(dp(16), dp(14), dp(16), dp(10));

        TextView title = new TextView(context);
        title.setTextSize(14);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        title.setText(LumineStrings.get(titleRes));
        card.addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        parent.addView(card, params);
        return card;
    }

    private TextView addRow(LinearLayout card, int labelRes) {
        final Context context = getContext();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = new TextView(context);
        label.setTextSize(14);
        label.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        label.setText(LumineStrings.get(labelRes));
        row.addView(label, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView value = new TextView(context);
        value.setTextSize(14);
        value.setGravity(Gravity.END);
        value.setSingleLine(true);
        value.setEllipsize(TextUtils.TruncateAt.END);
        value.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        valueParams.leftMargin = dp(16);
        row.addView(value, valueParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(10);
        card.addView(row, rowParams);
        return value;
    }

    private void set(TextView view, CharSequence text, int state) {
        view.setText(text);
        int key = Theme.key_windowBackgroundWhiteBlackText;
        if (state == GOOD) {
            key = Theme.key_windowBackgroundWhiteGreenText2;
        } else if (state == BAD) {
            key = Theme.key_text_RedRegular;
        }
        view.setTextColor(Theme.getColor(key));
    }

    private static String str(int res) {
        return LumineStrings.get(res);
    }

    public void refresh() {
        refreshApp();
        refreshPush();
        refreshAccount();
    }

    private void refreshApp() {
        String version = "";
        try {
            final Context context = ApplicationLoader.applicationContext;
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName + " (" + (info.versionCode / 10) + ")";
        } catch (Throwable ignore) {
        }
        String type = BuildVars.BUILD_TYPE_NAME;
        if (!TextUtils.isEmpty(type)) {
            type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        }
        set(versionValue, version, NEUTRAL);
        set(buildTypeValue, type + " (" + BuildVars.BUILD_COMMIT + ")", NEUTRAL);
        set(abiValue, Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : Build.CPU_ABI, NEUTRAL);
    }

    private void refreshPush() {
        set(serviceValue, PushStatus.typeName(PushConfig.type()), NEUTRAL);
        final PushStatus.Health health = PushStatus.health(account);
        set(statusValue, health.text, health.level);

        final boolean google = PushConfig.usesGoogle();
        setRowVisible(lastPushValue, google);
        final long lastPush = LuminePushLog.lastPushTime();
        if (lastPush <= 0) {
            set(lastPushValue, str(R.string.LumineNever), NEUTRAL);
        } else {
            set(lastPushValue, LocaleController.formatDateTime(lastPush / 1000L, true), GOOD);
        }

        final String error = google ? LuminePushLog.lastError() : "";
        setRowVisible(lastErrorValue, !TextUtils.isEmpty(error));
        set(lastErrorValue, error, BAD);
    }

    private static void setRowVisible(TextView value, boolean visible) {
        ((View) value.getParent()).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void refreshAccount() {
        final UserConfig config = UserConfig.getInstance(account);
        final TLRPC.User user = config.getCurrentUser();
        if (!config.isClientActivated() || user == null) {
            set(nameValue, str(R.string.LumineNotLoggedIn), NEUTRAL);
            set(idValue, "-", NEUTRAL);
        } else {
            set(nameValue, UserObject.getUserName(user), NEUTRAL);
            set(idValue, String.valueOf(user.id), NEUTRAL);
        }

        int res;
        int state = NEUTRAL;
        switch (ConnectionsManager.getInstance(account).getConnectionState()) {
            case ConnectionsManager.ConnectionStateConnected:
                res = R.string.LumineConnected;
                state = GOOD;
                break;
            case ConnectionsManager.ConnectionStateWaitingForNetwork:
                res = R.string.LumineWaitingNetwork;
                state = BAD;
                break;
            case ConnectionsManager.ConnectionStateUpdating:
                res = R.string.LumineUpdating;
                break;
            case ConnectionsManager.ConnectionStateConnectingToProxy:
                res = R.string.LumineProxy;
                break;
            default:
                res = R.string.LumineConnecting;
                break;
        }
        set(connectionValue, str(res), state);
    }
}
