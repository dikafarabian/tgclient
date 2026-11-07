/*
 * luminegram - bottom navigation bar of the luminegram settings.
 * Floating pill in the geometry of the main tab bar of the app: 56dp pill,
 * 8dp margin, 48dp tabs, capsule behind the selected tab, tab widths follow
 * the label width. Own view, no dependency on the main tabs.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

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

import me.vkryl.android.AnimatorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;

public class LumineTabBar extends View {

    public interface Listener {
        void onTabSelected(int index);
    }

    private static final float TEXT_SIZE_DP = 12f;
    private static final float TEXT_SIZE_MIN_DP = 9f;

    private final Drawable[] icons;
    private final String[] labels;
    private final float[] tabLeft;
    private final float[] tabWidth;
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint capsulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private Listener listener;
    private int selected;
    private float position;
    private ValueAnimator animator;
    private int pressedIndex = -1;
    private float pillLeft;
    private float pillRight;
    private int layoutWidth = -1;

    public LumineTabBar(Context context, int[] iconRes, String[] labels) {
        super(context);
        this.labels = labels;
        icons = new Drawable[iconRes.length];
        tabLeft = new float[iconRes.length];
        tabWidth = new float[iconRes.length];
        for (int i = 0; i < iconRes.length; i++) {
            icons[i] = ContextCompat.getDrawable(context, iconRes[i]).mutate();
        }
        textPaint.setTypeface(AndroidUtilities.bold());
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpf2(0.4f));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
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
        animator = ValueAnimator.ofFloat(position, index);
        animator.setDuration(320);
        animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animator.addUpdateListener(a -> {
            position = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(72));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = -1;
    }

    private void computeLayout(int width) {
        final int count = icons.length;
        final float padding = dp(4);
        final float gap = dp(20);
        final float minTab = dp(48);
        final float available = Math.min(width - dp(16), dp(328)) - padding * 2;

        textPaint.setTypeface(AndroidUtilities.bold());
        textPaint.setTextSize(dp(TEXT_SIZE_DP));
        float textSum = 0;
        for (String label : labels) {
            textSum += textPaint.measureText(label);
        }
        if (textSum + gap * count > available) {
            final float size = Math.max(TEXT_SIZE_MIN_DP, TEXT_SIZE_DP * (available - gap * count) / textSum);
            textPaint.setTextSize(dp(size));
        }

        float total = 0;
        for (int i = 0; i < count; i++) {
            tabWidth[i] = Math.max(minTab, textPaint.measureText(labels[i]) + gap);
            total += tabWidth[i];
        }
        if (total > available) {
            final float scale = available / total;
            total = 0;
            for (int i = 0; i < count; i++) {
                tabWidth[i] *= scale;
                total += tabWidth[i];
            }
        }

        final float pillWidth = total + padding * 2;
        pillLeft = (width - pillWidth) / 2f;
        pillRight = pillLeft + pillWidth;
        float x = pillLeft + padding;
        for (int i = 0; i < count; i++) {
            tabLeft[i] = x;
            x += tabWidth[i];
        }
        layoutWidth = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getWidth();
        if (layoutWidth != width) {
            computeLayout(width);
        }

        final boolean dark = Theme.isCurrentThemeDark();
        final int selectedColor = Theme.getColor(Theme.key_glass_tabSelected);
        final int selectedTextColor = Theme.getColor(Theme.key_glass_tabSelectedText);
        final int normalColor = Theme.getColor(Theme.key_glass_tabUnselected);

        final float pillTop = dp(8);
        final float pillBottom = pillTop + dp(56);
        final float radius = dp(28);

        pillPaint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_glass_targetMainTabs), 0xF2));
        pillPaint.setShadowLayer(dpf2(2.667f), 0, dpf2(0.85f), dark ? 0x04FFFFFF : 0x20000000);
        rect.set(pillLeft, pillTop, pillRight, pillBottom);
        canvas.drawRoundRect(rect, radius, radius, pillPaint);
        strokePaint.setColor(dark ? 0x11FFFFFF : 0x14000000);
        canvas.drawRoundRect(rect, radius, radius, strokePaint);

        final float tabTop = pillTop + dp(4);
        final float tabHeight = dp(48);

        for (int i = 0; i < icons.length; i++) {
            final float t = Math.max(0f, Math.min(1f, 1f - Math.abs(position - i)));
            final float cx = tabLeft[i] + tabWidth[i] / 2f;

            if (t > 0f) {
                final float alpha = AnimatorUtils.DECELERATE_INTERPOLATOR.getInterpolation(t);
                final float scale = 0.6f + 0.4f * t;
                capsulePaint.setColor(Theme.multAlpha(selectedColor, 0.09f * alpha));
                rect.set(tabLeft[i], tabTop, tabLeft[i] + tabWidth[i], tabTop + tabHeight);
                final float r = Math.min(rect.width(), rect.height()) / 2f;
                canvas.save();
                canvas.scale(scale, scale, rect.centerX(), rect.centerY());
                canvas.drawRoundRect(rect, r, r, capsulePaint);
                canvas.restore();
            }

            final Drawable icon = icons[i];
            final int half = dp(12);
            icon.setBounds((int) cx - half, (int) (tabTop + dp(4)), (int) cx + half, (int) (tabTop + dp(28)));
            icon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(normalColor, selectedColor, t), PorterDuff.Mode.SRC_IN));
            icon.draw(canvas);

            textPaint.setTypeface(t >= 0.5f ? AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_EXTRA_BOLD) : AndroidUtilities.bold());
            textPaint.setColor(ColorUtils.blendARGB(normalColor, selectedTextColor, t));
            final CharSequence text = TextUtils.ellipsize(labels[i], textPaint, tabWidth[i] - dp(4), TextUtils.TruncateAt.END);
            final float textWidth = textPaint.measureText(text, 0, text.length());
            final float baseline = tabTop + dp(28.33f) - textPaint.getFontMetrics().top;
            canvas.drawText(text, 0, text.length(), cx - textWidth / 2f, baseline, textPaint);
        }
    }

    private int indexAt(float x, float y) {
        if (layoutWidth < 0 || y < dp(8) || y > dp(64)) {
            return -1;
        }
        for (int i = 0; i < icons.length; i++) {
            if (x >= tabLeft[i] && x < tabLeft[i] + tabWidth[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedIndex = indexAt(event.getX(), event.getY());
                return pressedIndex >= 0;
            case MotionEvent.ACTION_UP:
                final int index = indexAt(event.getX(), event.getY());
                if (index >= 0 && index == pressedIndex && index != selected) {
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
