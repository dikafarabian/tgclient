// ======================================================
// luminegram - iOS style settings page
// ======================================================

package app.lumine.ios

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import app.exteraless.appearance.AltSeekbar
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawableRenderNode
import kotlin.math.roundToInt

class IosStyleSettingsActivity : UniversalFragment() {

    private var angleSlider: AltSeekbar? = null
    private var intensitySlider: AltSeekbar? = null
    private var tintSlider: AltSeekbar? = null
    private var chatPreview: IosChatPreview? = null

    override fun getTitle(): CharSequence = LocaleController.getString(R.string.LumineIosStyle)

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter?) {
        val context = context ?: return

        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineChatHeader)))
        items.add(check(TOGGLE_CENTER_CHATS, R.string.LumineCenterTitleChats, IosStyleConfig.centerTitleChats.Bool(), R.string.LumineCenterTitleChatsInfo))
        if (IosStyleHelper.centerChatTitle()) {
            items.add(check(TOGGLE_COMPACT_PILL, R.string.LumineCompactPill, IosStyleConfig.chatHeader.Bool(), R.string.LumineCompactPillInfo))
            if (IosStyleHelper.compactChatPill()) {
                items.add(check(TOGGLE_AVATAR_SLOT, R.string.LumineAvatarSlot, IosStyleConfig.chatHeaderAvatarSlot.Bool(), R.string.LumineAvatarSlotInfo))
                if (!IosStyleHelper.chatAvatarInMenuSlot()) {
                    items.add(check(TOGGLE_AVATAR_STATIC, R.string.LumineAvatarStatic, IosStyleConfig.chatHeaderAvatarStatic.Bool(), R.string.LumineAvatarStaticInfo))
                }
            }
            if (!IosStyleHelper.chatAvatarInMenuSlot() && !IosStyleHelper.chatAvatarStatic()) {
                items.add(check(TOGGLE_AVATAR_RIGHT, R.string.LumineAvatarRight, IosStyleConfig.centerTitleRightAvatar.Bool()))
            }
        }
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow())

        adapter?.whiteSectionStart()
        items.add(check(TOGGLE_CENTER_MAIN, R.string.LumineCenterTitleMain, IosStyleConfig.centerTitleMain.Bool(), R.string.LumineCenterTitleMainInfo))
        items.add(check(TOGGLE_CHAT_TITLE_MARQUEE, R.string.LumineChatTitleMarquee, IosStyleConfig.chatTitleMarquee.Bool(), R.string.LumineChatTitleMarqueeInfo))
        items.add(check(TOGGLE_UNREAD_BADGE, R.string.LumineUnreadBadge, IosStyleConfig.unreadBadgeOnBackButton.Bool(), R.string.LumineUnreadBadgeInfo))
        if (IosStyleConfig.unreadBadgeOnBackButton.Bool()) {
            items.add(check(TOGGLE_UNREAD_BADGE_IOS, R.string.LumineUnreadBadgeIos, IosStyleConfig.unreadBadgeOnBackButtonIos.Bool()))
        }
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow())

        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineChatInput)))
        items.add(check(TOGGLE_INPUT_APPEARANCE, R.string.LumineInputAppearance, IosStyleConfig.inputAppearance.Bool(), R.string.LumineInputAppearanceInfo))
        items.add(check(TOGGLE_INPUT_BUTTON_PLACEMENT, R.string.LumineInputButtonPlacement, IosStyleConfig.inputButtonPlacement.Bool(), R.string.LumineInputButtonPlacementInfo))
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow())

        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineLiquidGlass)))
        items.add(UItem.asCustom(angleSlider(context), SLIDER_HEIGHT_DP))
        items.add(UItem.asCustom(intensitySlider(context), SLIDER_HEIGHT_DP))
        items.add(UItem.asCustom(tintSlider(context), SLIDER_HEIGHT_DP))
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow())
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) {
        val toggled = when (item.id) {
            TOGGLE_CENTER_CHATS -> IosStyleConfig.centerTitleChats
            TOGGLE_COMPACT_PILL -> IosStyleConfig.chatHeader
            TOGGLE_AVATAR_SLOT -> IosStyleConfig.chatHeaderAvatarSlot
            TOGGLE_AVATAR_STATIC -> IosStyleConfig.chatHeaderAvatarStatic
            TOGGLE_AVATAR_RIGHT -> IosStyleConfig.centerTitleRightAvatar
            TOGGLE_CENTER_MAIN -> IosStyleConfig.centerTitleMain
            TOGGLE_CHAT_TITLE_MARQUEE -> IosStyleConfig.chatTitleMarquee
            TOGGLE_UNREAD_BADGE -> IosStyleConfig.unreadBadgeOnBackButton
            TOGGLE_UNREAD_BADGE_IOS -> IosStyleConfig.unreadBadgeOnBackButtonIos
            TOGGLE_INPUT_APPEARANCE -> IosStyleConfig.inputAppearance
            TOGGLE_INPUT_BUTTON_PLACEMENT -> IosStyleConfig.inputButtonPlacement
            else -> return
        }
        toggled.setConfigBool(!toggled.Bool())
        chatPreview?.rebuild()
        listView.adapter.update(true)
    }

    override fun createView(context: Context): View {
        val content = super.createView(context) as FrameLayout
        val preview = IosChatPreview(context, this)
        chatPreview = preview
        val height = AndroidUtilities.dp(PREVIEW_HEIGHT_DP.toFloat())
        content.addView(preview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT.toFloat(), PREVIEW_HEIGHT_DP.toFloat(), Gravity.TOP))
        (listView.layoutParams as FrameLayout.LayoutParams).topMargin = height
        return content
    }

    override fun onResume() {
        super.onResume()
        chatPreview?.onResume()
    }

    override fun onPause() {
        super.onPause()
        chatPreview?.onPause()
    }

    override fun onFragmentDestroy() {
        chatPreview?.destroy()
        chatPreview = null
        super.onFragmentDestroy()
    }

    private fun check(id: Int, title: Int, checked: Boolean, info: Int = 0): UItem {
        val label = LocaleController.getString(title)
        val item = if (info == 0) {
            UItem.asCheck(id, label)
        } else {
            UItem.asCheck(id, label, LocaleController.getString(info), true)
        }
        return item.setChecked(checked)
    }

    override fun onLongClick(item: UItem, view: View, position: Int, x: Float, y: Float): Boolean = false

    private fun angleSlider(context: Context): AltSeekbar = angleSlider ?: DegreeSeekbar(
        context,
        { value ->
            IosStyleConfig.glassAngle.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 360,
        LocaleController.getString(R.string.LumineLiquidGlassAngle),
        "0°", "360°",
    ).also {
        it.setProgress(IosStyleConfig.glassAngle.Int().toFloat())
        angleSlider = it
    }

    private fun intensitySlider(context: Context): AltSeekbar = intensitySlider ?: PercentSeekbar(
        context,
        { value ->
            IosStyleConfig.glassIntensity.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 150,
        LocaleController.getString(R.string.LumineLiquidGlassIntensity),
        "0%", "150%",
    ).also {
        it.setProgress(IosStyleConfig.glassIntensity.Int().toFloat())
        intensitySlider = it
    }

    private fun tintSlider(context: Context): AltSeekbar = tintSlider ?: PercentSeekbar(
        context,
        { value ->
            IosStyleConfig.glassTint.setConfigInt(value.roundToInt())
            refreshLiquidGlassEffects()
        },
        0, 100,
        LocaleController.getString(R.string.LumineLiquidGlassTint),
        "0%", "100%",
    ).also {
        it.setProgress(IosStyleConfig.glassTint.Int().toFloat())
        tintSlider = it
    }

    private fun refreshLiquidGlassEffects() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BlurredBackgroundDrawableRenderNode.invalidateLiquidGlassDrawables()
        }
    }

    private class PercentSeekbar(
        context: Context,
        onDrag: AltSeekbar.OnDrag,
        min: Int,
        max: Int,
        title: String,
        left: String,
        right: String,
    ) : AltSeekbar(context, onDrag, min, max, title, left, right) {
        override fun getTextForHeader(): CharSequence = "$roundedValue%"
    }

    private class DegreeSeekbar(
        context: Context,
        onDrag: AltSeekbar.OnDrag,
        min: Int,
        max: Int,
        title: String,
        left: String,
        right: String,
    ) : AltSeekbar(context, onDrag, min, max, title, left, right) {
        override fun getTextForHeader(): CharSequence = "$roundedValue°"
    }

    private companion object {
        const val SLIDER_HEIGHT_DP = 112
        const val PREVIEW_HEIGHT_DP = 220
        const val TOGGLE_CHAT_TITLE_MARQUEE = 1
        const val TOGGLE_CENTER_CHATS = 2
        const val TOGGLE_COMPACT_PILL = 3
        const val TOGGLE_AVATAR_SLOT = 4
        const val TOGGLE_AVATAR_STATIC = 5
        const val TOGGLE_AVATAR_RIGHT = 6
        const val TOGGLE_CENTER_MAIN = 7
        const val TOGGLE_UNREAD_BADGE = 8
        const val TOGGLE_UNREAD_BADGE_IOS = 9
        const val TOGGLE_INPUT_APPEARANCE = 10
        const val TOGGLE_INPUT_BUTTON_PLACEMENT = 11
    }
}
