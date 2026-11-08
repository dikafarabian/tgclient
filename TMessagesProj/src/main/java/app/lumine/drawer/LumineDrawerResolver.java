/*
 * luminegram - drawer item resolver.
 * Turns an item id into an icon, a label and the actions of a row. Returns
 * nothing for an item that has no meaning right now (empty archive, no bots).
 */
package app.lumine.drawer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import app.lumine.LumineStrings;
import app.lumine.ghost.LumineGhost;
import app.lumine.settings.LumineGhostActivity;
import app.lumine.settings.LumineSettingsActivity;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.CallLogActivity;
import org.telegram.ui.ChannelCreateActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.WebAppDisclaimerAlert;
import org.telegram.ui.bots.BotWebViewSheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LumineDrawerResolver {

    public static final class Info {
        public final int iconRes;
        public final CharSequence text;
        public final Runnable onClick;
        public final Runnable onLongClick;

        Info(int iconRes, CharSequence text, Runnable onClick, Runnable onLongClick) {
            this.iconRes = iconRes;
            this.text = text;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }
    }

    private LumineDrawerResolver() {
    }

    public static List<Info> resolve(int id, int account, BaseFragment fragment) {
        final LumineDrawerItem item = LumineDrawerItem.getById(id);
        if (item == null || fragment == null) {
            return Collections.emptyList();
        }
        if (item == LumineDrawerItem.BOTS) {
            return resolveBots(account, fragment);
        }
        if (item == LumineDrawerItem.ARCHIVE && !hasArchivedChats(account)) {
            return Collections.emptyList();
        }
        final Info info = resolveItem(item, account, fragment);
        return info == null ? Collections.<Info>emptyList() : Collections.singletonList(info);
    }

    public static Info describe(int id) {
        final LumineDrawerItem item = LumineDrawerItem.getById(id);
        if (item == null) {
            return null;
        }
        switch (item) {
            case PROFILE:
                return new Info(R.drawable.left_status_profile, LocaleController.getString(R.string.MyProfile), null, null);
            case ARCHIVE:
                return new Info(R.drawable.msg_archive, LocaleController.getString(R.string.ArchivedChats), null, null);
            case BOTS:
                return new Info(R.drawable.msg_bot, LocaleController.getString(R.string.SearchApps), null, null);
            case NEW_GROUP:
                return new Info(R.drawable.msg_groups, LocaleController.getString(R.string.NewGroup), null, null);
            case CONTACTS:
                return new Info(R.drawable.msg_contacts, LocaleController.getString(R.string.Contacts), null, null);
            case NEW_CHANNEL:
                return new Info(R.drawable.msg_channel, LocaleController.getString(R.string.NewChannel), null, null);
            case CALLS:
                return new Info(R.drawable.msg_calls, LocaleController.getString(R.string.Calls), null, null);
            case SAVED:
                return new Info(R.drawable.msg_saved, LocaleController.getString(R.string.SavedMessages), null, null);
            case SETTINGS:
                return new Info(R.drawable.msg_settings, LocaleController.getString(R.string.Settings), null, null);
            case GHOST_MODE:
                return new Info(R.drawable.lumine_tab_stealth, ghostTitle(), null, null);
            case LUMINE_SETTINGS:
                return new Info(R.drawable.lumine_settings_sparkle, LocaleController.getString(R.string.AppName), null, null);
            default:
                return null;
        }
    }

    private static Info resolveItem(LumineDrawerItem item, final int account, final BaseFragment fragment) {
        switch (item) {
            case PROFILE:
                return new Info(R.drawable.left_status_profile, LocaleController.getString(R.string.MyProfile), new Runnable() {
                    @Override
                    public void run() {
                        final Bundle args = new Bundle();
                        args.putLong("user_id", UserConfig.getInstance(account).getClientUserId());
                        args.putBoolean("my_profile", true);
                        fragment.presentFragment(new ProfileActivity(args));
                    }
                }, null);
            case ARCHIVE:
                return new Info(R.drawable.msg_archive, LocaleController.getString(R.string.ArchivedChats), new Runnable() {
                    @Override
                    public void run() {
                        final Bundle args = new Bundle();
                        args.putInt("folderId", 1);
                        fragment.presentFragment(new DialogsActivity(args));
                    }
                }, null);
            case NEW_GROUP:
                return new Info(R.drawable.msg_groups, LocaleController.getString(R.string.NewGroup), new Runnable() {
                    @Override
                    public void run() {
                        fragment.presentFragment(new GroupCreateActivity(new Bundle()));
                    }
                }, null);
            case CONTACTS:
                return new Info(R.drawable.msg_contacts, LocaleController.getString(R.string.Contacts), new Runnable() {
                    @Override
                    public void run() {
                        final Bundle args = new Bundle();
                        args.putBoolean("needPhonebook", true);
                        args.putBoolean("needFinishFragment", false);
                        fragment.presentFragment(new ContactsActivity(args));
                    }
                }, null);
            case NEW_CHANNEL:
                return new Info(R.drawable.msg_channel, LocaleController.getString(R.string.NewChannel), new Runnable() {
                    @Override
                    public void run() {
                        presentChannelCreate(fragment);
                    }
                }, null);
            case CALLS:
                return new Info(R.drawable.msg_calls, LocaleController.getString(R.string.Calls), new Runnable() {
                    @Override
                    public void run() {
                        fragment.presentFragment(new CallLogActivity());
                    }
                }, null);
            case SAVED:
                return new Info(R.drawable.msg_saved, LocaleController.getString(R.string.SavedMessages), new Runnable() {
                    @Override
                    public void run() {
                        final Bundle args = new Bundle();
                        args.putLong("user_id", UserConfig.getInstance(account).getClientUserId());
                        fragment.presentFragment(new ChatActivity(args));
                    }
                }, null);
            case SETTINGS:
                return new Info(R.drawable.msg_settings, LocaleController.getString(R.string.Settings), new Runnable() {
                    @Override
                    public void run() {
                        fragment.presentFragment(new SettingsActivity());
                    }
                }, null);
            case GHOST_MODE:
                return new Info(R.drawable.lumine_tab_stealth, ghostTitle(), new Runnable() {
                    @Override
                    public void run() {
                        final boolean wasActive = LumineGhost.active();
                        LumineGhost.setActive(!wasActive);
                        BulletinFactory.of(fragment).createSuccessBulletin(LumineStrings.get(
                                wasActive ? R.string.LumineDrawerGhostDisabled : R.string.LumineDrawerGhostEnabled)).show();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        fragment.presentFragment(new LumineGhostActivity());
                    }
                });
            case LUMINE_SETTINGS:
                return new Info(R.drawable.lumine_settings_sparkle, LocaleController.getString(R.string.AppName), new Runnable() {
                    @Override
                    public void run() {
                        fragment.presentFragment(new LumineSettingsActivity());
                    }
                }, null);
            default:
                return null;
        }
    }

    private static CharSequence ghostTitle() {
        return LumineStrings.get(LumineGhost.active() ? R.string.LumineDrawerGhostDisable : R.string.LumineDrawerGhostEnable);
    }

    private static void presentChannelCreate(BaseFragment fragment) {
        final SharedPreferences prefs = MessagesController.getGlobalMainSettings();
        if (BuildVars.DEBUG_VERSION || !prefs.getBoolean("channel_intro", false)) {
            fragment.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANNEL_CREATE));
            prefs.edit().putBoolean("channel_intro", true).apply();
        } else {
            final Bundle args = new Bundle();
            args.putInt("step", 0);
            fragment.presentFragment(new ChannelCreateActivity(args));
        }
    }

    public static boolean hasArchivedChats(int account) {
        try {
            final List<TLRPC.Dialog> archived = MessagesController.getInstance(account).getDialogs(1);
            return archived != null && !archived.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static List<Info> resolveBots(final int account, final BaseFragment fragment) {
        final LaunchActivity launchActivity = findLaunchActivity(fragment);
        final TLRPC.TL_attachMenuBots attachMenuBots = MediaDataController.getInstance(account).getAttachMenuBots();
        if (launchActivity == null || attachMenuBots == null || attachMenuBots.bots == null || attachMenuBots.bots.isEmpty()) {
            return Collections.emptyList();
        }
        final ArrayList<Info> result = new ArrayList<>();
        for (final TLRPC.TL_attachMenuBot bot : attachMenuBots.bots) {
            if (!bot.show_in_side_menu) {
                continue;
            }
            result.add(new Info(R.drawable.msg_bot, bot.short_name, botClick(account, fragment, bot, launchActivity), new Runnable() {
                @Override
                public void run() {
                    BotWebViewSheet.deleteBot(account, bot.bot_id, null);
                }
            }));
        }
        return result;
    }

    private static Runnable botClick(final int account, final BaseFragment fragment, final TLRPC.TL_attachMenuBot bot, final LaunchActivity launchActivity) {
        return new Runnable() {
            @Override
            public void run() {
                if (bot.inactive || bot.side_menu_disclaimer_needed) {
                    final Context context = fragment.getContext() != null ? fragment.getContext() : launchActivity;
                    WebAppDisclaimerAlert.show(context, new Utilities.Callback<Boolean>() {
                        @Override
                        public void run(Boolean allowSendMessage) {
                            final TLRPC.TL_messages_toggleBotInAttachMenu req = new TLRPC.TL_messages_toggleBotInAttachMenu();
                            req.bot = MessagesController.getInstance(account).getInputUser(bot.bot_id);
                            req.enabled = true;
                            req.write_allowed = true;
                            ConnectionsManager.getInstance(account).sendRequest(req, new org.telegram.tgnet.RequestDelegate() {
                                @Override
                                public void run(TLObject response, TLRPC.TL_error error) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            bot.side_menu_disclaimer_needed = false;
                                            bot.inactive = false;
                                            LaunchActivity.showAttachMenuBot(launchActivity, account, bot, null, true);
                                            MediaDataController.getInstance(account).updateAttachMenuBotsInCache();
                                        }
                                    });
                                }
                            }, ConnectionsManager.RequestFlagInvokeAfter | ConnectionsManager.RequestFlagFailOnServerErrors);
                        }
                    }, null, null);
                } else {
                    LaunchActivity.showAttachMenuBot(launchActivity, account, bot, null, true);
                }
            }
        };
    }

    private static LaunchActivity findLaunchActivity(BaseFragment fragment) {
        final Activity activity = AndroidUtilities.findActivity(fragment.getContext() != null ? fragment.getContext() : fragment.getParentActivity());
        return activity instanceof LaunchActivity ? (LaunchActivity) activity : LaunchActivity.instance;
    }
}
