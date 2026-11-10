/*
 * luminegram - static icons for the animated buttons of the input bar.
 * The emoji, voice, video, sticker and GIF buttons are drawn from Lottie files, which an icon pack
 * cannot replace. When the stock icon of a state is replaced by a pack or a built-in set, that state
 * is drawn as a plain image taken through the activity resources; every other state keeps its animation.
 * Based on exteraless/exteraless (ChatActivityEnterViewAnimatedIconView static icon mode), rewritten.
 */
package app.lumine.icons;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.ChatActivityEnterViewAnimatedIconView;

public final class LumineIconStatic {

    private LumineIconStatic() {
    }

    private static int resourceFor(ChatActivityEnterViewAnimatedIconView.State state) {
        if (state == null) {
            return 0;
        }
        switch (state) {
            case VOICE:
                return R.drawable.input_mic_pressed;
            case VIDEO:
                return R.drawable.input_video_pressed;
            case STICKER:
                return R.drawable.msg_sticker;
            case KEYBOARD:
                return R.drawable.input_keyboard;
            case GIF:
                return R.drawable.msg_gif;
            case SMILE:
                return R.drawable.input_smile;
            default:
                return 0;
        }
    }

    public static boolean apply(ChatActivityEnterViewAnimatedIconView view, ChatActivityEnterViewAnimatedIconView.State state) {
        if (!LumineIconConfig.enabled() && LumineIconConfig.baseSet() == LumineIconBase.DEFAULT) {
            return false;
        }
        int res = resourceFor(state);
        if (res == 0 || !LumineIconManager.get().replaces(res)) {
            return false;
        }
        view.setImageResource(res);
        if (state == ChatActivityEnterViewAnimatedIconView.State.VOICE) {
            view.setContentDescription(LocaleController.getString(R.string.AccDescrVoiceMessage));
        } else if (state == ChatActivityEnterViewAnimatedIconView.State.VIDEO) {
            view.setContentDescription(LocaleController.getString(R.string.AccDescrVideoMessage));
        }
        return true;
    }
}
