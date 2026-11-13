/*
 * luminegram - unread chats counter on the header back button.
 * The badge lives on the ActionBar: ChatActivity refreshes it when the
 * dialogs unread count changes, ActionBar.drawChild lets it wrap the back
 * button drawing, and the iOS variant widens the glass back pill through
 * lumineBackPillExtra(). Numbers follow entinyGram's badge.
 */
package app.lumine.chat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;

import me.vkryl.android.animator.FactorAnimator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesStorage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;

public final class LumineBackBadge {

    private static final int MAX_SHOWN = 999;
    private static final float CHIP_HEIGHT = 18f;
    private static final float CHIP_TRIM = 9f;
    private static final float CHIP_GAP = 10f;
    private static final float ARROW_SLIDE = 5f;

    private LumineBackBadge() {
    }

    public static void refresh(ActionBar bar, int account) {
        if (bar == null) {
            return;
        }
        if (!LumineChatConfig.getUnreadBadge()) {
            bar.lumineBackBadge = null;
            return;
        }
        badgeOf(bar).setCount(MessagesStorage.getInstance(account).getMainUnreadCount(), true);
    }

    private static BackBadge badgeOf(ActionBar bar) {
        if (bar.lumineBackBadge == null) {
            bar.lumineBackBadge = new BackBadge(bar);
        }
        return bar.lumineBackBadge;
    }

    public static final class BackBadge implements FactorAnimator.Target {

        private static final int PRESENCE = 0;
        private static final int WIDTH = 1;

        private final ActionBar bar;
        private String label = "";
        private int digits = 1;
        private int count;
        private boolean wanted;

        private final FactorAnimator presence = new FactorAnimator(PRESENCE, this, CubicBezierInterpolator.EASE_OUT_QUINT, 320);
        private final FactorAnimator chipWidth = new FactorAnimator(WIDTH, this, CubicBezierInterpolator.EASE_OUT_QUINT, 320);
        private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Paint chipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF chip = new RectF();

        private BackBadge(ActionBar actionBar) {
            bar = actionBar;
            textPaint.setTypeface(AndroidUtilities.bold());
        }

        public void setCount(int value, boolean animated) {
            count = Math.max(0, Math.min(value, MAX_SHOWN));
            if (count > 0) {
                final String text = Integer.toString(count);
                if (!text.equals(label)) {
                    label = text;
                    digits = text.length();
                }
            }
            final float targetWidth = chipWidthFor(digits);
            if (!animated || chipWidth.getFactor() == 0f) {
                chipWidth.forceFactor(targetWidth);
            } else {
                chipWidth.animateTo(targetWidth);
            }
            syncWanted(!animated);
            bar.invalidate();
        }

        public boolean isMoving() {
            return presence.isAnimating() || chipWidth.isAnimating();
        }

        public int pillExtra() {
            if (!iosShape()) {
                return 0;
            }
            syncWanted(false);
            return Math.max(0, (int) ((chipWidth.getFactor() - AndroidUtilities.dpf2(CHIP_TRIM)) * presence.getFactor()));
        }

        public void beforeBack(Canvas canvas) {
            syncWanted(false);
            applyTail();
            canvas.save();
            if (iosShape()) {
                canvas.translate(-AndroidUtilities.dpf2(ARROW_SLIDE) * reveal(), 0f);
            }
        }

