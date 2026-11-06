// ======================================================
// luminegram - attach icon swap with scale and fade
// ======================================================

package app.lumine.chats;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.ui.Components.CubicBezierInterpolator;

public class AttachSwapDrawable extends Drawable {

    private static final long DURATION = 650;

    private final Drawable first;
    private final Drawable second;
    private final Rect childBounds = new Rect();
    private ValueAnimator animator;
    private float progress;
    private int alpha = 255;

    public AttachSwapDrawable(Drawable first, Drawable second) {
        this.first = first.mutate();
        this.second = second.mutate();
    }

    public void setMore(boolean more, boolean animate) {
        final float target = more ? 1f : 0f;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (!animate || progress == target) {
            progress = target;
            invalidateSelf();
            return;
        }
        final ValueAnimator anim = ValueAnimator.ofFloat(progress, target);
        anim.setDuration(Math.max(1L, Math.round(DURATION * Math.abs(target - progress))));
        anim.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidateSelf();
        });
        animator = anim;
        anim.start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final float out = ease(1f - progress / 0.6f);
        final float in = ease((progress - 0.4f) / 0.6f);
        drawChild(canvas, first, out);
        drawChild(canvas, second, in);
    }

    private static float ease(float v) {
        return CubicBezierInterpolator.EASE_BOTH.getInterpolation(Math.max(0f, Math.min(1f, v)));
    }

    private void drawChild(Canvas canvas, Drawable child, float f) {
        if (f <= 0f) {
            return;
        }
        final Rect bounds = getBounds();
        final int w = child.getIntrinsicWidth() > 0 ? child.getIntrinsicWidth() : bounds.width();
        final int h = child.getIntrinsicHeight() > 0 ? child.getIntrinsicHeight() : bounds.height();
        final int cx = bounds.centerX();
        final int cy = bounds.centerY();
        childBounds.set(cx - w / 2, cy - h / 2, cx - w / 2 + w, cy - h / 2 + h);
        child.setBounds(childBounds);
        child.setAlpha(Math.round(alpha * f));
        canvas.save();
        canvas.scale(f, f, cx, cy);
        child.draw(canvas);
        canvas.restore();
    }

    @Override
    public int getIntrinsicWidth() {
        return Math.max(first.getIntrinsicWidth(), second.getIntrinsicWidth());
    }

    @Override
    public int getIntrinsicHeight() {
        return Math.max(first.getIntrinsicHeight(), second.getIntrinsicHeight());
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        first.setColorFilter(colorFilter);
        second.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
