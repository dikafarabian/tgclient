/*
 * luminegram - Home tab of the luminegram settings.
 * Bento layout in Material 3: a hero card with the app identity (name,
 * version, build type, commit, ABI) and a tile row with Notification and
 * Account. The hero turns to the error container when push is in trouble.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import app.lumine.LumineStrings;
import app.lumine.md3.LumineMd3;
import app.lumine.md3.LumineTonalCard;
import app.lumine.push.PushConfig;
import app.lumine.push.PushStatus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
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

    private LumineTonalCard hero;
    private GradientDrawable heroIconBackground;
    private ImageView heroIcon;
    private TextView heroName;
    private TextView heroVersion;
    private LinearLayout heroChips;
    private TextView chipBuild;
    private TextView chipCommit;
    private TextView chipAbi;

    private LumineTonalCard notificationTile;
    private GradientDrawable notificationIconBackground;
    private ImageView notificationIcon;
    private TextView notificationTitle;
    private TextView notificationService;
    private TextView notificationState;

    private LumineTonalCard accountTile;
    private GradientDrawable accountIconBackground;
    private ImageView accountIcon;
    private TextView accountTitle;
    private TextView accountName;
    private TextView accountConnection;

    public LumineHomePage(Context context, int account, Runnable onNotification) {
        super(context);
        this.account = account;
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(12), dp(16), dp(16));
        addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        buildHero(context, list);

        LinearLayout tiles = new LinearLayout(context);
        tiles.setOrientation(LinearLayout.HORIZONTAL);
        tiles.setBaselineAligned(false);
        list.addView(tiles, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        notificationTile = new LumineTonalCard(context, LumineMd3.TILE_RADIUS_DP, 0);
        notificationTile.setMinimumHeight(dp(LumineMd3.TILE_MIN_HEIGHT_DP));
        notificationTile.setClickable(true);
        notificationTile.setOnClickListener(v -> onNotification.run());
        LinearLayout notificationContent = tileContent(context);
        FrameLayout notificationIconFrame = iconFrame(context, LumineMd3.TILE_ICON_DP, 20, R.drawable.lumine_ic_notification);
        notificationIconBackground = (GradientDrawable) notificationIconFrame.getBackground();
        notificationIcon = (ImageView) notificationIconFrame.getChildAt(0);
        notificationContent.addView(notificationIconFrame, new LinearLayout.LayoutParams(dp(LumineMd3.TILE_ICON_DP), dp(LumineMd3.TILE_ICON_DP)));
        notificationTitle = tileText(context, 15, true);
        notificationTitle.setText(LumineStrings.get(R.string.LumineNotification));
        notificationContent.addView(notificationTitle, tileTextParams(10));
        notificationService = tileText(context, 13, false);
        notificationContent.addView(notificationService, tileTextParams(2));
        notificationState = tileText(context, 13, false);
        notificationContent.addView(notificationState, tileTextParams(0));
        notificationTile.addView(notificationContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        notificationParams.rightMargin = dp(LumineMd3.GAP_DP) / 2;
        tiles.addView(notificationTile, notificationParams);

        accountTile = new LumineTonalCard(context, LumineMd3.TILE_RADIUS_DP, 0);
        accountTile.setMinimumHeight(dp(LumineMd3.TILE_MIN_HEIGHT_DP));
        LinearLayout accountContent = tileContent(context);
        FrameLayout accountIconFrame = iconFrame(context, LumineMd3.TILE_ICON_DP, 20, R.drawable.lumine_ic_account);
        accountIconBackground = (GradientDrawable) accountIconFrame.getBackground();
        accountIcon = (ImageView) accountIconFrame.getChildAt(0);
        accountContent.addView(accountIconFrame, new LinearLayout.LayoutParams(dp(LumineMd3.TILE_ICON_DP), dp(LumineMd3.TILE_ICON_DP)));
        accountTitle = tileText(context, 15, true);
        accountTitle.setText(LumineStrings.get(R.string.LumineCardAccount));
        accountContent.addView(accountTitle, tileTextParams(10));
        accountName = tileText(context, 13, false);
        accountContent.addView(accountName, tileTextParams(2));
        accountConnection = tileText(context, 13, false);
        accountContent.addView(accountConnection, tileTextParams(0));
        accountTile.addView(accountContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams accountParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        accountParams.leftMargin = dp(LumineMd3.GAP_DP) / 2;
        tiles.addView(accountTile, accountParams);

        refresh();
    }

    private void buildHero(Context context, LinearLayout list) {
        hero = new LumineTonalCard(context, LumineMd3.HERO_RADIUS_DP, 0);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        hero.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(top, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        FrameLayout iconFrame = iconFrame(context, LumineMd3.HERO_ICON_DP, 26, R.drawable.lumine_settings_sparkle);
        heroIconBackground = (GradientDrawable) iconFrame.getBackground();
        heroIcon = (ImageView) iconFrame.getChildAt(0);
        top.addView(iconFrame, new LinearLayout.LayoutParams(dp(LumineMd3.HERO_ICON_DP), dp(LumineMd3.HERO_ICON_DP)));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textsParams.leftMargin = dp(16);
        top.addView(texts, textsParams);

        heroName = new TextView(context);
        heroName.setTextSize(22);
        heroName.setTypeface(AndroidUtilities.bold());
        heroName.setSingleLine(true);
        heroName.setEllipsize(TextUtils.TruncateAt.END);
        heroName.setText(LumineStrings.get(R.string.AppName));
        texts.addView(heroName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        heroVersion = new TextView(context);
        heroVersion.setTextSize(14);
        heroVersion.setSingleLine(true);
        texts.addView(heroVersion, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        heroChips = new LinearLayout(context);
        heroChips.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        chipsParams.topMargin = dp(16);
        content.addView(heroChips, chipsParams);
        chipBuild = chip(context, false);
        chipCommit = chip(context, true);
        chipAbi = chip(context, false);

        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        heroParams.bottomMargin = dp(LumineMd3.GAP_DP);
        list.addView(hero, heroParams);
    }

    private TextView chip(Context context, boolean monospace) {
        TextView chip = new TextView(context);
        chip.setTextSize(12);
        chip.setSingleLine(true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), 0, dp(12), 0);
        chip.setTypeface(monospace ? Typeface.MONOSPACE : AndroidUtilities.bold());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(28));
        if (heroChips.getChildCount() > 0) {
            params.leftMargin = dp(8);
        }
        heroChips.addView(chip, params);
        return chip;
    }

    private FrameLayout iconFrame(Context context, int sizeDp, int iconDp, int iconRes) {
        FrameLayout frame = new FrameLayout(context);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        frame.setBackground(background);
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        frame.addView(icon, new FrameLayout.LayoutParams(dp(iconDp), dp(iconDp), Gravity.CENTER));
        return frame;
    }

    private LinearLayout tileContent(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(LumineMd3.TILE_PADDING_DP), dp(LumineMd3.TILE_PADDING_DP), dp(LumineMd3.TILE_PADDING_DP), dp(LumineMd3.TILE_PADDING_DP));
        return content;
    }

    private TextView tileText(Context context, int sizeSp, boolean bold) {
        TextView text = new TextView(context);
        text.setTextSize(sizeSp);
        text.setSingleLine(true);
        text.setEllipsize(TextUtils.TruncateAt.END);
        if (bold) {
            text.setTypeface(AndroidUtilities.bold());
        }
        return text;
    }

    private LinearLayout.LayoutParams tileTextParams(int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topDp);
        return params;
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

    private static String str(int res) {
        return LumineStrings.get(res);
    }

    private static int stateColor(int state, int neutral) {
        if (state == GOOD) {
            return Theme.getColor(Theme.key_windowBackgroundWhiteGreenText2);
        } else if (state == BAD) {
            return Theme.getColor(Theme.key_text_RedRegular);
        }
        return neutral;
    }

    private void setChip(TextView chip, CharSequence text, int background, int foreground) {
        chip.setText(text);
        chip.setTextColor(foreground);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dp(14));
        shape.setColor(ColorUtils.setAlphaComponent(background, 0x2E));
        chip.setBackground(shape);
    }

    public void refresh() {
        final LumineMd3.Roles roles = LumineMd3.roles();
        final PushStatus.Health health = PushStatus.health(account);
        refreshHero(roles, health.level == BAD);
        refreshNotification(roles, health);
        refreshAccount(roles);
    }

    private void refreshHero(LumineMd3.Roles roles, boolean problem) {
        final int container = problem ? roles.errorContainer : roles.primaryContainer;
        final int onContainer = problem ? roles.onErrorContainer : roles.onPrimaryContainer;
        hero.setCardColor(container);
        heroIconBackground.setColor(problem ? roles.error : roles.primary);
        heroIcon.setColorFilter(problem ? roles.errorContainer : roles.onPrimary, PorterDuff.Mode.SRC_IN);
        heroName.setTextColor(onContainer);
        heroVersion.setTextColor(onContainer);

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
        heroVersion.setText(version);
        setChip(chipBuild, type, onContainer, onContainer);
        setChip(chipCommit, BuildVars.BUILD_COMMIT, onContainer, onContainer);
        setChip(chipAbi, Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : Build.CPU_ABI, onContainer, onContainer);
    }

    private void refreshNotification(LumineMd3.Roles roles, PushStatus.Health health) {
        notificationTile.setCardColor(roles.surfaceContainer);
        notificationIconBackground.setColor(roles.primaryContainer);
        notificationIcon.setColorFilter(roles.onPrimaryContainer, PorterDuff.Mode.SRC_IN);
        notificationTitle.setTextColor(roles.onSurface);
        notificationService.setTextColor(roles.onSurfaceVariant);
        notificationService.setText(PushStatus.typeName(PushConfig.type()));
        notificationState.setText(health.text);
        notificationState.setTextColor(stateColor(health.level, roles.onSurfaceVariant));
    }

    private void refreshAccount(LumineMd3.Roles roles) {
        accountTile.setCardColor(roles.surfaceContainer);
        accountIconBackground.setColor(roles.primaryContainer);
        accountIcon.setColorFilter(roles.onPrimaryContainer, PorterDuff.Mode.SRC_IN);
        accountTitle.setTextColor(roles.onSurface);
        accountName.setTextColor(roles.onSurfaceVariant);

        final UserConfig config = UserConfig.getInstance(account);
        final TLRPC.User user = config.getCurrentUser();
        if (!config.isClientActivated() || user == null) {
            accountName.setText(str(R.string.LumineNotLoggedIn));
        } else {
            accountName.setText(UserObject.getUserName(user));
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
        accountConnection.setText(str(res));
        accountConnection.setTextColor(stateColor(state, roles.onSurfaceVariant));
    }
}
