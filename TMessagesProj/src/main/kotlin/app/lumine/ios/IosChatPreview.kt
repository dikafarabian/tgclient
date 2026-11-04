// ======================================================
// luminegram - iOS style live chat preview
// ======================================================

package app.lumine.ios

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.MotionEvent
import android.view.PixelCopy
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ChatActivityContainer
import org.telegram.ui.Components.LayoutHelper
import tw.nekomimi.nekogram.NekoConfig
import java.io.File
import java.io.FileOutputStream

class IosChatPreview(context: Context, private val host: BaseFragment) : FrameLayout(context) {

    private var container: ChatActivityContainer? = null
    private var touchBlocked = false
    private var waitAttempts = 0
    private var blankRetries = 0
    private var placeholder: Bitmap? = null
    private var pendingReveal = false
    private val rebuildRunnable = Runnable { tryBuild() }
    private val blankCheckRunnable = Runnable { checkBlank() }
    private val captureRunnable = Runnable { capture() }
    private var scrolling = false
    private var touching = false
    private var scrollDir = 0
    private var scrollCarry = 0f
    private var lastFrameNanos = 0L
    private var scrolledContainer: ChatActivityContainer? = null
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!scrolling) return
            stepScroll(frameTimeNanos)
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        clipChildren = true
        clipToPadding = true
        background = IosCheckerDrawable()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && container == null && placeholder == null) loadPlaceholder(w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        placeholder?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        super.dispatchDraw(canvas)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoScroll()
    }

    private fun startAutoScroll() {
        if (scrolling) return
        scrolling = true
        lastFrameNanos = 0L
        scrollCarry = 0f
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun stopAutoScroll() {
        scrolling = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun stepScroll(now: Long) {
        val last = lastFrameNanos
        lastFrameNanos = now
        val current = container
        val list = current?.chatActivity?.chatListView
        if (current !== scrolledContainer) {
            scrolledContainer = current
            scrollDir = 0
        }
        if (list == null || touching || pendingReveal || last == 0L) return
        if (scrollDir == 0) scrollDir = if (list.canScrollVertically(1)) 1 else -1
        if (!list.canScrollVertically(scrollDir)) scrollDir = -scrollDir
        scrollCarry += (now - last) / 1_000_000_000f * AndroidUtilities.dp(SCROLL_SPEED_DP)
        val px = scrollCarry.toInt()
        if (px > 0) {
            scrollCarry -= px
            list.scrollBy(0, px * scrollDir)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAutoScroll()
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
        if (placeholder != null || pendingReveal) {
            placeholder = null
            pendingReveal = false
            container?.alpha = 1f
            invalidate()
        }
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
        AndroidUtilities.cancelRunOnUIThread(captureRunnable)
        AndroidUtilities.cancelRunOnUIThread(blankCheckRunnable)
        AndroidUtilities.runOnUIThread(blankCheckRunnable, BLANK_CHECK_DELAY_MS)
    }

    private fun hasTitle(current: ChatActivityContainer): Boolean =
        !current.chatActivity.avatarContainer?.titleTextView?.text.isNullOrEmpty()

    private fun checkBlank() {
        val current = container ?: return
        val ok = hasTitle(current)
        if (!ok && blankRetries < MAX_BLANK_RETRIES) {
            blankRetries++
            rebuildNow()
            AndroidUtilities.runOnUIThread(blankCheckRunnable, BLANK_CHECK_DELAY_MS)
            return
        }
        if (pendingReveal) reveal(current)
        if (ok) {
            AndroidUtilities.cancelRunOnUIThread(captureRunnable)
            AndroidUtilities.runOnUIThread(captureRunnable, CAPTURE_DELAY_MS)
        }
    }

    private fun reveal(current: ChatActivityContainer) {
        pendingReveal = false
        current.animate().alpha(1f).setDuration(FADE_MS).withEndAction {
            placeholder = null
            invalidate()
        }.start()
    }

    fun onResume() {
        container?.onResume()
        if (isAttachedToWindow) startAutoScroll()
    }

    fun onPause() {
        stopAutoScroll()
        container?.onPause()
    }

    fun destroy() {
        stopAutoScroll()
        AndroidUtilities.cancelRunOnUIThread(rebuildRunnable)
        AndroidUtilities.cancelRunOnUIThread(blankCheckRunnable)
        AndroidUtilities.cancelRunOnUIThread(captureRunnable)
        release()
    }

    private fun rebuildNow() {
        val first = container == null
        release()
        val args = Bundle().apply {
            putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())
        }
        val next = ChatActivityContainer(context, host.parentLayout, args)
        next.chatActivity.stylePreview = true
        container = next
        if (first) pendingReveal = placeholder != null
        if (first || pendingReveal) next.alpha = 0f
        addView(next, LayoutHelper.createFrameMatchParent())
        if (first && !pendingReveal) next.animate().alpha(1f).setDuration(FADE_MS).start()
    }

    private fun release() {
        val old = container ?: return
        container = null
        removeView(old)
        old.onPause()
        old.chatActivity.onRemoveFromParent()
        old.chatActivity.onFragmentDestroy()
    }

    private fun signature(w: Int, h: Int): String {
        val user = UserConfig.getInstance(UserConfig.selectedAccount).currentUser
        val key = listOf(
            w, h, UserConfig.selectedAccount,
            user?.first_name, user?.last_name, user?.photo?.photo_id,
            Theme.isCurrentThemeDark(),
            Theme.getColor(Theme.key_actionBarDefault),
            Theme.getColor(Theme.key_windowBackgroundWhite),
            IosStyleConfig.chatHeader.Bool(),
            IosStyleConfig.chatHeaderAvatarSlot.Bool(),
            IosStyleConfig.centerTitleRightAvatar.Bool(),
            IosStyleConfig.chatTitleMarquee.Bool(),
            IosStyleConfig.unreadBadgeOnBackButtonIos.Bool(),
            NekoConfig.unreadBadgeOnBackButton.Bool(),
            NekoConfig.iOSMessageInputField.Bool(),
            IosStyleConfig.liquidGlassIntensity.Int(),
            IosStyleConfig.liquidGlassTint.Int(),
            IosStyleConfig.hideFadeViews.Bool(),
        ).joinToString("|")
        return Integer.toHexString(key.hashCode())
    }

    private fun cacheFile(w: Int, h: Int): File = File(context.cacheDir, "$FILE_PREFIX${signature(w, h)}.png")

    private fun loadPlaceholder(w: Int, h: Int) {
        try {
            val file = cacheFile(w, h)
            if (file.exists()) placeholder = BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Throwable) {
            placeholder = null
        }
        if (placeholder != null) invalidate()
    }

    private fun capture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val current = container ?: return
        if (!isAttachedToWindow || !isShown || current.alpha < 1f || pendingReveal || width <= 0 || height <= 0) return
        if (host.parentLayout?.isTransitionAnimationInProgress() == true) return
        if (!hasTitle(current)) return
        val target = cacheFile(width, height)
        if (target.exists()) return
        val window = host.parentActivity?.window ?: return
        val location = IntArray(2)
        getLocationInWindow(location)
        val rect = Rect(location[0], location[1], location[0] + width, location[1] + height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            PixelCopy.request(window, rect, bitmap, { result ->
                if (result == PixelCopy.SUCCESS) save(bitmap, target)
            }, Handler(Looper.getMainLooper()))
        } catch (e: Throwable) {
            bitmap.recycle()
        }
    }

    private fun save(bitmap: Bitmap, target: File) {
        Utilities.globalQueue.postRunnable {
            try {
                FileOutputStream(target).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                target.parentFile?.listFiles()?.forEach { file ->
                    if (file.name.startsWith(FILE_PREFIX) && file != target) file.delete()
                }
            } catch (e: Throwable) {
                target.delete()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            val inChrome = ev.y < AndroidUtilities.dp(HEADER_ZONE_DP.toFloat()) ||
                ev.y > height - AndroidUtilities.dp(INPUT_ZONE_DP.toFloat())
            touchBlocked = inChrome
        }
        if (touchBlocked) return true
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> touching = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> touching = false
        }
        return super.dispatchTouchEvent(ev)
    }

    private companion object {
        const val FILE_PREFIX = "lumine_ios_preview_"
        const val REBUILD_DELAY_MS = 150L
        const val WAIT_STEP_MS = 30L
        const val MAX_WAIT_ATTEMPTS = 30
        const val BLANK_CHECK_DELAY_MS = 450L
        const val MAX_BLANK_RETRIES = 2
        const val CAPTURE_DELAY_MS = 700L
        const val FADE_MS = 140L
        const val SCROLL_SPEED_DP = 36f
        const val HEADER_ZONE_DP = 56
        const val INPUT_ZONE_DP = 56
    }
}
