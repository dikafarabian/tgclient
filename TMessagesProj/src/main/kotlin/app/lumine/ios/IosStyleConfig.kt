// ======================================================
// luminegram - iOS style settings
// ======================================================

package app.lumine.ios

import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import tw.nekomimi.nekogram.NekoConfig
import tw.nekomimi.nekogram.config.ConfigItem

object IosStyleConfig {

    private val sync = Any()
    private val configs = ArrayList<ConfigItem>()

    @Volatile
    private var configLoaded = false

    @JvmStatic
    fun getPreferences(): SharedPreferences = NekoConfig.getPreferences()

    @JvmField
    val centerTitleRightAvatar = addConfig("LumineIosCenterTitleRightAvatar", ConfigItem.configTypeBool, false)

    @JvmField
    val chatHeader = addConfig("LumineIosChatHeader", ConfigItem.configTypeBool, true)

    @JvmField
    val chatHeaderAvatarSlot = addConfig("LumineIosChatHeaderAvatarSlot", ConfigItem.configTypeBool, true)

    @JvmField
    val chatTitleMarquee = addConfig("LumineIosChatTitleMarquee", ConfigItem.configTypeBool, true)

    @JvmField
    val unreadBadgeOnBackButtonIos =
        addConfig("LumineIosUnreadBadgeOnBackButtonIos", ConfigItem.configTypeBool, true)

    @JvmField
    val liquidGlassIntensity = addConfig("LumineLiquidGlassIntensity", ConfigItem.configTypeInt, 40)

    @JvmField
    val liquidGlassTint = addConfig("LumineLiquidGlassTint", ConfigItem.configTypeInt, 20)

    @JvmField
    val stickerTimeSide = addConfig("LumineStickerTimeSide", ConfigItem.configTypeBool, false)

    @JvmField
    val hideFadeViews = addConfig("LumineHideFadeViews", ConfigItem.configTypeBool, false)

    @JvmField
    val avatarSender = addConfig("LumineAvatarSender", ConfigItem.configTypeInt, 0)

    @JvmField
    val avatarSenderHidden = addConfig("LumineAvatarSenderHidden", ConfigItem.configTypeBool, false)

    @JvmField
    val inputTextAnimations = addConfig("LumineInputTextAnimations", ConfigItem.configTypeBool, false)

    @JvmField
    val inputAnimationStrength = addConfig("LumineInputAnimationStrength", ConfigItem.configTypeInt, 2)

    private fun addConfig(key: String, type: Int, defaultValue: Any?): ConfigItem {
        val item = ConfigItem(key, type, defaultValue)
        configs.add(item)
        return item
    }

    @JvmStatic
    fun ensureLoaded() {
        if (!configLoaded) loadConfig(false)
    }

    @JvmStatic
    fun init() {
        loadConfig(false)
    }

    @JvmStatic
    fun loadConfig(force: Boolean) {
        synchronized(sync) {
            if (configLoaded && !force) return
            if (ApplicationLoader.applicationContext == null) return
            val preferences = getPreferences()
            for (item in configs) {
                try {
                    when (item.type) {
                        ConfigItem.configTypeBool ->
                            item.value = preferences.getBoolean(item.key, item.defaultValue as Boolean)

                        ConfigItem.configTypeInt ->
                            item.value = preferences.getInt(item.key, item.defaultValue as Int)
                    }
                } catch (e: Exception) {
                    FileLog.e(e)
                }
            }
            configLoaded = true
        }
    }
}