        public void afterBack(Canvas canvas, View back) {
            canvas.restore();
            final float fade = reveal();
            if (fade <= 0.01f || label.isEmpty()) {
                return;
            }
            final float alpha = back.getAlpha() * fade;
            final float scale = 0.7f + 0.3f * fade;
            final float cy = back.getY() + back.getHeight() / 2f;
            final float cx = back.getX() + back.getWidth() / 2f;
            if (iosShape()) {
                textPaint.setTextSize(AndroidUtilities.dpf2(12f));
                final float width = Math.max(AndroidUtilities.dpf2(CHIP_HEIGHT),
                        Math.min(chipWidth.getFactor() * presence.getFactor() + AndroidUtilities.dpf2(CHIP_TRIM) * (1f - presence.getFactor()), chipWidth.getFactor()));
                final float left = cx - AndroidUtilities.dpf2(ARROW_SLIDE) * fade + AndroidUtilities.dpf2(CHIP_GAP);
                chip.set(left, cy - AndroidUtilities.dpf2(CHIP_HEIGHT) / 2f, left + width, cy + AndroidUtilities.dpf2(CHIP_HEIGHT) / 2f);
                drawChip(canvas, alpha, scale, Theme.key_actionBarActionModeDefault, Theme.key_actionBarActionModeDefaultIcon);
            } else {
                textPaint.setTextSize(AndroidUtilities.dpf2(10.5f));
                final float height = AndroidUtilities.dpf2(16f);
                final float width = Math.max(height, measuredLabel() + AndroidUtilities.dpf2(9f));
                final float left = cx + AndroidUtilities.dpf2(3f);
                final float top = cy - AndroidUtilities.dpf2(15f);
                chip.set(left, top, left + width, top + height);
                drawChip(canvas, alpha, scale, Theme.key_chats_unreadCounter, Theme.key_chats_unreadCounterText);
            }
        }

        @Override
        public void onFactorChanged(int id, float factor, float fraction, FactorAnimator callee) {
            bar.invalidate();
        }

        private void drawChip(Canvas canvas, float alpha, float scale, int fillKey, int textKey) {
            final int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
            chipPaint.setColor(Theme.getColor(fillKey, bar.getMenuResourcesProvider()));
            chipPaint.setAlpha(chipPaint.getAlpha() * a / 255);
            textPaint.setColor(Theme.getColor(textKey, bar.getMenuResourcesProvider()));
            textPaint.setAlpha(textPaint.getAlpha() * a / 255);
            canvas.save();
            canvas.scale(scale, scale, chip.centerX(), chip.centerY());
            final float r = chip.height() / 2f;
            canvas.drawRoundRect(chip, r, r, chipPaint);
            final float baseline = chip.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f;
            canvas.drawText(label, chip.centerX() - measuredLabel() / 2f, baseline, textPaint);
            canvas.restore();
        }

        private float measuredLabel() {
            return textPaint.measureText(label);
        }

        private float reveal() {
            if (!iosShape()) {
                return presence.getFactor();
            }
            final float smallest = chipWidthFor(1) - AndroidUtilities.dpf2(CHIP_TRIM);
            if (smallest <= 0f) {
                return presence.getFactor();
            }
            return Math.max(0f, Math.min(1f, pillExtraRaw() / smallest));
        }

        private float pillExtraRaw() {
            return Math.max(0f, (chipWidth.getFactor() - AndroidUtilities.dpf2(CHIP_TRIM)) * presence.getFactor());
        }

        private boolean iosShape() {
            return LumineChatConfig.getUnreadBadgeIos() && bar.lumineBackPillMode();
        }

        private void syncWanted(boolean force) {
            final boolean want = count > 0 && !bar.isActionModeShowed() && !bar.isSearchFieldVisible();
            if (want == wanted && !force) {
                return;
            }
            wanted = want;
            if (force) {
                presence.forceFactor(want ? 1f : 0f);
            } else {
                presence.animateTo(want ? 1f : 0f);
            }
        }

        private void applyTail() {
            final BackDrawable arrow = bar.getBackButtonDrawable() instanceof BackDrawable ? (BackDrawable) bar.getBackButtonDrawable() : null;
            if (arrow == null) {
                return;
            }
            if (arrow.lumineTail != 0f) {
                arrow.lumineTail = 0f;
                arrow.invalidateSelf();
            }
        }

        private float chipWidthFor(int length) {
            if (length <= 1) {
                return AndroidUtilities.dpf2(CHIP_HEIGHT);
            } else if (length == 2) {
                return AndroidUtilities.dpf2(24f);
            }
            return AndroidUtilities.dpf2(31f);
        }
    }
}
