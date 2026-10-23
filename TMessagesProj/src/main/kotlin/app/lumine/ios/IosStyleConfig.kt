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
    val glassAngle = addConfig("LumineIosGlassAngle", ConfigItem.configTypeInt, 0)

    @JvmField
    val glassIntensity = addConfig("LumineIosGlassIntensity", ConfigItem.configTypeInt, 50)

    @JvmField
    val glassTint = addConfig("LumineIosGlassTint", ConfigItem.configTypeInt, 50)

    @JvmField
    val inputButtonPlacement = addConfig("LumineIosInputButtonPlacement", ConfigItem.configTypeBool, true)

    @JvmField
    val inputAppearance = addConfig("LumineIosInputAppearance", ConfigItem.configTypeBool, true)

    @JvmField
    val bottomNavigationBar = addConfig("LumineIosBottomNavigationBar", ConfigItem.configTypeBool, false)

    @JvmField
    val chatsTabReturnsToFirstFolder =
        addConfig("LumineIosChatsTabReturnsToFirstFolder", ConfigItem.configTypeBool, true)

    @JvmField
    val centerTitleMain = addConfig("LumineIosCenterTitleMain", ConfigItem.configTypeBool, false)

    @JvmField
    val centerTitleChats = addConfig("LumineIosCenterTitleChats", ConfigItem.configTypeBool, true)

    @JvmField
    val centerTitleRightAvatar = addConfig("LumineIosCenterTitleRightAvatar", ConfigItem.configTypeBool, false)

    @JvmField
    val chatHeader = addConfig("LumineIosChatHeader", ConfigItem.configTypeBool, true)

    @JvmField
    val chatHeaderAvatarSlot = addConfig("LumineIosChatHeaderAvatarSlot", ConfigItem.configTypeBool, true)

    @JvmField
    val chatHeaderAvatarStatic = addConfig("LumineIosChatHeaderAvatarStatic", ConfigItem.configTypeBool, false)

    @JvmField
    val chatHeaderNoPill = addConfig("LumineIosChatHeaderNoPill", ConfigItem.configTypeBool, false)

    @JvmField
    val chatTitleMarquee = addConfig("LumineIosChatTitleMarquee", ConfigItem.configTypeBool, true)

    @JvmField
    val unreadBadgeOnBackButton = addConfig("LumineIosUnreadBadgeOnBackButton", ConfigItem.configTypeBool, true)

    @JvmField
    val unreadBadgeOnBackButtonIos =
        addConfig("LumineIosUnreadBadgeOnBackButtonIos", ConfigItem.configTypeBool, true)

    private fun addConfig(key: String, type: Int, defaultValue: Any?): ConfigItem {
        val item = ConfigItem(key, type, defaultValue)
        configs.add(item)
        return item
    }

    @JvmStatic
    fun getConfigTypes(): Map<String, Int> = configs.associate { it.key to it.type }

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

    @JvmStatic
    fun reset() {
        synchronized(sync) {
            val editor = getPreferences().edit()
            for (item in configs) {
                editor.remove(item.key)
                item.value = item.defaultValue
            }
            editor.apply()
        }
    }
}
