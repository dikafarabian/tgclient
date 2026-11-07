/*
 * luminegram - bottom navigation bar of the luminegram settings.
 * Floating pill in the geometry of the main tab bar of the app: 56dp pill,
 * 8dp margin, 48dp tabs. Only the selected tab shows its label and sits in a
 * tonal capsule; the other tabs are icon only. Tab widths follow the
 * selection animation (250 ms, fast-out-slow-in). Own view, no dependency on
 * the main tabs.
 */
package app.lumine.settings;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

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
    private final float[] fullWidth;
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
    private float availableWidth;
    private int metricsWidth = -1;

    public LumineTabBar(Context context, int[] iconRes, String[] labels) {
        super(context);
        this.labels = labels;
        icons = new Drawable[iconRes.length];
        tabLeft = new float[iconRes.length];
        tabWidth = new float[iconRes.length];
        fullWidth = new float[iconRes.length];
        for (int i = 0; i < iconRes.length; i++) {
            icons[i] = ContextCompat.getDrawable(context, iconRes[i]).mutate();
        }
        textPaint.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_EXTRA_BOLD));
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
        animator.setDuration(250);
        animator.setInterpolator(new PathInterpolator(0.4f, 0f, 0.2f, 1f));
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
        metricsWidth = -1;
    }

    private void computeMetrics(int width) {
        final int count = icons.length;
        final float padding = dp(4);
        final float gap = dp(20);
        final float minTab = dp(48);
        availableWidth = Math.min(width - dp(16), dp(328)) - padding * 2;

        textPaint.setTextSize(dp(TEXT_SIZE_DP));
        float maxLabel = 0;
        for (String label : labels) {
            maxLabel = Math.max(maxLabel, textPaint.measureText(label));
        }
        final float room = availableWidth - (count - 1) * minTab - gap;
        if (maxLabel > room && maxLabel > 0) {
            textPaint.setTextSize(dp(Math.max(TEXT_SIZE_MIN_DP, TEXT_SIZE_DP * room / maxLabel)));
        }
        for (int i = 0; i < count; i++) {
            fullWidth[i] = Math.max(minTab, textPaint.measureText(labels[i]) + gap);
        }
        metricsWidth = width;
    }

    private float weight(int i) {
        return Math.max(0f, Math.min(1f, 1f - Math.abs(position - i)));
    }

    private void computeLayout(int width) {
        if (metricsWidth != width) {
            computeMetrics(width);
        }
        final int count = icons.length;
        final float padding = dp(4);
        final float minTab = dp(48);

        float total = 0;
        for (int i = 0; i < count; i++) {
            tabWidth[i] = minTab + (fullWidth[i] - minTab) * weight(i);
            total += tabWidth[i];
        }
        if (total > availableWidth) {
            final float scale = availableWidth / total;
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        computeLayout(getWidth());

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
            final float t = weight(i);
            final float cx = tabLeft[i] + tabWidth[i] / 2f;

            if (t > 0f) {
                capsulePaint.setColor(Theme.multAlpha(selectedColor, 0.09f * t));
                rect.set(tabLeft[i], tabTop, tabLeft[i] + tabWidth[i], tabTop + tabHeight);
                final float r = Math.min(rect.width(), rect.height()) / 2f;
                canvas.drawRoundRect(rect, r, r, capsulePaint);
            }

            final Drawable icon = icons[i];
            final int half = dp(12);
            final int iconTop = (int) (tabTop + dp(12) - dp(8) * t);
            icon.setBounds((int) cx - half, iconTop, (int) cx + half, iconTop + dp(24));
            icon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(normalColor, selectedColor, t), PorterDuff.Mode.SRC_IN));
            icon.draw(canvas);

            if (t > 0.02f) {
                final int color = ColorUtils.setAlphaComponent(selectedTextColor, (int) (Color.alpha(selectedTextColor) * t));
                textPaint.setColor(color);
                final CharSequence text = TextUtils.ellipsize(labels[i], textPaint, fullWidth[i] - dp(4), TextUtils.TruncateAt.END);
                final float textWidth = textPaint.measureText(text, 0, text.length());
                final float baseline = tabTop + dp(28.33f) - textPaint.getFontMetrics().top;
                canvas.save();
                canvas.clipRect(tabLeft[i], tabTop, tabLeft[i] + tabWidth[i], tabTop + tabHeight);
                canvas.drawText(text, 0, text.length(), cx - textWidth / 2f, baseline, textPaint);
                canvas.restore();
            }
        }
    }

    private int indexAt(float x, float y) {
        if (metricsWidth < 0 || y < dp(8) || y > dp(64)) {
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
