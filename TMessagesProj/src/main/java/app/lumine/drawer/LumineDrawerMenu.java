/*
 * luminegram - scrolling list of drawer rows built from the saved layout.
 * A divider is placed lazily, so dividers at the start, at the end or in a
 * row collapse into one.
 */
package app.lumine.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

public class LumineDrawerMenu extends ScrollView {

    private final LinearLayout container;
    private final Paint topGradientPaint = new Paint();
    private LinearGradient topGradient;
    private int lastGradientColor;
    private Runnable onItemClick;

    public LumineDrawerMenu(Context context) {
        super(context);
        setVerticalScrollBarEnabled(false);
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8) + AndroidUtilities.navigationBarHeight);
        addView(container, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        updateGradient();
    }

    public void setOnItemClick(Runnable onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void clearMenu() {
        container.removeAllViews();
    }

    public void rebuildMenu(int account, BaseFragment fragment) {
        clearMenu();
        boolean hasAnyItem = false;
        boolean dividerPending = false;
        final List<Integer> layout = LumineDrawer.getLayout();
        for (int i = 0; i < layout.size(); i++) {
            final Integer id = layout.get(i);
            if (id == null) {
                continue;
            }
            if (id == LumineDrawerItem.DIVIDER.getId()) {
                if (hasAnyItem) {
                    dividerPending = true;
                }
                continue;
            }
            final List<LumineDrawerResolver.Info> items = LumineDrawerResolver.resolve(id, account, fragment);
            if (items.isEmpty()) {
                continue;
            }
            if (dividerPending) {
                final View divider = new View(getContext());
                divider.setBackgroundColor(dividerColor());
                container.addView(divider, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1f / AndroidUtilities.density, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 12, 8, 12, 8));
                dividerPending = false;
            }
            for (final LumineDrawerResolver.Info info : items) {
                final LumineDrawerItemView itemView = new LumineDrawerItemView(getContext());
                itemView.setMenuItem(id, info.iconRes, info.text);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClick != null) {
                            onItemClick.run();
                        }
                        if (info.onClick != null) {
                            info.onClick.run();
                        }
                    }
                });
                if (info.onLongClick != null) {
                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (onItemClick != null) {
                                onItemClick.run();
                            }
                            info.onLongClick.run();
                            return true;
                        }
                    });
                }
                container.addView(itemView);
            }
            hasAnyItem = true;
        }
    }

    public void updateColors() {
        for (int i = 0; i < container.getChildCount(); i++) {
            final View child = container.getChildAt(i);
            if (child instanceof LumineDrawerItemView) {
                ((LumineDrawerItemView) child).updateColors();
            } else {
                child.setBackgroundColor(dividerColor());
            }
        }
        updateGradient();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getScrollY() > 0) {
            canvas.save();
            canvas.translate(0, getScrollY());
            canvas.drawRect(0, 0, getWidth(), AndroidUtilities.dp(16), topGradientPaint);
            canvas.restore();
        }
    }

    private static int dividerColor() {
        final int color = Theme.getColor(Theme.key_divider);
        if (Color.alpha(color) == 0) {
            return Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), 0.25f);
        }
        return color;
    }

    private void updateGradient() {
        final int color = Theme.getColor(Theme.key_windowBackgroundWhite);
        if (topGradient != null && color == lastGradientColor) {
            return;
        }
        lastGradientColor = color;
        topGradient = new LinearGradient(0, 0, 0, AndroidUtilities.dp(16), new int[]{color, color & 0x00ffffff}, null, Shader.TileMode.CLAMP);
        topGradientPaint.setShader(topGradient);
        invalidate();
    }
}
