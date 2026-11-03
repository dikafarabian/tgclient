// ======================================================
// luminegram - iOS style policy helpers
// ======================================================

package app.lumine.ios

import android.graphics.Color
import xyz.nextalone.nagram.NaConfig

object IosStyleHelper {

    @JvmStatic
    fun liquidGlassIntensity(base: Float): Float {
        IosStyleConfig.ensureLoaded()
        return base * IosStyleConfig.liquidGlassIntensity.Int() / 50f
    }

    @JvmStatic
    fun liquidGlassVersion(): Int {
        IosStyleConfig.ensureLoaded()
        return IosStyleConfig.liquidGlassIntensity.Int() * 101 + IosStyleConfig.liquidGlassTint.Int()
    }

    @JvmStatic
    fun liquidGlassTint(color: Int): Int {
        IosStyleConfig.ensureLoaded()
        val alpha = (Color.alpha(color) * IosStyleConfig.liquidGlassTint.Int() / 50f).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    @JvmStatic
    fun centerScreenTitles(): Boolean =
        NaConfig.centerActionBarTitle.Bool()

    @JvmStatic
    fun centerChatTitle(): Boolean {
        IosStyleConfig.ensureLoaded()
        return IosStyleConfig.chatCenterTitle.Bool()
    }

    @JvmStatic
    fun compactChatPill(): Boolean = centerChatTitle() && IosStyleConfig.chatHeader.Bool()

    @JvmStatic
    fun chatAvatarInMenuSlot(): Boolean = compactChatPill() && IosStyleConfig.chatHeaderAvatarSlot.Bool()

    @JvmStatic
    fun chatAvatarOnRight(): Boolean =
        centerChatTitle() && !chatAvatarInMenuSlot() &&
            IosStyleConfig.centerTitleRightAvatar.Bool()

    @JvmStatic
    fun shouldCenterTitle(fragment: Any?): Boolean {
        if (fragment == null) return centerScreenTitles()
        if (fragment.javaClass.name == "org.telegram.ui.ChatActivity") return centerChatTitle()
        return centerScreenTitles()
    }
}
