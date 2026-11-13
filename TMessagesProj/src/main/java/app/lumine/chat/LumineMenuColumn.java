/*
 * luminegram - iOS style message menu.
 * The long-press menu becomes a card that sits beside the message bubble:
 * the post is pinned, the column of menu items can scroll, and the whole
 * scrim shifts with it. ChatActivity owns the scrim fields, this class
 * owns every number and the overlay view that carries the menu card.
 */
package app.lumine.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static org.telegram.messenger.AndroidUtilities.dp;

import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.ChatScrimPopupContainerLayout;
import org.telegram.ui.Components.ReactionsContainerLayout;

public final class LumineMenuColumn {

    public static final int RADIUS = 24;
    public static final int STOCK_RADIUS = 12;
    public static final int MIN_WIDTH = 240;

    private LumineMenuColumn() {
    }

    public static boolean enabled() {
        return LumineChatConfig.getIosMenu();
    }

    public static int radiusDp() {
        return enabled() ? RADIUS : STOCK_RADIUS;
    }

    public static Column create(Context context, ChatMessageCell cell, ChatScrimPopupContainerLayout menu, ReactionsContainerLayout reactions, Runnable dismiss) {
        if (!enabled() || cell.getMessageObject() == null) {
            return null;
        }
        return new Column(context, cell, menu, reactions != null && reactions.getParent() != null ? reactions : null, dismiss);
    }

    public static final class Column {

        public float shift;
        public float clipTop;
        public Runnable onChanged;

        private final ChatMessageCell cell;
        private final ChatScrimPopupContainerLayout menu;
        private final ReactionsContainerLayout reactions;
        private final Runnable dismiss;
        private final Overlay view;

        private float scroll;
        private float minScroll;
        private float viewTop;
        private float viewBottom;
        private float postLeft;
        private float postRight;
        private float originalTop;
        private float postHeight;
        private View content;
        private boolean closing;

