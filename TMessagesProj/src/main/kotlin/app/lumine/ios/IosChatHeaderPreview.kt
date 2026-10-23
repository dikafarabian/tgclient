// ======================================================
// luminegram - iOS style chat header preview
// ======================================================

package app.lumine.ios

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import kotlin.math.max
import kotlin.math.min

class IosChatHeaderPreview(context: Context) : IosPreviewView(context) {

    override fun onDraw(canvas: Canvas) {
        layoutBackdrop()
        drawBackdrop(canvas)

        val centered = IosStyleHelper.centerChatTitle()
        val compact = IosStyleHelper.compactChatPill()
        val avatarInMenu = IosStyleHelper.chatAvatarInMenuSlot()
        val avatarStatic = IosStyleHelper.chatAvatarStatic()
        val avatarRight = IosStyleHelper.chatAvatarOnRight()
        val avatarInPill = !avatarInMenu && !avatarStatic

        val title = sampleName
        val subtitle = LocaleController.getString(R.string.Online)
        val iconColor = Theme.getColor(Theme.key_actionBarDefaultIcon)
        val titleColor = Theme.getColor(Theme.key_actionBarDefaultTitle)
        val subtitleColor = Theme.getColor(Theme.key_actionBarDefaultSubtitle)

        val chip = dp(CHIP_DP)
        val gap = dp(GAP_DP)
        val avatarSize = dp(AVATAR_DP)
        val avatarRoom = avatarSize - dp(2f) + dp(9f)
        val rowTop = backdrop.top + dp(ROW_TOP_DP)
        val rowBottom = rowTop + chip
        val centerY = (rowTop + rowBottom) / 2f

        val counterOnBack = IosStyleConfig.unreadBadgeOnBackButton.Bool()
        val counterInPill = counterOnBack && IosStyleConfig.unreadBadgeOnBackButtonIos.Bool()
        val counterExtra = if (counterInPill) dp(COUNTER_EXTRA_DP) else 0f
        val backLeft = backdrop.left + dp(EDGE_DP)
        val backRight = backLeft + chip + counterExtra
        box.set(backLeft, rowTop, backRight, rowBottom)
        drawGlass(canvas, box, PreviewPanel.HEADER)
        drawIcon(canvas, R.drawable.ic_ab_back, backLeft + dp(23f), centerY, iconColor)
        if (counterInPill) {
            drawText(canvas, UNREAD_SAMPLE, backLeft + dp(37f), centerY + dp(5f), 15f, iconColor, true, Paint.Align.LEFT)
        } else if (counterOnBack) {
            drawCircle(canvas, backRight - dp(4f), rowTop + dp(4f), dp(9f), Theme.getColor(Theme.key_chats_actionBackground))
            drawText(canvas, UNREAD_SAMPLE, backRight - dp(4f), rowTop + dp(8f), 11f, Theme.getColor(Theme.key_chats_actionIcon), true, Paint.Align.CENTER)
        }

        val menuRight = backdrop.right - dp(EDGE_DP)
        val menuLeft = menuRight - chip
        box.set(menuLeft, rowTop, menuRight, rowBottom)
        drawGlass(canvas, box, PreviewPanel.HEADER)
        if (avatarInMenu) {
            drawAvatar(canvas, (menuLeft + menuRight) / 2f, centerY, avatarSize)
        } else {
            drawIcon(canvas, R.drawable.ic_ab_other, (menuLeft + menuRight) / 2f, centerY, iconColor)
        }

        val titleWidth = max(measureText(title, TITLE_SP, true), measureText(subtitle, SUBTITLE_SP, false))
        val inset = max(backRight - backdrop.left, backdrop.right - menuLeft) + gap
        val areaLeft = backdrop.left + inset
        val areaRight = backdrop.right - inset
        val textInset = dp(TEXT_INSET_DP)

        val pillLeft: Float
        val pillRight: Float
        var avatarCenterX = -1f
        val textCenterX: Float
        var textLeftX = -1f

        if (!centered) {
            pillLeft = backRight + gap
            pillRight = menuLeft - gap
            avatarCenterX = pillLeft + dp(6f) + avatarSize / 2f
            textLeftX = pillLeft + dp(6f) + avatarSize + dp(10f)
            textCenterX = 0f
        } else if (!compact) {
            pillLeft = areaLeft
            pillRight = areaRight
            avatarCenterX = if (avatarRight) pillRight - dp(9f) - avatarSize / 2f else pillLeft + dp(9f) + avatarSize / 2f
            textCenterX = (pillLeft + pillRight) / 2f
        } else {
            val insetLeft = textInset + if (avatarInPill && !avatarRight) avatarRoom else 0f
            val insetRight = textInset + if (avatarInPill && avatarRight) avatarRoom else 0f
            val staticReserve = if (avatarStatic) backdrop.left + dp(STATIC_AVATAR_LEFT_DP) + counterExtra + avatarSize - dp(2f) + dp(6f) else 0f
            val limitLeft = max(areaLeft, staticReserve)
            val width = min(max(titleWidth + dp(3f) + insetLeft + insetRight, dp(MIN_PILL_DP)), areaRight - limitLeft)
            val left = min(max((areaLeft + areaRight) / 2f - width / 2f, limitLeft), areaRight - width)
            pillLeft = left
            pillRight = left + width
            if (avatarInPill) {
                avatarCenterX = if (avatarRight) pillRight - dp(9f) - avatarSize / 2f else pillLeft + dp(9f) + avatarSize / 2f
            }
            textCenterX = (pillLeft + insetLeft + pillRight - insetRight) / 2f
        }

        box.set(pillLeft, rowTop, pillRight, rowBottom)
        drawGlass(canvas, box, PreviewPanel.HEADER)

        if (avatarStatic) {
            val staticCenterX = backdrop.left + dp(STATIC_AVATAR_LEFT_DP) + counterExtra + avatarSize / 2f
            drawAvatar(canvas, staticCenterX, centerY, avatarSize)
        } else if (avatarInPill && avatarCenterX >= 0f) {
            drawAvatar(canvas, avatarCenterX, centerY, avatarSize)
        }

        val align = if (centered) Paint.Align.CENTER else Paint.Align.LEFT
        val textX = if (centered) textCenterX else textLeftX
        drawText(canvas, title, textX, centerY - dp(3f), TITLE_SP, titleColor, true, align)
        drawText(canvas, subtitle, textX, centerY + dp(14f), SUBTITLE_SP, subtitleColor, false, align)

        val bubbleTop = rowBottom + dp(14f)
        val bubbleHeight = dp(30f)
        val bubbleWidth = backdrop.width() * 0.55f
        drawBubble(canvas, backdrop.left + dp(10f), bubbleTop, bubbleWidth, bubbleHeight, false)
        drawBubble(canvas, backdrop.right - dp(10f) - bubbleWidth, bubbleTop + bubbleHeight + dp(8f), bubbleWidth, bubbleHeight, true)
    }

    private companion object {
        const val UNREAD_SAMPLE = "12"
        const val CHIP_DP = 46f
        const val GAP_DP = 6f
        const val EDGE_DP = 8f
        const val ROW_TOP_DP = 8f
        const val AVATAR_DP = 38f
        const val COUNTER_EXTRA_DP = 24f
        const val TEXT_INSET_DP = 16f
        const val MIN_PILL_DP = 120f
        const val STATIC_AVATAR_LEFT_DP = 61f
        const val TITLE_SP = 16f
        const val SUBTITLE_SP = 13f
    }
}
