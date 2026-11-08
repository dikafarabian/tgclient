/*
 * luminegram - side drawer container.
 * A panel of min(300dp, screen - 56dp) that slides in from the left edge, with
 * a scrim, a shifted content layer, edge and panel swipes and predictive back.
 * LaunchActivity creates it only while the drawer option is on.
 */
package app.lumine.drawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.MainTabsActivity;

public class LumineDrawerContainer extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {

    private final FrameLayout drawerPanel;
    private final FrameLayout bulletinContainer;
    private final LinearLayout content;
    private final LumineDrawerMenu menuView;

    private final Paint scrimPaint = new Paint();
    private final Rect rect = new Rect();
    private final Path clipPath = new Path();
    private final float[] radii = new float[8];

    private int drawerWidth;
    private float progress;
    private boolean isOpen;
    private boolean isAnimating;
    private boolean tracking;
    private boolean startedEdgeSwipe;
    private boolean tapClosePending;
    private boolean notificationsRegistered;
    private boolean predictiveBackInProgress;
    private float predictiveBackStartProgress;
    private float startX;
    private float startY;
    private float startProgress;

    private View navigationTranslationTarget;
    private ValueAnimator animator;
    private VelocityTracker velocityTracker;

    public LumineDrawerContainer(Context context) {
        super(context);
        setVisibility(View.GONE);
        setTag("drawer_container");

        drawerWidth = calculateDrawerWidth();

        drawerPanel = new FrameLayout(context);
        drawerPanel.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        drawerPanel.setTranslationX(-drawerWidth);
        addView(drawerPanel, LayoutHelper.createFrame(drawerWidth, LayoutHelper.MATCH_PARENT, Gravity.LEFT));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        drawerPanel.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        bulletinContainer = new FrameLayout(context);
        drawerPanel.addView(bulletinContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        menuView = new LumineDrawerMenu(context);
        final LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 0);
        menuParams.weight = 1f;
        content.addView(menuView, menuParams);

        menuView.setOnItemClick(new Runnable() {
            @Override
            public void run() {
                closeDrawer(true);
            }
        });
    }

    public boolean isDrawerOpen() {
        return progress > 0.001f || isAnimating || predictiveBackInProgress;
    }

    public void openDrawer(boolean animated) {
        if (!LumineDrawer.enabled()) {
            onCloseComplete();
            return;
        }
        if (progress >= 0.999f && !isAnimating) {
            isOpen = true;
            setProgress(1f);
            return;
        }
        isOpen = true;
        updateDrawerWidth();
        super.setVisibility(View.VISIBLE);
        applyDrawerPanelPadding();
        refreshContents();
        if (animated) {
            animateProgress(1f, false);
        } else {
            setProgress(1f);
        }
    }

    public void closeDrawer(boolean animated) {
        if (progress <= 0.001f && !isAnimating) {
            isOpen = false;
            onCloseComplete();
            return;
        }
        isOpen = false;
        if (animated) {
            animateProgress(0f, false);
        } else {
            setProgress(0f);
            onCloseComplete();
        }
    }

    public void toggleDrawer() {
        if (isDrawerOpen()) {
            closeDrawer(true);
        } else {
            openDrawer(true);
        }
    }

    public void onAccountChanged() {
        refreshContents();
    }

    public void dispose() {
        cancelAnimations();
        onCloseComplete();
        recycleVelocityTracker();
        unregisterNotifications();
        resetNavigationTranslationTarget();
    }

    public boolean startPredictiveBack() {
        if (predictiveBackInProgress || tracking || startedEdgeSwipe || getVisibility() != View.VISIBLE) {
            return false;
        }
        if (isAnimating) {
            cancelAnimations();
        }
        if (progress <= 0.001f) {
            return false;
        }
        predictiveBackInProgress = true;
        predictiveBackStartProgress = progress;
        tapClosePending = false;
        super.setVisibility(View.VISIBLE);
        return true;
    }

    public void updatePredictiveBackProgress(float backProgress) {
        if (!predictiveBackInProgress) {
            return;
        }
        setProgress(predictiveBackStartProgress * (1f - Math.max(0f, Math.min(1f, backProgress)) * 0.5f));
    }

    public void cancelPredictiveBack() {
        if (!predictiveBackInProgress) {
            return;
        }
        predictiveBackInProgress = false;
        isOpen = predictiveBackStartProgress > 0.001f;
        animateProgress(predictiveBackStartProgress, true);
    }

