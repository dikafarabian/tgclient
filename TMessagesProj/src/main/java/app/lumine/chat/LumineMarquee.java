/*
 * luminegram - auto-scrolling chat title.
 * A long chat title runs through the header instead of being cut. Stock
 * SimpleTextView already knows how to scroll; this class only flips the
 * three flags that decide between scrolling and the gradient ellipsize.
 */
package app.lumine.chat;

import org.telegram.ui.ActionBar.SimpleTextView;

public final class LumineMarquee {

    private LumineMarquee() {
    }

    public static boolean enabled() {
        return LumineChatConfig.getChatTitleMarquee();
    }

    /*
     * Applied once when the container is built and again on every setTitle,
     * because theme code can turn the gradient ellipsize back on.
     */
    public static void apply(SimpleTextView title) {
        final boolean marquee = enabled();
        title.setScrollNonFitText(marquee);
        title.setEllipsizeByGradient(!marquee, false);
        title.setRightDrawableOutside(!marquee);
    }
}
