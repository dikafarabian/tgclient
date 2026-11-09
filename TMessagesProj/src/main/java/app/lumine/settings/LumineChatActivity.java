/*
 * luminegram - Chat Interface page.
 * Options that change how the chat screens look. Every option reads and
 * writes through LumineChatConfig.
 */
package app.lumine.settings;

import android.content.Context;
import android.widget.LinearLayout;

import app.lumine.LumineStrings;
import app.lumine.chat.LumineChatConfig;
import org.telegram.messenger.R;

public class LumineChatActivity extends LumineSubPage {

    @Override
    protected int titleRes() {
        return R.string.LumineChatTitle;
    }

    @Override
    protected void build(Context context, LinearLayout list) {
        final LinearLayout card = LumineCards.card(context, list, LumineStrings.get(R.string.LumineChatHeaderInput));
        LumineCards.toggleRow(context, card, LumineStrings.get(R.string.LumineChatHideFade), null, LumineChatConfig.getHideFade(), new LumineCards.OnToggle() {
            @Override
            public void onToggle(boolean enabled) {
                LumineChatConfig.setHideFade(enabled);
            }
        });
        LumineCards.info(context, list, LumineStrings.get(R.string.LumineChatHideFadeInfo));
    }
}
