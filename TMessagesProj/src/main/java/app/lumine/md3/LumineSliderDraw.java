/*
 * luminegram - Material 3 slider drawing.
 * Called from the start of SeekBarView.onDraw for plain sliders. Active and
 * inactive segments with a 7dp gap around a thin handle, rounded 8dp at the
 * outer ends and 3dp at the gap, track 13dp, handle 4dp wide and 5dp taller
 * on each side. Thumb position, touch handling and value mapping stay stock;
 * only the look changes. Colors are the ones the slider was given by the
 * theme. Returns false when the stock drawing must run (two sided, buffered,
 * steps, timestamps, disabled).
 */
package app.lumine.md3;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public final class LumineSliderDraw {

    private static final float TRACK_DP = 13f;
    private static final float GAP_DP = 7f;
    private static final float HANDLE_WIDTH_DP = 4f;
    private static final float HANDLE_EXTRA_DP = 5f;
    private static final float OUTER_RADIUS_DP = 8f;
    private static final float INNER_RADIUS_DP = 3f;

    private static final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path PATH = new Path();
    private static final RectF RECT = new RectF();
    private static final float[] RADII = new float[8];

    private LumineSliderDraw() {
    }

    public static boolean draw(Canvas canvas, int width, int height, int thumbX, int selectorWidth, boolean plain, int activeColor, int inactiveColor) {
        if (!plain || width <= 0 || height <= 0 || !LumineMd3.sliderEnabled()) {
            return false;
        }
        final float scale = Math.min(1f, height / (float) dp(TRACK_DP + 2 * HANDLE_EXTRA_DP));
        final float trackHeight = dp(TRACK_DP) * scale;
        final float gap = dp(GAP_DP) * scale;
        final float handleWidth = dp(HANDLE_WIDTH_DP) * scale;
        final float handleHeight = trackHeight + 2 * dp(HANDLE_EXTRA_DP) * scale;
        final float outer = dp(OUTER_RADIUS_DP) * scale;
        final float inner = dp(INNER_RADIUS_DP) * scale;

        final float centerY = height / 2f;
        final float top = centerY - trackHeight / 2f;
        final float bottom = centerY + trackHeight / 2f;
        final float left = selectorWidth / 2f;
        final float right = width - selectorWidth / 2f;
        final float cx = thumbX + selectorWidth / 2f;

        PAINT.setColor(activeColor);
        segment(canvas, left, top, cx - gap, bottom, outer, inner);

        PAINT.setColor(inactiveColor);
        segment(canvas, cx + gap, top, right, bottom, inner, outer);

        PAINT.setColor(activeColor);
        RECT.set(cx - handleWidth / 2f, centerY - handleHeight / 2f, cx + handleWidth / 2f, centerY + handleHeight / 2f);
        canvas.drawRoundRect(RECT, handleWidth / 2f, handleWidth / 2f, PAINT);
        return true;
    }

    private static void segment(Canvas canvas, float l, float t, float r, float b, float leftRadius, float rightRadius) {
        if (r - l <= 0f) {
            return;
        }
        final float limit = Math.min((r - l) / 2f, (b - t) / 2f);
        final float lr = Math.min(leftRadius, limit);
        final float rr = Math.min(rightRadius, limit);
        RADII[0] = lr;
        RADII[1] = lr;
        RADII[2] = rr;
        RADII[3] = rr;
        RADII[4] = rr;
        RADII[5] = rr;
        RADII[6] = lr;
        RADII[7] = lr;
        RECT.set(l, t, r, b);
        PATH.reset();
        PATH.addRoundRect(RECT, RADII, Path.Direction.CW);
        canvas.drawPath(PATH, PAINT);
    }
}
