/*
 * luminegram - side bubbles of the iOS input.
 * The two round glass backgrounds behind the attach button (left) and the
 * send or record button (right). ChatInputViewsContainer owns one instance and
 * only calls draw and updateColors on it.
 */
package app.lumine.chat.ios;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.chat.ChatInputViewsContainer;

public final class LumineSideBubbles {

    public static final int SIZE = 44;

    private final View host;
    private final Rect leftRect = new Rect();
    private final Rect rightRect = new Rect();
    private BlurredBackgroundDrawable left;
    private BlurredBackgroundDrawable right;
    private boolean leftVisible;
    private boolean rightVisible;
    private int alpha = 255;

    public LumineSideBubbles(View host) {
        this.host = host;
    }

    public void setDrawables(BlurredBackgroundDrawable leftDrawable, BlurredBackgroundDrawable rightDrawable) {
        left = leftDrawable;
        right = rightDrawable;
        left.setRadius(dp(ChatInputViewsContainer.INPUT_BUBBLE_RADIUS));
        right.setRadius(dp(ChatInputViewsContainer.INPUT_BUBBLE_RADIUS));
    }

    public boolean ready() {
        return left != null && right != null;
    }

    public void setLeftBounds(int start, int end) {
        leftRect.left = start;
        leftRect.right = end;
        leftVisible = true;
        host.invalidate();
    }

    public void setRightBounds(int start, int end) {
        rightRect.left = start;
        rightRect.right = end;
        rightVisible = true;
        host.invalidate();
    }

    public void setAlpha(int value) {
        if (alpha != value) {
            alpha = value;
            host.invalidate();
        }
    }

    public void updateColors() {
        if (left != null) {
            left.updateColors();
        }
        if (right != null) {
            right.updateColors();
        }
    }

    public void draw(Canvas canvas, int bottom) {
        if (left != null && leftVisible) {
            drawOne(canvas, left, leftRect, bottom);
        }
        if (right != null && rightVisible) {
            drawOne(canvas, right, rightRect, bottom);
        }
    }

    private void drawOne(Canvas canvas, BlurredBackgroundDrawable drawable, Rect rect, int bottom) {
        rect.top = bottom - dp(SIZE);
        rect.bottom = bottom;
        drawable.setBounds(rect);
        drawable.setAlpha(alpha);
        drawable.draw(canvas);
    }
}
