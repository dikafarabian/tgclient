/*
 * luminegram - Material 3 switch drawing.
 * Called from the start of Switch.onDraw. Draws a capsule track and a round
 * thumb that grows from 16dp to 24dp with the progress of the switch. The
 * 52x32dp design is scaled to fit the view as laid out by the caller, so the
 * size and the touch area stay stock. Returns false when the stock drawing
 * must run (disabled, icon switches, color override animation).
 */
package app.lumine.md3;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.graphics.ColorUtils;

public final class LumineSwitchDraw {

    private static final float TRACK_WIDTH_DP = 52f;
    private static final float TRACK_HEIGHT_DP = 32f;
    private static final float THUMB_OFF_DP = 16f;
    private static final float THUMB_ON_DP = 24f;
    private static final float OUTLINE_DP = 2f;

    private static final Paint TRACK = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint THUMB = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF RECT = new RectF();

    static {
        STROKE.setStyle(Paint.Style.STROKE);
    }

    private LumineSwitchDraw() {
    }

    public static boolean draw(Canvas canvas, int width, int height, float progress, boolean plain) {
        if (!plain || width <= 0 || height <= 0 || !LumineMd3.switchEnabled()) {
            return false;
        }
        final float scale = Math.min(1f, Math.min(width / (float) dp(TRACK_WIDTH_DP), height / (float) dp(TRACK_HEIGHT_DP)));
        final float trackWidth = dp(TRACK_WIDTH_DP) * scale;
        final float trackHeight = dp(TRACK_HEIGHT_DP) * scale;
        final float left = (width - trackWidth) / 2f;
        final float top = (height - trackHeight) / 2f;
        final float t = Math.max(0f, Math.min(1f, progress));

        final LumineMd3.Roles roles = LumineMd3.roles();

        TRACK.setColor(ColorUtils.blendARGB(roles.surfaceContainerHigh, roles.primary, t));
        RECT.set(left, top, left + trackWidth, top + trackHeight);
        canvas.drawRoundRect(RECT, trackHeight / 2f, trackHeight / 2f, TRACK);

        if (t < 1f) {
            final float stroke = dpf2(OUTLINE_DP) * scale;
            STROKE.setStrokeWidth(stroke);
            STROKE.setColor(ColorUtils.setAlphaComponent(roles.outline, Math.round(255 * (1f - t))));
            RECT.inset(stroke / 2f, stroke / 2f);
            final float radius = (trackHeight - stroke) / 2f;
            canvas.drawRoundRect(RECT, radius, radius, STROKE);
        }

        final float thumb = dpf2(THUMB_OFF_DP + (THUMB_ON_DP - THUMB_OFF_DP) * t) * scale;
        final float cx = left + dpf2(16f + (TRACK_WIDTH_DP - 32f) * t) * scale;
        final float cy = top + trackHeight / 2f;
        THUMB.setColor(ColorUtils.blendARGB(roles.outline, roles.onPrimary, t));
        canvas.drawCircle(cx, cy, thumb / 2f, THUMB);
        return true;
    }
}
