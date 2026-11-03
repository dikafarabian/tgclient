// ======================================================
// luminegram - unread badge on the back button
// ======================================================

package app.lumine.ios

import org.telegram.messenger.MessagesStorage
import org.telegram.ui.ActionBar.ActionBar
import tw.nekomimi.nekogram.NekoConfig

object UnreadBackBadgeHelper {

    private const val SAMPLE_COUNT = 12

    @JvmStatic
    @JvmOverloads
    fun update(actionBar: ActionBar?, account: Int, sample: Boolean = false) {
        if (actionBar == null) return
        val badge = actionBar.backButtonImageView ?: return
        if (!NekoConfig.unreadBadgeOnBackButton.Bool()) {
            actionBar.setIOSUnreadBadgeAvailable(false)
            return
        }
        val count = if (sample) SAMPLE_COUNT else MessagesStorage.getInstance(account).mainUnreadCount
        actionBar.setIOSUnreadBadgeAvailable(count > 0)
        badge.checkUnreadView(count)
        actionBar.updateBackPillWidth(true)
    }
}