    public void commitPredictiveBack() {
        if (!predictiveBackInProgress) {
            closeDrawer(true);
            return;
        }
        predictiveBackInProgress = false;
        isOpen = false;
        if (progress <= 0.001f) {
            onCloseComplete();
        } else {
            animateProgress(0f, true);
        }
    }

    public boolean handleEdgeSwipeIntercept(MotionEvent ev) {
        if (!LumineDrawer.enabled()) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startX = ev.getX();
            startY = ev.getY();
            startProgress = progress;
            startedEdgeSwipe = false;
            tracking = false;
            if (canStartClosedDrawerSwipe(ev)) {
                startedEdgeSwipe = true;
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.clear();
                velocityTracker.addMovement(ev);
            }
            return false;
        }
        if (startedEdgeSwipe) {
            if (velocityTracker != null) {
                velocityTracker.addMovement(ev);
            }
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                final float dx = ev.getX() - startX;
                final float dy = ev.getY() - startY;
                if (shouldBlockClosedDrawerSwipe(dx, dy)) {
                    startedEdgeSwipe = false;
                    return false;
                }
                if (shouldStartClosedDrawerTracking(dx, Math.abs(dy))) {
                    beginClosedDrawerTracking(ev, dx);
                    return true;
                }
            }
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                startedEdgeSwipe = false;
            }
        }
        return false;
    }

    public boolean handleEdgeSwipeTouch(MotionEvent ev) {
        if (!LumineDrawer.enabled()) {
            return false;
        }
        if (!startedEdgeSwipe && !tracking) {
            return false;
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            if (tracking) {
                setProgress(Math.max(0f, Math.min(1f, startProgress + (ev.getX() - startX) / drawerWidth)));
            }
            return true;
        }
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            return true;
        }
        if (tracking) {
            finishTracking();
        }
        startedEdgeSwipe = false;
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isClosingAnimationInProgress()) {
            return !shouldPassClosingTouchThrough(ev);
        }
        if (ev.getAction() != MotionEvent.ACTION_DOWN) {
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                final float dx = ev.getX() - startX;
                if (shouldStartVisibleDrawerTracking(dx, Math.abs(ev.getY() - startY))) {
                    beginVisibleDrawerTracking(ev, dx);
                    return true;
                }
            }
            return false;
        }
        if (isAnimating) {
            cancelAnimations();
        }
        startX = ev.getX();
        startY = ev.getY();
        startProgress = progress;
        tracking = false;
        tapClosePending = ev.getX() > drawerPanel.getTranslationX() + drawerWidth;
        return tapClosePending;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
        if (isClosingAnimationInProgress()) {
            return !shouldPassClosingTouchThrough(ev);
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (isAnimating) {
                cancelAnimations();
            }
            startX = ev.getX();
            startY = ev.getY();
            startProgress = progress;
            tracking = false;
            tapClosePending = ev.getX() > drawerPanel.getTranslationX() + drawerWidth;
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (!tracking) {
                final float dx = ev.getX() - startX;
                if (shouldStartVisibleDrawerTracking(dx, Math.abs(ev.getY() - startY))) {
                    beginVisibleDrawerTracking(ev, dx);
                }
            }
            if (tracking) {
                setProgress(Math.max(0f, Math.min(1f, startProgress + (ev.getX() - startX) / drawerWidth)));
            }
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (tracking) {
                finishTracking();
                return true;
            }
            if (action == MotionEvent.ACTION_UP && tapClosePending) {
                tapClosePending = false;
                closeDrawer(true);
                return true;
            }
            tapClosePending = false;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (progress <= 0f) {
            super.dispatchDraw(canvas);
            return;
        }
        final float clamped = Math.max(0f, Math.min(1f, progress));
        final int alpha;
        int red = 0;
        int green = 0;
        int blue = 0;
        if (!LumineDrawer.immersive() || AndroidUtilities.isTablet()) {
            alpha = (int) (clamped * 102f);
        } else {
            alpha = (int) (clamped * 160f);
            final int color = Theme.getColor(Theme.key_windowBackgroundWhite);
            red = Color.red(color);
            green = Color.green(color);
            blue = Color.blue(color);
        }
        scrimPaint.setColor(Color.argb(alpha, red, green, blue));
        canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child != drawerPanel || LumineDrawer.immersive()) {
            return super.drawChild(canvas, child, drawingTime);
        }
        final float radius = AndroidUtilities.dp(24);
        radii[0] = 0;
        radii[1] = 0;
        radii[2] = radius;
        radii[3] = radius;
        radii[4] = radius;
        radii[5] = radius;
        radii[6] = 0;
        radii[7] = 0;
        final RectF bounds = AndroidUtilities.rectTmp;
        bounds.set(child.getX(), child.getY(), child.getX() + child.getWidth(), child.getY() + child.getHeight());
        clipPath.rewind();
        clipPath.addRoundRect(bounds, radii, Path.Direction.CW);
        final int save = canvas.save();
        canvas.clipPath(clipPath);
        final boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(save);
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDrawerWidth();
        setProgress(progress);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerNotifications();
        Bulletin.addDelegate(bulletinContainer, new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int tag) {
                return AndroidUtilities.navigationBarHeight;
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Bulletin.removeDelegate(bulletinContainer);
        cancelAnimations();
        onCloseComplete();
        recycleVelocityTracker();
        resetNavigationTranslationTarget();
        unregisterNotifications();
    }

    private void animateProgress(final float target, boolean fast) {
        cancelAnimations();
        isAnimating = true;
        final float targetOffset = drawerWidth * target;
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(getDrawerOffset(), targetOffset);
        animator = valueAnimator;
        valueAnimator.setDuration(getAnimationDuration(targetOffset, fast));
        valueAnimator.setInterpolator(fast ? CubicBezierInterpolator.EASE_OUT_QUINT : CubicBezierInterpolator.Emphasized);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                setDrawerOffset((float) a.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean canceled;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animator == animation) {
                    animator = null;
                }
                if (canceled) {
                    return;
                }
                isAnimating = false;
                setDrawerOffset(targetOffset);
                if (target == 0f) {
                    onCloseComplete();
                }
            }
        });
        valueAnimator.start();
    }

    private long getAnimationDuration(float target, boolean fast) {
        if (!fast) {
            return 400L;
        }
        float distance = getDrawerOffset();
        if (target > distance) {
            distance = drawerWidth - distance;
        }
        return Math.max((long) ((250f / Math.max(drawerWidth, 1)) * distance), 100L);
    }

    private void cancelAnimations() {
        if (animator != null) {
            final ValueAnimator running = animator;
            animator = null;
            running.cancel();
        }
        isAnimating = false;
        setProgress(progress);
        if (isOpen || progress > 0.001f || tracking || startedEdgeSwipe) {
            return;
        }
        onCloseComplete();
    }

    private float getDrawerOffset() {
        return drawerWidth * progress;
    }

    private void setDrawerOffset(float offset) {
        final float clamped = Math.max(0f, Math.min(drawerWidth, offset));
        setProgress(drawerWidth != 0 ? clamped / drawerWidth : 0f);
    }

    private void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        syncDrawerState();
        invalidate();
    }

    private void syncDrawerState() {
        if (progress <= 0.001f && !isAnimating && !tracking && !startedEdgeSwipe && !predictiveBackInProgress) {
            applyClosedState();
            return;
        }
        drawerPanel.setTranslationX(-drawerWidth * (1f - progress));
        translateNavigationLayout(progress <= 0.001f ? 0f : getNavigationLayoutTranslation(progress));
        if (getVisibility() != View.VISIBLE) {
            super.setVisibility(View.VISIBLE);
        }
    }

    private void applyClosedState() {
        progress = 0f;
        drawerPanel.setTranslationX(-drawerWidth);
        translateNavigationLayout(0f);
        if (getVisibility() != View.GONE) {
            super.setVisibility(View.GONE);
        }
        tapClosePending = false;
    }

    private void onCloseComplete() {
        isOpen = false;
        tracking = false;
        startedEdgeSwipe = false;
        predictiveBackInProgress = false;
        predictiveBackStartProgress = 0f;
        setProgress(0f);
        tapClosePending = false;
        menuView.clearMenu();
    }

    private float getNavigationLayoutTranslation(float value) {
        if (LumineDrawer.immersive()) {
            return drawerWidth * value;
        }
        return drawerWidth * value * 0.3f;
    }

    private void translateNavigationLayout(float translationX) {
        final ViewParent parent = getParent();
        if (!(parent instanceof DrawerLayoutContainer)) {
            resetNavigationTranslationTarget();
            return;
        }
        final DrawerLayoutContainer layoutContainer = (DrawerLayoutContainer) parent;
        final INavigationLayout navigationLayout = layoutContainer.getParentActionBarLayout();
        if (navigationLayout == null) {
            resetNavigationTranslationTarget();
            return;
        }
        final View target = resolveNavigationTranslationTarget(layoutContainer, navigationLayout);
        if (navigationTranslationTarget != null && navigationTranslationTarget != target) {
            navigationTranslationTarget.setTranslationX(0f);
        }
        navigationTranslationTarget = target;
        if (target != null) {
            target.setTranslationX(translationX);
        }
    }

    private View resolveNavigationTranslationTarget(DrawerLayoutContainer layoutContainer, INavigationLayout navigationLayout) {
        final ViewGroup view = navigationLayout.getView();
        final ViewParent parent = view.getParent();
        if (parent instanceof View && ((View) parent).getParent() == layoutContainer) {
            return (View) parent;
        }
        return view;
    }

    private void resetNavigationTranslationTarget() {
        if (navigationTranslationTarget != null) {
            navigationTranslationTarget.setTranslationX(0f);
            navigationTranslationTarget = null;
        }
    }

    private int calculateDrawerWidth() {
        return Math.min(AndroidUtilities.dp(300), AndroidUtilities.displaySize.x - AndroidUtilities.dp(56));
    }

    private void updateDrawerWidth() {
        drawerWidth = calculateDrawerWidth();
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) drawerPanel.getLayoutParams();
        lp.width = drawerWidth;
        drawerPanel.setLayoutParams(lp);
    }

    private void applyDrawerPanelPadding() {
        drawerPanel.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);
    }

    private boolean isClosingAnimationInProgress() {
        return isAnimating && !isOpen;
    }

    private boolean shouldPassClosingTouchThrough(MotionEvent ev) {
        return ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && ev.getX() > drawerPanel.getTranslationX() + drawerWidth;
    }

    private float getDrawerOpenTouchSlop() {
        return AndroidUtilities.getPixelsInCM(0.2f, true);
    }

    private float getDrawerCloseTouchSlop() {
        return AndroidUtilities.getPixelsInCM(0.4f, true);
    }

    private boolean shouldBlockClosedDrawerSwipe(float dx, float dy) {
        final float absDy = Math.abs(dy);
        float slop = AndroidUtilities.touchSlop;
        if (slop <= 0f) {
            slop = getDrawerOpenTouchSlop();
        }
        return absDy >= slop && absDy > Math.abs(dx);
    }

    private boolean shouldStartClosedDrawerTracking(float dx, float absDy) {
        return dx > 0f && dx / 3f > absDy && Math.abs(dx) >= getDrawerOpenTouchSlop();
    }

    private boolean shouldStartVisibleDrawerTracking(float dx, float absDy) {
        if (dx < 0f) {
            return Math.abs(dx) >= absDy && Math.abs(dx) >= getDrawerCloseTouchSlop();
        }
        return startProgress < 0.999f && dx > 0f && dx / 3f > absDy && dx >= getDrawerOpenTouchSlop();
    }

    private void beginClosedDrawerTracking(MotionEvent ev, float dx) {
        tracking = true;
        tapClosePending = false;
        if (isAnimating) {
            cancelAnimations();
        }
        super.setVisibility(View.VISIBLE);
        applyDrawerPanelPadding();
        refreshContents();
        offsetTrackingStart(ev, dx);
        resetTrackingVelocity(ev);
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void beginVisibleDrawerTracking(MotionEvent ev, float dx) {
        tracking = true;
        tapClosePending = false;
        if (isAnimating) {
            cancelAnimations();
        }
        offsetTrackingStart(ev, dx);
        resetTrackingVelocity(ev);
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void offsetTrackingStart(MotionEvent ev, float dx) {
        startX += Math.signum(dx) * (dx < 0f ? getDrawerCloseTouchSlop() : getDrawerOpenTouchSlop());
        startY = ev.getY();
        startProgress = progress;
    }

    private void resetTrackingVelocity(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
        velocityTracker.addMovement(ev);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private static int swipeVelocity() {
        return AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? 1250 : 850;
    }

    private void finishTracking() {
        float xVelocity = 0f;
        float yVelocity = 0f;
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            xVelocity = velocityTracker.getXVelocity();
            yVelocity = velocityTracker.getYVelocity();
        }
        final int swipe = swipeVelocity();
        final boolean shouldOpen = (progress >= 1f / (isOpen ? 1.25f : 5f) || (xVelocity >= swipe && Math.abs(xVelocity) >= Math.abs(yVelocity)))
                && (xVelocity >= 0f || Math.abs(xVelocity) < swipe);
        if (shouldOpen) {
            final boolean fast = !isOpen && Math.abs(xVelocity) >= swipe;
            isOpen = true;
            animateProgress(1f, fast);
        } else {
            final boolean fast = isOpen && Math.abs(xVelocity) >= swipe;
            isOpen = false;
            animateProgress(0f, fast);
        }
        recycleVelocityTracker();
        tracking = false;
        startedEdgeSwipe = false;
        tapClosePending = false;
    }

    private boolean canStartClosedDrawerSwipe(MotionEvent ev) {
        if (!canOpen(ev)) {
            return false;
        }
        final ViewParent parent = getParent();
        if (!(parent instanceof DrawerLayoutContainer)) {
            return false;
        }
        final INavigationLayout navigationLayout = ((DrawerLayoutContainer) parent).getParentActionBarLayout();
        if (navigationLayout == null || navigationLayout.getFragmentStack().size() != 1 || !navigationLayout.allowSwipe()) {
            return false;
        }
        final BaseFragment lastFragment = navigationLayout.getLastFragment();
        if (lastFragment != null && lastFragment.getLastSheet() != null && lastFragment.getLastSheet().attachedToParent()) {
            return false;
        }
        final ViewGroup view = navigationLayout.getView();
        if (view == null) {
            return false;
        }
        view.getHitRect(rect);
        return rect.contains((int) ev.getX(), (int) ev.getY()) && findScrollingChild(view, ev.getX() - rect.left, ev.getY() - rect.top) == null;
    }

    private boolean canOpen(MotionEvent ev) {
        final BaseFragment lastFragment = getLastFragment();
        if (lastFragment instanceof DialogsActivity) {
            return ((DialogsActivity) lastFragment).canOpenDrawerBySwipe(ev);
        }
        return false;
    }

    private View findScrollingChild(ViewGroup group, float x, float y) {
        for (int i = 0; i < group.getChildCount(); i++) {
            final View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }
            child.getHitRect(rect);
            if (!rect.contains((int) x, (int) y)) {
                continue;
            }
            if (child.canScrollHorizontally(-1)) {
                return child;
            }
            if (child instanceof ViewGroup) {
                final View found = findScrollingChild((ViewGroup) child, x - rect.left, y - rect.top);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private BaseFragment getLastFragment() {
        final ViewParent parent = getParent();
        if (!(parent instanceof DrawerLayoutContainer)) {
            return null;
        }
        final INavigationLayout navigationLayout = ((DrawerLayoutContainer) parent).getParentActionBarLayout();
        if (navigationLayout == null) {
            return null;
        }
        final BaseFragment lastFragment = navigationLayout.getLastFragment();
        return lastFragment instanceof MainTabsActivity ? ((MainTabsActivity) lastFragment).getCurrentVisibleFragment() : lastFragment;
    }

    private void refreshContents() {
        final BaseFragment lastFragment = getLastFragment();
        if (lastFragment != null) {
            menuView.rebuildMenu(UserConfig.selectedAccount, lastFragment);
        } else {
            menuView.clearMenu();
        }
    }

    private void updateColors() {
        drawerPanel.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        menuView.updateColors();
        invalidate();
    }

    private void registerNotifications() {
        if (notificationsRegistered) {
            return;
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final NotificationCenter center = NotificationCenter.getInstance(a);
            center.addObserver(this, NotificationCenter.appDidLogout);
            center.addObserver(this, NotificationCenter.attachMenuBotsDidLoad);
        }
        final NotificationCenter global = NotificationCenter.getGlobalInstance();
        global.addObserver(this, NotificationCenter.didSetNewTheme);
        global.addObserver(this, NotificationCenter.themeAccentListUpdated);
        global.addObserver(this, NotificationCenter.reloadInterface);
        notificationsRegistered = true;
    }

    private void unregisterNotifications() {
        if (!notificationsRegistered) {
            return;
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final NotificationCenter center = NotificationCenter.getInstance(a);
            center.removeObserver(this, NotificationCenter.appDidLogout);
            center.removeObserver(this, NotificationCenter.attachMenuBotsDidLoad);
        }
        final NotificationCenter global = NotificationCenter.getGlobalInstance();
        global.removeObserver(this, NotificationCenter.didSetNewTheme);
        global.removeObserver(this, NotificationCenter.themeAccentListUpdated);
        global.removeObserver(this, NotificationCenter.reloadInterface);
        notificationsRegistered = false;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didSetNewTheme) {
            updateColors();
        } else if (id == NotificationCenter.themeAccentListUpdated) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    updateColors();
                }
            });
        } else if (id == NotificationCenter.reloadInterface) {
            if (isOpen) {
                refreshContents();
            }
            updateColors();
        } else if (id == NotificationCenter.attachMenuBotsDidLoad) {
            if (account == UserConfig.selectedAccount && isOpen) {
                refreshContents();
            }
        } else if (id == NotificationCenter.appDidLogout) {
            if (isOpen) {
                closeDrawer(false);
            }
        }
    }
}
