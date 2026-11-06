// ======================================================
// luminegram - iOS style settings page
// ======================================================

package app.lumine.ios

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.RadioColorCell
import org.telegram.ui.Components.ItemOptions
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import tw.nekomimi.nekogram.NekoConfig
import app.lumine.chats.ActionButtonStyle

class IosStyleSettingsActivity : UniversalFragment() {

    private var chatPreview: IosChatPreview? = null

    override fun getTitle(): CharSequence = LocaleController.getString(R.string.LumineIosStyle)

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter?) {
        adapter?.whiteSectionStart()
        items.add(UItem.asHeader(LocaleController.getString(R.string.LumineIosStyleSection)))
        items.add(check(TOGGLE_COMPACT_PILL, R.string.LumineCompactPill, IosStyleConfig.chatHeader.Bool(), R.string.LumineCompactPillInfo))
        if (IosStyleHelper.compactChatPill()) {
            items.add(check(TOGGLE_AVATAR_SLOT, R.string.LumineAvatarSlot, IosStyleConfig.chatHeaderAvatarSlot.Bool(), R.string.LumineAvatarSlotInfo))
            if (!IosStyleHelper.chatAvatarInMenuSlot()) {
                items.add(check(TOGGLE_AVATAR_RIGHT, R.string.LumineAvatarRight, IosStyleConfig.centerTitleRightAvatar.Bool()))
            }
        }
        items.add(check(TOGGLE_CHAT_TITLE_MARQUEE, R.string.LumineChatTitleMarquee, IosStyleConfig.chatTitleMarquee.Bool(), R.string.LumineChatTitleMarqueeInfo))
        items.add(check(TOGGLE_UNREAD_BADGE, R.string.LumineUnreadBadge, NekoConfig.unreadBadgeOnBackButton.Bool(), R.string.LumineUnreadBadgeInfo))
        if (NekoConfig.unreadBadgeOnBackButton.Bool()) {
            items.add(check(TOGGLE_UNREAD_BADGE_IOS, R.string.LumineUnreadBadgeIos, IosStyleConfig.unreadBadgeOnBackButtonIos.Bool()))
        }
        items.add(check(TOGGLE_IOS_INPUT, R.string.iOSMessageInputField, NekoConfig.iOSMessageInputField.Bool(), R.string.iOSMessageInputFieldNotice))
        items.add(check(TOGGLE_HIDE_FADE, R.string.LumineHideFade, IosStyleConfig.hideFadeViews.Bool(), R.string.LumineHideFadeInfo))
        items.add(UItem.asButton(
            BUTTON_AVATAR_SENDER,
            LocaleController.getString(R.string.LumineAvatarSender),
            LocaleController.getString(SENDER_LABELS[IosStyleHelper.avatarSenderMode().coerceIn(0, 2)])
        ))
        items.add(check(TOGGLE_INPUT_ANIMATION, R.string.LumineInputTextAnimations, IosStyleConfig.inputTextAnimations.Bool()))
        if (IosStyleConfig.inputTextAnimations.Bool()) {
            items.add(UItem.asIntSlideView(
                1, 0, IosStyleConfig.inputAnimationStrength.Int().coerceIn(0, 4), 4,
                { value: Int -> LocaleController.getString(STRENGTH_LABELS[value.coerceIn(0, 4)]) },
                { value: Int -> IosStyleConfig.inputAnimationStrength.setConfigInt(value) }
            ))
        }
        items.add(UItem.asButton(
            BUTTON_ACTION_STYLE,
            LocaleController.getString(R.string.LumineActionButtonStyle),
            LocaleController.getString(ACTION_STYLE_LABELS[ActionButtonStyle.getCurrentStyle()])
        ))
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
        if (item.id == BUTTON_AVATAR_SENDER) {
            showAvatarSenderDialog()
            return
        }
        if (item.id == BUTTON_ACTION_STYLE) {
            val options = ItemOptions.makeOptions(this, view).setDimAlpha(40)
            for (style in ACTION_STYLE_LABELS.indices) {
                options.add(LocaleController.getString(ACTION_STYLE_LABELS[style])) {
                    IosStyleConfig.actionButtonStyle.setConfigInt(style)
                    chatPreview?.rebuild()
                    listView.adapter.update(true)
                }
            }
            options.show()
            return
        }
        val toggled = when (item.id) {
            TOGGLE_COMPACT_PILL -> IosStyleConfig.chatHeader
            TOGGLE_AVATAR_SLOT -> IosStyleConfig.chatHeaderAvatarSlot
            TOGGLE_AVATAR_RIGHT -> IosStyleConfig.centerTitleRightAvatar
            TOGGLE_CHAT_TITLE_MARQUEE -> IosStyleConfig.chatTitleMarquee
            TOGGLE_UNREAD_BADGE -> NekoConfig.unreadBadgeOnBackButton
            TOGGLE_UNREAD_BADGE_IOS -> IosStyleConfig.unreadBadgeOnBackButtonIos
            TOGGLE_IOS_INPUT -> NekoConfig.iOSMessageInputField
            TOGGLE_HIDE_FADE -> IosStyleConfig.hideFadeViews
            TOGGLE_INPUT_ANIMATION -> IosStyleConfig.inputTextAnimations
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

    override fun onTransitionAnimationEnd(isOpen: Boolean, backward: Boolean) {
        super.onTransitionAnimationEnd(isOpen, backward)
        if (isOpen) chatPreview?.onOpened()
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

    private fun showAvatarSenderDialog() {
        val context = parentActivity ?: return
        val provider = resourceProvider
        val current = IosStyleHelper.avatarSenderMode()
        val builder = AlertDialog.Builder(context, provider)
        builder.setTitle(LocaleController.getString(R.string.LumineAvatarSender))
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        builder.setView(layout)
        for (mode in SENDER_LABELS.indices) {
            val cell = RadioColorCell(context, provider)
            cell.setPadding(AndroidUtilities.dp(4f), 0, AndroidUtilities.dp(4f), 0)
            cell.setCheckColor(
                Theme.getColor(Theme.key_radioBackground, provider),
                Theme.getColor(Theme.key_dialogRadioBackgroundChecked, provider)
            )
            cell.setTextAndValue(LocaleController.getString(SENDER_LABELS[mode]), mode == current)
            cell.background = Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, provider), 2)
            layout.addView(cell)
            cell.setOnClickListener {
                IosStyleConfig.avatarSender.setConfigInt(mode)
                chatPreview?.rebuild()
                listView.adapter.update(true)
                builder.dismissRunnable.run()
            }
        }
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null)
        showDialog(builder.create())
    }

    override fun onLongClick(item: UItem, view: View, position: Int, x: Float, y: Float): Boolean = false

    private companion object {
        const val PREVIEW_HEIGHT_DP = 132
        const val TOGGLE_CHAT_TITLE_MARQUEE = 1
        const val TOGGLE_COMPACT_PILL = 3
        const val TOGGLE_AVATAR_SLOT = 4
        const val TOGGLE_AVATAR_RIGHT = 6
        const val TOGGLE_UNREAD_BADGE = 8
        const val TOGGLE_UNREAD_BADGE_IOS = 9
        const val TOGGLE_IOS_INPUT = 10
        const val TOGGLE_HIDE_FADE = 11
        const val TOGGLE_INPUT_ANIMATION = 12
        const val BUTTON_ACTION_STYLE = 30
        val ACTION_STYLE_LABELS = intArrayOf(
            R.string.LumineActionButtonStyleAccent,
            R.string.LumineActionButtonStyleNeutral,
            R.string.LumineActionButtonStyleWhite
        )
        val STRENGTH_LABELS = intArrayOf(
            R.string.LumineInputAnimationWeakest,
            R.string.LumineInputAnimationWeak,
            R.string.LumineInputAnimationModerate,
            R.string.LumineInputAnimationStrong,
            R.string.LumineInputAnimationStrongest
        )
        const val BUTTON_AVATAR_SENDER = 31
        val SENDER_LABELS = intArrayOf(
            R.string.LumineAvatarSenderShow,
            R.string.LumineAvatarSenderHide,
            R.string.LumineAvatarSenderHold
        )
    }
}
