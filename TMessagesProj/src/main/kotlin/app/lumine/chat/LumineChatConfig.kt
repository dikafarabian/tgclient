/*
 * luminegram - Chat Interface settings.
 * Every option of the Chat Interface page is kept here, in LumineConfig.
 */
package app.lumine.chat

import app.lumine.LumineConfig

object LumineChatConfig {

    const val SENDER_SHOW = 0
    const val SENDER_HIDE = 1
    const val SENDER_HOLD = 2

    @JvmStatic
    var chatPill: Boolean
        get() = LumineConfig.getBoolean("LumineChatPill", true)
        set(value) = LumineConfig.setBoolean("LumineChatPill", value)

    @JvmStatic
    var chatPillAvatarSlot: Boolean
        get() = LumineConfig.getBoolean("LumineChatPillAvatarSlot", false)
        set(value) = LumineConfig.setBoolean("LumineChatPillAvatarSlot", value)

    @JvmStatic
    var chatPillAvatarSwap: Boolean
        get() = LumineConfig.getBoolean("LumineChatPillAvatarSwap", false)
        set(value) = LumineConfig.setBoolean("LumineChatPillAvatarSwap", value)

    @JvmStatic
    var chatPillAvatarStatic: Boolean
        get() = LumineConfig.getBoolean("LumineChatPillAvatarStatic", false)
        set(value) = LumineConfig.setBoolean("LumineChatPillAvatarStatic", value)

    @JvmStatic
    var chatPillHideAvatar: Boolean
        get() = LumineConfig.getBoolean("LumineChatPillHideAvatar", false)
        set(value) = LumineConfig.setBoolean("LumineChatPillHideAvatar", value)

    @JvmStatic
    var chatPillAvatarRight: Boolean
        get() = LumineConfig.getBoolean("LumineChatPillAvatarRight", false)
        set(value) = LumineConfig.setBoolean("LumineChatPillAvatarRight", value)

    @JvmStatic
    var chatTitleMarquee: Boolean
        get() = LumineConfig.getBoolean("LumineChatTitleMarquee", false)
        set(value) = LumineConfig.setBoolean("LumineChatTitleMarquee", value)

    @JvmStatic
    var unreadBadge: Boolean
        get() = LumineConfig.getBoolean("LumineChatUnreadBadge", false)
        set(value) = LumineConfig.setBoolean("LumineChatUnreadBadge", value)

    @JvmStatic
    var unreadBadgeIos: Boolean
        get() = LumineConfig.getBoolean("LumineChatUnreadBadgeIos", false)
        set(value) = LumineConfig.setBoolean("LumineChatUnreadBadgeIos", value)

    @JvmStatic
    var iosInput: Boolean
        get() = LumineConfig.getBoolean("LumineChatIosInput", false)
        set(value) = LumineConfig.setBoolean("LumineChatIosInput", value)

    @JvmStatic
    var hideFade: Boolean
        get() = LumineConfig.getBoolean("LumineChatHideFade", false)
        set(value) = LumineConfig.setBoolean("LumineChatHideFade", value)

    @JvmStatic
    var avatarSender: Int
        get() = LumineConfig.getInt("LumineChatAvatarSender", SENDER_SHOW).coerceIn(SENDER_SHOW, SENDER_HOLD)
        set(value) = LumineConfig.setInt("LumineChatAvatarSender", value.coerceIn(SENDER_SHOW, SENDER_HOLD))

    @JvmStatic
    var avatarSenderHidden: Boolean
        get() = LumineConfig.getBoolean("LumineChatAvatarSenderHidden", false)
        set(value) = LumineConfig.setBoolean("LumineChatAvatarSenderHidden", value)

    @JvmStatic
    var inputAnimation: Boolean
        get() = LumineConfig.getBoolean("LumineChatInputAnimation", false)
        set(value) = LumineConfig.setBoolean("LumineChatInputAnimation", value)

    @JvmStatic
    var inputAnimationStrength: Int
        get() = LumineConfig.getInt("LumineChatInputAnimationStrength", 2).coerceIn(0, 4)
        set(value) = LumineConfig.setInt("LumineChatInputAnimationStrength", value.coerceIn(0, 4))

    @JvmStatic
    var actionButtonStyle: Int
        get() = LumineConfig.getInt("LumineChatActionButtonStyle", 0).coerceIn(0, 2)
        set(value) = LumineConfig.setInt("LumineChatActionButtonStyle", value.coerceIn(0, 2))

    @JvmStatic
    var iosList: Boolean
        get() = LumineConfig.getBoolean("LumineChatIosList", false)
        set(value) = LumineConfig.setBoolean("LumineChatIosList", value)

    @JvmStatic
    var iosCompact: Boolean
        get() = LumineConfig.getBoolean("LumineChatIosCompact", false)
        set(value) = LumineConfig.setBoolean("LumineChatIosCompact", value)
}
