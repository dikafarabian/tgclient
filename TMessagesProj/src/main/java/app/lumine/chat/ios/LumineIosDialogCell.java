/*
 * luminegram - iOS style chat list.
 * Larger avatars and taller rows in the dialogs list. DialogCell asks its
 * lumineIosList() predicate and lets this class own every number the mode
 * changes, so no layout logic sits inside the upstream file.
 */
package app.lumine.chat.ios;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.view.View;

import app.lumine.chat.LumineChatConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Cells.DialogCell;

public final class LumineIosDialogCell {

    public static final int AVATAR_SIZE = 60;
    public static final int STOCK_AVATAR_START = 11;
    public static final int STOCK_MESSAGE_PADDING = 72;
    public static final int ROW_EXTRA = 14;
    public static final int OFFSET = 5;
    public static final int AVATAR_TOUCH = 78;

    private LumineIosDialogCell() {
    }

    public static boolean usable() {
        return LumineChatConfig.getIosList();
    }

    /*
     * Runs at the top of buildLayout so every later dp(avatarStart) and
     * dp(messagePaddingStart) read sees the iOS values. Only the exact pair we
     * wrote is reverted when the mode is off, so cells that carry their own
     * paddings (topics, profile channel rows) are never touched.
     */
    public static void applyStarts(DialogCell cell, boolean ios) {
        if (ios) {
            cell.avatarStart = STOCK_AVATAR_START + 2;
            cell.messagePaddingStart = STOCK_MESSAGE_PADDING + 6;
        } else if (cell.avatarStart == STOCK_AVATAR_START + 2 && cell.messagePaddingStart == STOCK_MESSAGE_PADDING + 6) {
            cell.avatarStart = STOCK_AVATAR_START;
            cell.messagePaddingStart = STOCK_MESSAGE_PADDING;
        }
    }

    public static boolean insideAvatar(View cell, float x) {
        if (!LocaleController.isRTL) {
            return x >= 0 && x < dp(AVATAR_TOUCH);
        }
        return x >= cell.getMeasuredWidth() - dp(AVATAR_TOUCH) && x < cell.getMeasuredWidth();
    }
}
