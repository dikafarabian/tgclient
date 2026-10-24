// ======================================================
// luminegram - iOS style live chat preview
// ======================================================

package app.lumine.ios

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ChatActivityContainer
import org.telegram.ui.Components.LayoutHelper

class IosChatPreview(context: Context, private val host: BaseFragment) : FrameLayout(context) {

    private var container: ChatActivityContainer? = null
    private var touchBlocked = false
    private val rebuildRunnable = Runnable { rebuildNow() }

    init {
        clipChildren = true
        clipToPadding = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (container == null) rebuildNow()
    }

    fun rebuild() {
        AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
        AndroidUtilities.runOnUIThread(rebuildRunnable, REBUILD_DELAY_MS)
    }

    fun onResume() {
        container?.onResume()
    }

    fun onPause() {
        container?.onPause()
    }

    fun destroy() {
        AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
        release()
    }

    private fun rebuildNow() {
        release()
        val args = Bundle().apply {
            putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())
        }
        val next = ChatActivityContainer(context, host.parentLayout, args)
        next.chatActivity.stylePreview = true
        container = next
        addView(next, LayoutHelper.createFrameMatchParent())
    }

    private fun release() {
        val old = container ?: return
        container = null
        removeView(old)
        old.onPause()
        old.chatActivity.onRemoveFromParent()
        old.chatActivity.onFragmentDestroy()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            val inChrome = ev.y < AndroidUtilities.dp(HEADER_ZONE_DP.toFloat()) ||
                ev.y > height - AndroidUtilities.dp(INPUT_ZONE_DP.toFloat())
            touchBlocked = inChrome
        }
        if (touchBlocked) return true
        return super.dispatchTouchEvent(ev)
    }

    private companion object {
        const val REBUILD_DELAY_MS = 150L
        const val HEADER_ZONE_DP = 64
        const val INPUT_ZONE_DP = 72
    }
}
