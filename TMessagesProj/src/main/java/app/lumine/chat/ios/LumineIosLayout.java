/*
 * luminegram - iOS input layout controller.
 * One instance per enter view. After every layout of the enter view it fixes
 * the margins of the field and the emoji button and reports the screen
 * position of the two side bubbles to ChatInputViewsContainer.
 */
package app.lumine.chat.ios;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.view.View;
import android.widget.FrameLayout;

import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.chat.ChatInputViewsContainer;

public final class LumineIosLayout {

    private final ChatActivityEnterView enterView;
    private final ChatActivity fragment;
    private final FrameLayout attachBubble;
    private final FrameLayout sendContainer;
    private final View emojiButton;
    private final int[] bubbleLoc = new int[2];
    private final int[] containerLoc = new int[2];

    private LumineIosLayout(ChatActivityEnterView enterView, ChatActivity fragment, FrameLayout attachBubble, FrameLayout sendContainer, View emojiButton) {
        this.enterView = enterView;
        this.fragment = fragment;
        this.attachBubble = attachBubble;
        this.sendContainer = sendContainer;
        this.emojiButton = emojiButton;
    }

    public static void install(ChatActivityEnterView enterView, ChatActivity fragment, FrameLayout attachBubble, FrameLayout sendContainer, View emojiButton) {
        if (attachBubble == null || sendContainer == null || emojiButton == null || fragment == null) {
            return;
        }
        final LumineIosLayout layout = new LumineIosLayout(enterView, fragment, attachBubble, sendContainer, emojiButton);
        enterView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> layout.update());
    }

    private void update() {
        final ChatInputViewsContainer container = fragment.chatInputViewsContainer;
        if (container == null || !container.sideBubbles.ready() || attachBubble.getWidth() == 0 || sendContainer.getWidth() == 0) {
            return;
        }
        updateField();
        container.setInputBubbleOffsets(dp(50), dp(50));
        container.getLocationInWindow(containerLoc);
        attachBubble.getLocationInWindow(bubbleLoc);
        final int left = bubbleLoc[0] - containerLoc[0];
        container.sideBubbles.setLeftBounds(left, left + attachBubble.getWidth());
        sendContainer.getLocationInWindow(bubbleLoc);
        final int right = bubbleLoc[0] - containerLoc[0] + sendContainer.getWidth();
        container.sideBubbles.setRightBounds(right - dp(ChatActivityEnterView.DEFAULT_HEIGHT), right);
        container.sideBubbles.setAlpha(255);
    }

    private void updateField() {
        final FrameLayout.LayoutParams emojiLp = (FrameLayout.LayoutParams) emojiButton.getLayoutParams();
        final int emojiRight = dp(3);
        if ((emojiLp.gravity & android.view.Gravity.RIGHT) == android.view.Gravity.RIGHT && emojiLp.rightMargin != emojiRight) {
            emojiLp.rightMargin = emojiRight;
            emojiButton.setLayoutParams(emojiLp);
        }
        if (enterView.messageEditText == null) {
            return;
        }
        final FrameLayout.LayoutParams editLp = (FrameLayout.LayoutParams) enterView.messageEditText.getLayoutParams();
        final int editRight = dp(49);
        if (editLp.rightMargin != editRight) {
            editLp.rightMargin = editRight;
            enterView.messageEditText.setLayoutParams(editLp);
        }
    }
}
