// ======================================================
// luminegram - iOS style settings page
// ======================================================

package app.lumine.ios

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import tw.nekomimi.nekogram.NekoConfig

class IosStyleSettingsActivity : UniversalFragment() {

    private var chatPreview: IosChatPreview? = null

    override fun getTitle(): CharSequence = LocaleController.getString(R.string.LumineIosStyle)

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter?) {
        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineIosStyleSection)))
        items.add(check(TOGGLE_COMPACT_PILL, R.string.LumineCompactPill, IosStyleConfig.chatHeader.Bool(), R.string.LumineCompactPillInfo))
        items.add(check(TOGGLE_AVATAR_SLOT, R.string.LumineAvatarSlot, IosStyleConfig.chatHeaderAvatarSlot.Bool(), R.string.LumineAvatarSlotInfo))
        if (!IosStyleHelper.chatAvatarInMenuSlot()) {
            items.add(check(TOGGLE_AVATAR_RIGHT, R.string.LumineAvatarRight, IosStyleConfig.centerTitleRightAvatar.Bool()))
        }
        items.add(check(TOGGLE_CHAT_TITLE_MARQUEE, R.string.LumineChatTitleMarquee, IosStyleConfig.chatTitleMarquee.Bool(), R.string.LumineChatTitleMarqueeInfo))
        items.add(check(TOGGLE_UNREAD_BADGE, R.string.LumineUnreadBadge, NekoConfig.unreadBadgeOnBackButton.Bool(), R.string.LumineUnreadBadgeInfo))
        if (NekoConfig.unreadBadgeOnBackButton.Bool()) {
            items.add(check(TOGGLE_UNREAD_BADGE_IOS, R.string.LumineUnreadBadgeIos, IosStyleConfig.unreadBadgeOnBackButtonIos.Bool()))
        }
        items.add(check(TOGGLE_IOS_INPUT, R.string.iOSMessageInputField, NekoConfig.iOSMessageInputField.Bool(), R.string.iOSMessageInputFieldNotice))
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow(null))

        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineLiquidGlassIntensity)))
        items.add(UItem.asIntSlideView(
            1, 0, IosStyleConfig.liquidGlassIntensity.Int(), 100,
            { value: Int -> "$value%" },
            { value: Int ->
                IosStyleConfig.liquidGlassIntensity.setConfigInt(value)
                chatPreview?.rebuild()
            }
        ))
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow(null))

        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineLiquidGlassTint)))
        items.add(UItem.asIntSlideView(
            1, 0, IosStyleConfig.liquidGlassTint.Int(), 100,
            { value: Int -> "$value%" },
            { value: Int ->
                IosStyleConfig.liquidGlassTint.setConfigInt(value)
                chatPreview?.rebuild()
            }
        ))
        adapter?.whiteSectionEnd()
        items.add(UItem.asShadow(LocaleController.getString(R.string.LumineLiquidGlassInfo)))
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) {
        val toggled = when (item.id) {
            TOGGLE_COMPACT_PILL -> IosStyleConfig.chatHeader
            TOGGLE_AVATAR_SLOT -> IosStyleConfig.chatHeaderAvatarSlot
            TOGGLE_AVATAR_RIGHT -> IosStyleConfig.centerTitleRightAvatar
            TOGGLE_CHAT_TITLE_MARQUEE -> IosStyleConfig.chatTitleMarquee
            TOGGLE_UNREAD_BADGE -> NekoConfig.unreadBadgeOnBackButton
            TOGGLE_UNREAD_BADGE_IOS -> IosStyleConfig.unreadBadgeOnBackButtonIos
            TOGGLE_IOS_INPUT -> NekoConfig.iOSMessageInputField
            else -> return
        }
        toggled.setConfigBool(!toggled.Bool())
        chatPreview?.rebuild()
        listView.adapter.update(true)
    }

    override fun createView(context: Context): View {
        val content = super.createView(context) as FrameLayout
        listView.setSections()
        listView.adapter.setApplyBackground(false)
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

    private companion object {
        const val PREVIEW_HEIGHT_DP = 228
        const val TOGGLE_CHAT_TITLE_MARQUEE = 1
        const val TOGGLE_COMPACT_PILL = 3
        const val TOGGLE_AVATAR_SLOT = 4
        const val TOGGLE_AVATAR_RIGHT = 6
        const val TOGGLE_UNREAD_BADGE = 8
        const val TOGGLE_UNREAD_BADGE_IOS = 9
        const val TOGGLE_IOS_INPUT = 10
    }
}
