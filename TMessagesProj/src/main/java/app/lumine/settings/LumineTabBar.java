/*
 * luminegram - bottom navigation bar of the luminegram settings.
 * Own view, no dependency on the main tabs of the app.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;

public class LumineTabBar extends View {

    public interface Listener {
        void onTabSelected(int index);
    }

    private final Drawable[] icons;
    private final String[] labels;
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint();
    private final RectF pillRect = new RectF();

    private Listener listener;
    private int selected;
    private float position;
    private ValueAnimator animator;
    private int pressedIndex = -1;

    public LumineTabBar(Context context, int[] iconRes, String[] labels) {
        super(context);
        this.labels = labels;
        icons = new Drawable[iconRes.length];
        for (int i = 0; i < iconRes.length; i++) {
            icons[i] = ContextCompat.getDrawable(context, iconRes[i]).mutate();
        }
        textPaint.setTextSize(dp(11));
        textPaint.setTypeface(AndroidUtilities.bold());
        textPaint.setTextAlign(Paint.Align.LEFT);
        setWillNotDraw(false);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public int getSelected() {
        return selected;
    }

    public void select(int index, boolean animated) {
        selected = index;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (!animated) {
            position = index;
            invalidate();
            return;
        }
        final float from = position;
        animator = ValueAnimator.ofFloat(from, index);
        animator.setDuration(260);
        animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animator.addUpdateListener(a -> {
            position = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(64));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int w = getWidth();
        final int count = icons.length;
        final float tabWidth = w / (float) count;

        canvas.drawColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        dividerPaint.setColor(Theme.getColor(Theme.key_divider));
        canvas.drawRect(0, 0, w, Math.max(1, dp(0.5f)), dividerPaint);

        final int selectedColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueIcon);
        final int normalColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon);

        final float pillCenter = (position + 0.5f) * tabWidth;
        pillPaint.setColor(ColorUtils.setAlphaComponent(selectedColor, 36));
        pillRect.set(pillCenter - dp(28), dp(8), pillCenter + dp(28), dp(36));
        canvas.drawRoundRect(pillRect, dp(14), dp(14), pillPaint);

        for (int i = 0; i < count; i++) {
            final float t = Math.max(0f, 1f - Math.abs(position - i));
            final int color = ColorUtils.blendARGB(normalColor, selectedColor, t);
            final float cx = (i + 0.5f) * tabWidth;

            final Drawable icon = icons[i];
            final int half = dp(12);
            icon.setBounds((int) cx - half, dp(10), (int) cx + half, dp(34));
            icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            icon.draw(canvas);

            textPaint.setColor(color);
            final CharSequence text = TextUtils.ellipsize(labels[i], textPaint, tabWidth - dp(6), TextUtils.TruncateAt.END);
            final float textWidth = textPaint.measureText(text, 0, text.length());
            canvas.drawText(text, 0, text.length(), cx - textWidth / 2f, dp(52), textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int count = icons.length;
        final int index = Math.max(0, Math.min(count - 1, (int) (event.getX() / (getWidth() / (float) count))));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedIndex = index;
                return true;
            case MotionEvent.ACTION_UP:
                if (pressedIndex == index && index != selected) {
                    select(index, true);
                    if (listener != null) {
                        listener.onTabSelected(index);
                    }
                }
                pressedIndex = -1;
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressedIndex = -1;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
