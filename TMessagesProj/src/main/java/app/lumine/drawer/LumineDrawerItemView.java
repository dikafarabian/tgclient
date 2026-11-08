/*
 * luminegram - one row of the side drawer: icon, bold label.
 */
package app.lumine.drawer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;

public class LumineDrawerItemView extends FrameLayout {

    private final ImageView iconView;
    private final TextView textView;
    private int itemId = Integer.MIN_VALUE;

    public LumineDrawerItemView(Context context) {
        super(context);
        setWillNotDraw(false);
        setLayoutParams(new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(48)));
        setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector), 12, 12));
        ScaleStateListAnimator.apply(this, 0.04f, 1.5f);

        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.CENTER);
        iconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
        addView(iconView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.CENTER_VERTICAL, 20, 0, 0, 0));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 68, 0, 16, 0));
    }

    public void setMenuItem(int itemId, int iconRes, CharSequence text) {
        this.itemId = itemId;
        iconView.setImageResource(iconRes);
        textView.setText(text);
    }

    public int getItemId() {
        return itemId;
    }

    public void updateColors() {
        setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector), 12, 12));
        iconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        invalidate();
    }
}