        private Column(Context context, ChatMessageCell messageCell, ChatScrimPopupContainerLayout menuLayout, ReactionsContainerLayout reactionsLayout, Runnable close) {
            cell = messageCell;
            menu = menuLayout;
            reactions = reactionsLayout;
            dismiss = close;
            final View first = menuLayout.getChildAt(0);
            if (first != null) {
                first.setMinimumWidth(dp(MIN_WIDTH));
            }
            for (int i = 0; i < menuLayout.getChildCount(); i++) {
                final ViewGroup.LayoutParams lp = menuLayout.getChildAt(i).getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    final ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) lp;
                    margins.setMarginStart(0);
                    margins.setMarginEnd(0);
                    margins.leftMargin = 0;
                    margins.rightMargin = 0;
                }
            }
            if (reactionsLayout != null) {
                final ViewGroup parent = (ViewGroup) reactionsLayout.getParent();
                if (parent != null) {
                    parent.removeView(reactionsLayout);
                }
                menuLayout.setReactionsLayout(null);
            }
            view = new Overlay(context, menuLayout, reactionsLayout, new Overlay.Listener() {
                private final int[] location = new int[2];

                @Override
                public boolean contains(float screenX, float screenY) {
                    content.getLocationOnScreen(location);
                    final float x = screenX - location[0];
                    final float y = screenY - location[1];
                    final float columnTop = viewTop + scroll;
                    final float top = Math.max(columnTop, minScroll < 0f ? viewTop : 0f);
                    return x >= postLeft && x <= postRight && y >= top && y <= columnTop + postHeight;
                }

                @Override
                public boolean canScroll() {
                    return minScroll < 0f;
                }

                @Override
                public void scroll(float delta) {
                    scrollBy(delta);
                }

                @Override
                public void dismiss() {
                    Column.this.dismiss.run();
                }
            });
        }

        public View getView() {
            return view;
        }

        public boolean close(Runnable done) {
            if (closing) {
                return true;
            }
            closing = true;
            view.animate().alpha(0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    done.run();
                }
            }).start();
            return true;
        }

        private void scrollBy(float delta) {
            if (minScroll >= 0f) {
                return;
            }
            applyScroll(Utilities.clamp(scroll + delta, minScroll, 0f));
        }

        private void applyScroll(float value) {
            scroll = value;
            shift = viewTop + scroll - originalTop;
            view.setMenuTranslation(scroll);
            if (onChanged != null) {
                onChanged.run();
            }
        }

        public void show(ActionBarPopupWindow window, View list, View contentView, float viewportTop, float viewportBottom) {
            content = contentView;
            final int[] location = new int[2];
            final int[] position = new int[2];
            contentView.getLocationOnScreen(location);
            list.getLocationOnScreen(position);
            final float listLeft = position[0] - location[0];
            cell.getLocationOnScreen(position);
            final float cellX = position[0] - location[0];
            final float cellTop = position[1] - location[1];
            final float cellY = cellTop + cell.getPaddingTop();
            postLeft = cellX + cell.getBackgroundDrawableLeft();
            postRight = cellX + cell.getBackgroundDrawableRight();
            originalTop = cellY + cell.getBackgroundDrawableTop();
            postHeight = Math.max(cellY + cell.getBackgroundDrawableBottom(), cellTop + cell.getMeasuredHeight()) - originalTop;
            final org.telegram.messenger.MessageObject.GroupedMessages group = cell.getCurrentMessagesGroup();
            if (group != null && list instanceof ViewGroup) {
                float l = Float.MAX_VALUE, r = -Float.MAX_VALUE, t = Float.MAX_VALUE, b = -Float.MAX_VALUE;
                final ViewGroup parent = (ViewGroup) list;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    final View c = parent.getChildAt(i);
                    if (!(c instanceof ChatMessageCell)) {
                        continue;
                    }
                    final ChatMessageCell grouped = (ChatMessageCell) c;
                    if (grouped.getCurrentMessagesGroup() != group) {
                        continue;
                    }
                    grouped.getLocationOnScreen(position);
                    final float cx = position[0] - location[0];
                    final float top = position[1] - location[1];
                    final float cy = top + grouped.getPaddingTop();
                    l = Math.min(l, cx + grouped.getBackgroundDrawableLeft());
                    r = Math.max(r, cx + grouped.getBackgroundDrawableRight());
                    t = Math.min(t, cy + grouped.getBackgroundDrawableTop());
                    b = Math.max(b, Math.max(cy + grouped.getBackgroundDrawableBottom(), top + grouped.getMeasuredHeight()));
                }
                if (l <= r && t <= b) {
                    postLeft = l;
                    postRight = r;
                    originalTop = t;
                    postHeight = b - t;
                }
            }

            menu.setMaxHeight(Math.round(viewportBottom - viewportTop));
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(Math.max(1, list.getWidth() - dp(12)), View.MeasureSpec.AT_MOST);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            menu.measure(widthSpec, heightSpec);
            int menuWidth = menu.getMeasuredWidth();
            int menuHeight = menu.getMeasuredHeight();

            final int reactionsHeight = reactions != null ? view.measureReactions(list.getWidth() - dp(12)) : 0;
            final int reactionsInset = reactionsHeight > 0 ? reactionsHeight + dp(4) : 0;

            viewTop = viewportTop + reactionsInset;
            viewBottom = viewportBottom;
            final float total = postHeight + menuHeight;
            final float available = viewBottom - viewTop;
            final boolean menuAbove = originalTop + total > viewBottom
                    && originalTop - menuHeight >= viewTop
                    && originalTop + postHeight <= viewBottom;

            if (menuAbove) {
                minScroll = 0f;
                clipTop = 0f;
                viewTop = originalTop;
                scroll = 0f;
            } else if (total <= available) {
                minScroll = 0f;
                clipTop = 0f;
                viewTop = Utilities.clamp(originalTop, viewTop, viewBottom - total);
                scroll = 0f;
            } else {
                final float visiblePost = Math.min(postHeight, Math.max(available * 0.25f, dp(120)));
                final int room = Math.round(available - visiblePost);
                if (menuHeight > room) {
                    menu.setMaxHeight(room);
                    menu.measure(widthSpec, heightSpec);
                    menuWidth = menu.getMeasuredWidth();
                    menuHeight = menu.getMeasuredHeight();
                }
                minScroll = -(postHeight + menuHeight - available);
                clipTop = viewTop;
                scroll = Math.max(minScroll, -(postHeight - visiblePost));
            }
            shift = viewTop + scroll - originalTop;

            final float viewportLeft = listLeft + dp(6);
            final float viewportRight = listLeft + list.getWidth() - dp(6);
            final View card = menu.getChildAt(0);
            final int cardPaddingLeft = card != null ? card.getPaddingLeft() : 0;
            final int cardPaddingRight = card != null ? card.getPaddingRight() : 0;
            final boolean out = cell.getMessageObject().isOutOwner();
            final float menuLeft = Utilities.clamp(out ? postRight - menuWidth + cardPaddingRight : postLeft - cardPaddingLeft,
                    viewportLeft - cardPaddingLeft, Math.max(viewportLeft, viewportRight - menuWidth + cardPaddingRight));

            view.setMenuPosition(location[0] + Math.round(menuLeft), location[1] + Math.round(menuAbove ? viewTop - reactionsInset - menuHeight : viewTop + postHeight), menuWidth, menuHeight);
            view.setMenuTranslation(scroll);
            if (reactionsHeight > 0) {
                final float reactionsLeft = Utilities.clamp(out ? postRight - view.reactionsWidth : postLeft, viewportLeft, Math.max(viewportLeft, viewportRight - view.reactionsWidth));
                view.setReactionsPosition(location[0] + Math.round(reactionsLeft), location[1] + Math.round(viewTop) - reactionsHeight - dp(4));
            }

            window.setAnimationStyle(0);
            window.setAnimationEnabled(false);
            window.setLayoutInScreen(true);
            window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            menu.setAlpha(0f);
            menu.animate().alpha(1f).setDuration(180).start();
        }
    }

    public static final class Overlay extends FrameLayout {

        interface Listener {
            boolean contains(float screenX, float screenY);

            boolean canScroll();

            void scroll(float delta);

            void dismiss();
        }

        private final View menu;
        private final ReactionsContainerLayout reactions;
        private final Listener listener;
        private final int touchSlop;
        private final int[] position = new int[2];
        private int menuX;
        private int menuY;
        private int menuWidth = 1;
        private int menuHeight = 1;
        private int reactionsX;
        private int reactionsY;
        private int reactionsWidth;
        private int reactionsHeight;
        private int pointerId = -1;
        private float downY;
        private float lastY;
        private boolean dragging;
        private boolean capturing;

        private Overlay(Context context, View menuView, ReactionsContainerLayout reactionsLayout, Listener menuListener) {
            super(context);
            menu = menuView;
            reactions = reactionsLayout;
            listener = menuListener;
            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            addView(menuView);
            if (reactionsLayout != null) {
                reactionsLayout.setPadding(dp(4), dp(4), dp(4), dp(22));
                addView(reactionsLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(74)));
            }
            setFocusableInTouchMode(true);
            setClipChildren(false);
        }

        void setMenuPosition(int x, int y, int width, int height) {
            menuX = x;
            menuY = y;
            menuWidth = Math.max(1, width);
            menuHeight = Math.max(1, height);
            requestLayout();
        }

        void setReactionsPosition(int x, int y) {
            reactionsX = x;
            reactionsY = y;
            requestLayout();
        }

        int measureReactions(int availableWidth) {
            if (reactions == null) {
                return 0;
            }
            reactionsWidth = Math.max(1, Math.min(availableWidth, Math.max(reactions.getTotalWidth(), dp(48))));
            reactions.getLayoutParams().width = reactionsWidth;
            reactions.getLayoutParams().height = dp(74) + Math.round(reactions.getTopOffset());
            reactions.measure(View.MeasureSpec.makeMeasureSpec(reactionsWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(reactions.getLayoutParams().height, View.MeasureSpec.EXACTLY));
            reactions.measureHint();
            reactionsWidth = Math.max(1, Math.min(availableWidth, Math.max(reactionsWidth, reactions.getHintTextWidth() + dp(24))));
            reactions.getLayoutParams().width = reactionsWidth;
            reactionsHeight = reactions.getLayoutParams().height;
            reactions.measure(View.MeasureSpec.makeMeasureSpec(reactionsWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(reactionsHeight, View.MeasureSpec.EXACTLY));
            return reactionsHeight;
        }

        void setMenuTranslation(float value) {
            menu.setTranslationY(value);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
            menu.measure(View.MeasureSpec.makeMeasureSpec(menuWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), View.MeasureSpec.AT_MOST));
            if (reactions != null) {
                reactions.measure(View.MeasureSpec.makeMeasureSpec(reactionsWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(reactionsHeight, View.MeasureSpec.EXACTLY));
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            getLocationOnScreen(position);
            int x = menuX - position[0];
            int y = menuY - position[1];
            menu.layout(x, y, x + menu.getMeasuredWidth(), y + menu.getMeasuredHeight());
            if (reactions != null) {
                x = reactionsX - position[0];
                y = reactionsY - position[1];
                reactions.layout(x, y, x + reactionsWidth, y + reactionsHeight);
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                pointerId = -1;
                dragging = false;
                capturing = false;
                downY = event.getY();
                lastY = event.getY();
                if (listener.contains(event.getRawX(), event.getRawY())) {
                    pointerId = event.getPointerId(0);
                    capturing = true;
                    return true;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (!capturing && listener.canScroll() && Math.abs(event.getY() - downY) > touchSlop) {
                    pointerId = event.getPointerId(0);
                    capturing = true;
                    dragging = true;
                    lastY = event.getY();
                    return true;
                }
            }
            return super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            final int action = event.getActionMasked();
            if (pointerId < 0) {
                if (action == MotionEvent.ACTION_DOWN) {
                    listener.dismiss();
                }
                return true;
            }
            final int index = event.findPointerIndex(pointerId);
            if (index < 0) {
                pointerId = -1;
                return true;
            }
            final float y = event.getY(index);
            if (action == MotionEvent.ACTION_MOVE) {
                if (!dragging && Math.abs(y - downY) > touchSlop) {
                    dragging = true;
                }
                if (dragging) {
                    listener.scroll(y - lastY);
                }
                lastY = y;
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                final boolean dismissOnTap = action == MotionEvent.ACTION_UP && !dragging;
                pointerId = -1;
                dragging = false;
                capturing = false;
                if (dismissOnTap) {
                    listener.dismiss();
                }
            }
            return true;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                listener.dismiss();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
