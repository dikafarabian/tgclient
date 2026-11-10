/*
 * luminegram - iOS style message input.
 * All logic of the iOS input lives here. ChatActivityEnterView only asks
 * isIOSInputStyle() and, in iOS mode, calls into this class right after the
 * stock statement it changes. Helpers take views as parameters so no field of
 * the enter view has to be opened.
 */
package app.lumine.chat.ios;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import app.lumine.chat.LumineChatConfig;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.chat.ChatInputViewsContainer;

public final class LumineIosInput {

    private LumineIosInput() {
    }

    public static boolean active(ChatActivity fragment) {
        if (!LumineChatConfig.getIosInput() || fragment == null || fragment.isInPreviewMode()) {
            return false;
        }
        final TLRPC.Chat chat = fragment.getCurrentChat();
        if (chat == null || fragment.getCurrentEncryptedChat() != null) {
            return true;
        }
        final boolean broadcast = (chat instanceof TLRPC.TL_channel || chat instanceof TLRPC.TL_channelForbidden) && !chat.megagroup && !chat.gigagroup;
        return !broadcast || ChatObject.hasAdminRights(chat);
    }

    public static int containerLeftInset(ChatActivity fragment) {
        if (fragment == null) {
            return 0;
        }
        final TLRPC.User user = fragment.getCurrentUser();
        if (user != null && !user.bot || fragment.getChatMode() == ChatActivity.MODE_WELCOME_MESSAGES) {
            return 15;
        }
        if (user != null && user.bot) {
            return 8;
        }
        if (fragment.getCurrentChat() == null) {
            return 0;
        }
        return 15;
    }

    public static void frameInsets(FrameLayout frame, ChatActivity fragment) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) frame.getLayoutParams();
        lp.leftMargin = dp(containerLeftInset(fragment));
        lp.rightMargin = dp(48);
        frame.setLayoutParams(lp);
    }

    public static void leftMargin(View view, float marginDp) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.leftMargin = dp(marginDp);
        view.setLayoutParams(lp);
    }

    public static void emojiToRight(View emojiButton) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) emojiButton.getLayoutParams();
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.leftMargin = 0;
        lp.rightMargin = dp(3);
        emojiButton.setLayoutParams(lp);
    }

    public static void attachLayoutToRight(View attachLayout) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) attachLayout.getLayoutParams();
        lp.rightMargin = dp(2);
        attachLayout.setLayoutParams(lp);
    }

    public static FrameLayout moveAttach(Context context, FrameLayout from, FrameLayout to, ImageView attachButton, Theme.ResourcesProvider resourcesProvider) {
        from.removeView(attachButton);
        attachButton.setImageResource(R.drawable.lumine_input_attach);
        attachButton.setBackground(null);
        attachButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
        final FrameLayout bubble = new FrameLayout(context);
        bubble.addView(attachButton, LayoutHelper.createFrame(38, 38, Gravity.CENTER));
        to.addView(bubble, LayoutHelper.createFrame(ChatActivityEnterView.DEFAULT_HEIGHT, ChatActivityEnterView.DEFAULT_HEIGHT, Gravity.BOTTOM | Gravity.LEFT, 0, 0, 0, 0));
        return bubble;
    }

    private static final int[] bubbleLoc = new int[2];
    private static final int[] containerLoc = new int[2];

    public static void updateBubbles(ChatInputViewsContainer container, FrameLayout attachBubble, FrameLayout sendContainer, View emojiButton, EditText edit,
                                     boolean hasBotButton, float progress, int sendWidth, Runnable retry) {
        if (container == null || attachBubble == null || sendContainer == null || !container.sideBubbles.ready()) {
            return;
        }
        if (progress <= 0f) {
            container.sideBubbles.setAlpha(0);
            container.setInputBubbleOffsets(0, 0);
            return;
        }
        if (attachBubble.getWidth() == 0 || sendContainer.getWidth() == 0) {
            if (!attachBubble.isAttachedToWindow() || attachBubble.isShown()) {
                attachBubble.post(retry);
            }
            return;
        }
        final int extra = Math.max(0, sendWidth - dp(ChatActivityEnterView.DEFAULT_HEIGHT));
        container.setInputBubbleOffsets(Math.round(dp(50) * progress), Math.round((dp(50) + extra) * progress));
        container.getLocationInWindow(containerLoc);
        attachBubble.getLocationInWindow(bubbleLoc);
        final int left = bubbleLoc[0] - containerLoc[0];
        container.sideBubbles.setLeftBounds(left, left + attachBubble.getWidth());
        sendContainer.getLocationInWindow(bubbleLoc);
        final int right = bubbleLoc[0] - containerLoc[0] + sendContainer.getWidth();
        container.sideBubbles.setRightBounds(right - sendWidth, right);
        container.sideBubbles.setAlpha((int) (progress * 255));
        emojiLayout(emojiButton, edit, hasBotButton, sendWidth);
    }

    public static void emojiLayout(View emojiButton, EditText edit, boolean hasBotButton, int sendWidth) {
        if (emojiButton == null || edit == null) {
            return;
        }
        final FrameLayout.LayoutParams emojiLp = (FrameLayout.LayoutParams) emojiButton.getLayoutParams();
        if (emojiLp == null || (emojiLp.gravity & Gravity.RIGHT) != Gravity.RIGHT) {
            return;
        }
        final int extra = Math.max(0, sendWidth - dp(ChatActivityEnterView.DEFAULT_HEIGHT));
        if (emojiLp.rightMargin != dp(3)) {
            emojiLp.rightMargin = dp(3);
            emojiButton.setLayoutParams(emojiLp);
        }
        emojiButton.setTranslationX(-extra);
        final FrameLayout.LayoutParams editLp = (FrameLayout.LayoutParams) edit.getLayoutParams();
        final int editRight = dp(49) + extra + (hasBotButton ? dp(20) : 0);
        if (editLp.rightMargin != editRight) {
            editLp.rightMargin = editRight;
            edit.setLayoutParams(editLp);
        }
    }

    public static void linearRightMargin(View view, int marginDp) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.rightMargin = dp(marginDp);
        view.setLayoutParams(lp);
    }

    public static void scheduledToAttachLayout(FrameLayout from, LinearLayout to, View scheduledButton) {
        if (to == null) {
            return;
        }
        from.removeView(scheduledButton);
        to.addView(scheduledButton, 0, LayoutHelper.createLinear(ChatActivityEnterView.DEFAULT_HEIGHT, ChatActivityEnterView.DEFAULT_HEIGHT, 0, 0, 30, 0));
    }

    public static void scheduledMargin(View scheduledButton, boolean botShown) {
        if (scheduledButton == null || !(scheduledButton.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            return;
        }
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) scheduledButton.getLayoutParams();
        final int margin = botShown ? -dp(10) : dp(30);
        if (lp.rightMargin != margin) {
            lp.rightMargin = margin;
            scheduledButton.setLayoutParams(lp);
        }
    }

    public static void botToEnd(LinearLayout layout, View botButton) {
        layout.removeView(botButton);
        layout.addView(botButton, LayoutHelper.createLinear(ChatActivityEnterView.DEFAULT_HEIGHT, ChatActivityEnterView.DEFAULT_HEIGHT, 0, 0, 30, 0));
    }
}
