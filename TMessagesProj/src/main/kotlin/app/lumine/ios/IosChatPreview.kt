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
    private var waitAttempts = 0
    private var blankRetries = 0
    private val rebuildRunnable = Runnable { tryBuild() }
    private val blankCheckRunnable = Runnable { checkBlank() }

    init {
        clipChildren = true
        clipToPadding = true
        background = IosCheckerDrawable()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (container == null) {
            waitAttempts = 0
            blankRetries = 0
            AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
            AndroidUtilities.runOnUIThread(rebuildRunnable)
        }
    }

    fun rebuild() {
        waitAttempts = 0
        blankRetries = 0
        AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
        AndroidUtilities.runOnUIThread(rebuildRunnable, REBUILD_DELAY_MS)
    }

    fun onOpened() {
        if (container == null) {
            AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
            AndroidUtilities.runOnUIThread(rebuildRunnable)
        }
    }

    private fun tryBuild() {
        val animating = host.parentLayout?.isTransitionAnimationInProgress() == true
        if (animating && waitAttempts < MAX_WAIT_ATTEMPTS) {
            waitAttempts++
            AndroidUtilities.runOnUIThread(rebuildRunnable, WAIT_STEP_MS)
            return
        }
        waitAttempts = 0
        rebuildNow()
        AndroidUtilities.cancelRunOnUIThread(blankCheckRunnable)
        AndroidUtilities.runOnUIThread(blankCheckRunnable, BLANK_CHECK_DELAY_MS)
    }

    private fun checkBlank() {
        val current = container ?: return
        val title = current.chatActivity.avatarContainer?.titleTextView?.text
        if (title.isNullOrEmpty() && blankRetries < MAX_BLANK_RETRIES) {
            blankRetries++
            rebuildNow()
            AndroidUtilities.runOnUIThread(blankCheckRunnable, BLANK_CHECK_DELAY_MS)
        }
    }

    fun onResume() {
        container?.onResume()
    }

    fun onPause() {
        container?.onPause()
    }

    fun destroy() {
        AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
        AndroidUtilities.cancelRunOnUIThread(blankCheckRunnable)
        release()
    }

    private fun rebuildNow() {
        val fade = container == null
        release()
        val args = Bundle().apply {
            putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())
        }
        val next = ChatActivityContainer(context, host.parentLayout, args)
        next.chatActivity.stylePreview = true
        container = next
        if (fade) next.alpha = 0f
        addView(next, LayoutHelper.createFrameMatchParent())
        if (fade) next.animate().alpha(1f).setDuration(FADE_MS).start()
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
        const val WAIT_STEP_MS = 30L
        const val MAX_WAIT_ATTEMPTS = 30
        const val BLANK_CHECK_DELAY_MS = 450L
        const val FADE_MS = 140L
        const val MAX_BLANK_RETRIES = 2
        const val HEADER_ZONE_DP = 56
        const val INPUT_ZONE_DP = 56
    }
}
