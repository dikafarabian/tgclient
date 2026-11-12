/*
 * luminegram - Chat Interface page.
 * Options that change how the chat screens look. Every option reads and
 * writes through LumineChatConfig.
 */
package app.lumine.settings;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.lumine.LumineStrings;
import app.lumine.chat.LumineActionButton;
import app.lumine.chat.LumineChatConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;

public class LumineChatActivity extends LumineSubPage {

    @Override
    protected int titleRes() {
        return R.string.LumineChatTitle;
    }

    @Override
    protected void build(Context context, LinearLayout list) {
        final LinearLayout inputCard = LumineCards.card(context, list, LumineStrings.get(R.string.LumineChatHeaderInput));
        LumineCards.toggleRow(context, inputCard, LumineStrings.get(R.string.LumineChatIosInput), null, LumineChatConfig.getIosInput(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setIosInput(enabled);
            }
        });
        LumineCards.toggleRow(context, inputCard, LumineStrings.get(R.string.LumineChatIosCompact), null, LumineChatConfig.getIosCompact(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setIosCompact(enabled);
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatIosInputInfo));

        final LinearLayout card = LumineCards.card(context, list, null);
        LumineCards.toggleRow(context, card, LumineStrings.get(R.string.LumineChatHideFade), null, LumineChatConfig.getHideFade(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setHideFade(enabled);
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatHideFadeInfo));

        final LinearLayout input = LumineCards.card(context, list, null);
        final LumineCards.Row styleRow = LumineCards.row(context, input, LumineStrings.get(R.string.LumineChatActionButton), v -> showStyleDialog());
        styleValue = styleRow.value;
        LumineCards.setValue(styleValue, styleName(LumineChatConfig.getActionButtonStyle()), 0);
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatActionButtonInfo));

        final LinearLayout iosCard = LumineCards.card(context, list, LumineStrings.get(R.string.LumineChatIosStyleCard));
        LumineCards.toggleRow(context, iosCard, LumineStrings.get(R.string.LumineChatIosList), null, LumineChatConfig.getIosList(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setIosList(enabled);
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatIosListInfo));

        final LinearLayout headerCard = LumineCards.card(context, list, LumineStrings.get(R.string.LumineChatHeaderCard));
        LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillBackground), null, LumineChatConfig.getChatPill(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setChatPill(enabled);
            }
        });
        LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillSlot), null, LumineChatConfig.getChatPillAvatarSlot(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setChatPillAvatarSlot(enabled);
                rebuild(160);
            }
        });
        if (LumineChatConfig.getChatPillAvatarSlot()) {
            LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillSwap), null, LumineChatConfig.getChatPillAvatarSwap(), new LumineCards.OnToggle() {
                @Override
                public void onToggle(boolean enabled) {
                    LumineChatConfig.setChatPillAvatarSwap(enabled);
                }
            });
        } else {
            LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillStatic), null, LumineChatConfig.getChatPillAvatarStatic(), new LumineCards.OnToggle() {
                @Override
                public void onToggle(boolean enabled) {
                    LumineChatConfig.setChatPillAvatarStatic(enabled);
                    rebuild(160);
                }
            });
            if (!LumineChatConfig.getChatPillAvatarStatic()) {
                LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillRight), null, LumineChatConfig.getChatPillAvatarRight(), new LumineCards.OnToggle() {
                    @Override
                    public void onToggle(boolean enabled) {
                        LumineChatConfig.setChatPillAvatarRight(enabled);
                    }
                });
            }
        }
        LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatPillHideAvatar), null, LumineChatConfig.getChatPillHideAvatar(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setChatPillHideAvatar(enabled);
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatPillSlotInfo));
        LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatMarquee), null, LumineChatConfig.getChatTitleMarquee(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setChatTitleMarquee(enabled);
            }
        });
        LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatUnreadBadge), null, LumineChatConfig.getUnreadBadge(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setUnreadBadge(enabled);
                rebuild(160);
            }
        });
        if (LumineChatConfig.getUnreadBadge()) {
            LumineCards.toggleRow(context, headerCard, LumineStrings.get(R.string.LumineChatUnreadBadgeIos), null, LumineChatConfig.getUnreadBadgeIos(), new LumineCards.OnToggle() {
                @Override
                public void onToggle(boolean enabled) {
                    LumineChatConfig.setUnreadBadgeIos(enabled);
                }
            });
        }
        LumineCards.info(context, list, LumineChatConfig.getUnreadBadge() ? LumineStrings.get(R.string.LumineChatUnreadBadgeIosInfo) : LumineStrings.get(R.string.LumineChatUnreadBadgeInfo));
    }

    private TextView styleValue;

    private String styleName(int style) {
        if (style == LumineActionButton.NEUTRAL) {
            return LumineStrings.get(R.string.LumineChatStyleNeutral);
        } else if (style == LumineActionButton.WHITE) {
            return LumineStrings.get(R.string.LumineChatStyleWhite);
        }
        return LumineStrings.get(R.string.LumineChatStyleAccent);
    }

    private void showStyleDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final int[] styles = {LumineActionButton.ACCENT, LumineActionButton.NEUTRAL, LumineActionButton.WHITE};
        final CharSequence[] names = new CharSequence[styles.length];
        for (int a = 0; a < styles.length; a++) {
            names[a] = styleName(styles[a]);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LumineStrings.get(R.string.LumineChatActionButton));
        builder.setItems(names, (dialog, which) -> {
            LumineChatConfig.setActionButtonStyle(styles[which]);
            LumineCards.setValue(styleValue, styleName(styles[which]), 0);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }
}
