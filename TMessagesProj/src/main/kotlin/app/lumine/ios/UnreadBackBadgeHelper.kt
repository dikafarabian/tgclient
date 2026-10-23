// ======================================================
// luminegram - unread badge on the back button
// ======================================================

package app.lumine.ios

import org.telegram.messenger.MessagesStorage
import org.telegram.ui.ActionBar.ActionBar

object UnreadBackBadgeHelper {

    @JvmStatic
    fun update(actionBar: ActionBar?, account: Int) {
        if (actionBar == null) return
        val badge = actionBar.backButtonImageView ?: return
        if (!IosStyleConfig.unreadBadgeOnBackButton.Bool()) {
            actionBar.setIOSUnreadBadgeAvailable(false)
            return
        }
        val count = MessagesStorage.getInstance(account).mainUnreadCount
        actionBar.setIOSUnreadBadgeAvailable(count > 0)
        badge.checkUnreadView(count)
        actionBar.updateBackPillWidth(true)
    }
}
