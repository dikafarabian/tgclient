// ======================================================
// luminegram - iOS style policy helpers
// ======================================================

package app.lumine.ios

object IosStyleHelper {

    @JvmStatic
    fun centerScreenTitles(): Boolean = IosStyleConfig.centerTitleMain.Bool()

    @JvmStatic
    fun centerChatTitle(): Boolean = IosStyleConfig.centerTitleChats.Bool()

    @JvmStatic
    fun compactChatPill(): Boolean = centerChatTitle() && IosStyleConfig.chatHeader.Bool()

    @JvmStatic
    fun chatAvatarInMenuSlot(): Boolean = compactChatPill() && IosStyleConfig.chatHeaderAvatarSlot.Bool()

    @JvmStatic
    fun chatAvatarStatic(): Boolean =
        compactChatPill() && !chatAvatarInMenuSlot() && IosStyleConfig.chatHeaderAvatarStatic.Bool()

    @JvmStatic
    fun chatAvatarOnRight(): Boolean =
        centerChatTitle() && !chatAvatarInMenuSlot() && !chatAvatarStatic() &&
            IosStyleConfig.centerTitleRightAvatar.Bool()

    @JvmStatic
    fun shouldCenterTitle(fragment: Any?): Boolean {
        if (fragment == null) return false
        if (fragment.javaClass.name == "org.telegram.ui.ChatActivity") return centerChatTitle()
        return centerScreenTitles()
    }
}
