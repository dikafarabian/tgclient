/*
 * luminegram - Material 3 tonal card.
 * Rounded container with a press bounce: scales down to 0.96 while pressed and
 * stays down for at least 100 ms so a quick tap is still visible.
 */
package app.lumine.md3;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class LumineTonalCard extends FrameLayout {

    private static final float PRESSED_SCALE = 0.96f;
    private static final long MIN_PRESS_MS = 100;
    private static final long PRESS_MS = 100;
    private static final long RELEASE_MS = 200;

    private final GradientDrawable background = new GradientDrawable();
    private final FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
    private boolean bounced;
    private long pressedAt;

    public LumineTonalCard(Context context, int radiusDp, int color) {
        super(context);
        background.setCornerRadius(dp(radiusDp));
        background.setColor(color);
        setBackground(background);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);
    }

    public void setCardColor(int color) {
        background.setColor(color);
    }

    public void setRadiusDp(int radiusDp) {
        background.setCornerRadius(dp(radiusDp));
        invalidateOutline();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isClickable() || isLongClickable()) {
                    bounce(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (bounced) {
                    bounce(false);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void bounce(boolean down) {
        animate().cancel();
        if (down) {
            bounced = true;
            pressedAt = SystemClock.uptimeMillis();
            animate().scaleX(PRESSED_SCALE).scaleY(PRESSED_SCALE).setStartDelay(0).setDuration(PRESS_MS).setInterpolator(interpolator).start();
        } else {
            bounced = false;
            final long remaining = Math.max(0, MIN_PRESS_MS - (SystemClock.uptimeMillis() - pressedAt));
            animate().scaleX(1f).scaleY(1f).setStartDelay(remaining).setDuration(RELEASE_MS).setInterpolator(interpolator).start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animate().cancel();
        bounced = false;
        setScaleX(1f);
        setScaleY(1f);
    }
}
