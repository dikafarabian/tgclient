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
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import app.lumine.LumineStrings;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
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
    private TextView buildValue;
    private TextView commitValue;
    private TextView abiValue;
    private TextView servicesValue;
    private TextView tokenValue;
    private TextView registrationValue;
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
        buildValue = addRow(appCard, R.string.LumineBuild);
        commitValue = addRow(appCard, R.string.LumineCommit);
        abiValue = addRow(appCard, R.string.LumineDevice);

        LinearLayout pushCard = addCard(list, R.string.LumineCardPush);
        servicesValue = addRow(pushCard, R.string.LumineGooglePlayServices);
        tokenValue = addRow(pushCard, R.string.LumineFcmToken);
        registrationValue = addRow(pushCard, R.string.LumineServerRegistration);

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
        String build = "";
        try {
            final Context context = ApplicationLoader.applicationContext;
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            build = String.valueOf(info.versionCode / 10);
        } catch (Throwable ignore) {
        }
        set(versionValue, version, NEUTRAL);
        set(buildValue, build, NEUTRAL);
        set(commitValue, BuildVars.BUILD_COMMIT, NEUTRAL);
        set(abiValue, Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : Build.CPU_ABI, NEUTRAL);
    }

    private void refreshPush() {
        boolean services = false;
        try {
            services = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ApplicationLoader.applicationContext) == ConnectionResult.SUCCESS;
        } catch (Throwable ignore) {
        }
        set(servicesValue, str(services ? R.string.LumineAvailable : R.string.LumineMissing), services ? GOOD : BAD);

        final String token = SharedConfig.pushString;
        final String status = SharedConfig.pushStringStatus == null ? "" : SharedConfig.pushStringStatus;
        if (!TextUtils.isEmpty(token)) {
            set(tokenValue, str(R.string.LumineTokenReady), GOOD);
        } else if (status.startsWith("__FIREBASE_FAILED__") || status.startsWith("__NO_GOOGLE_PLAY_SERVICES__")) {
            set(tokenValue, str(R.string.LumineTokenFailed), BAD);
        } else {
            set(tokenValue, str(R.string.LumineTokenWaiting), NEUTRAL);
        }

        final boolean registered = UserConfig.getInstance(account).registeredForPush;
        set(registrationValue, str(registered ? R.string.LumineRegistered : R.string.LumineNotRegistered), registered ? GOOD : BAD);
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
