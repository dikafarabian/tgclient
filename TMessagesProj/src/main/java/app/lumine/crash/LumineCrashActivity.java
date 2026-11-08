/*
 * luminegram - crash report screen.
 * Runs in the :lumine_crash process and uses no Telegram classes, so it cannot be broken by the state that crashed.
 */
package app.lumine.crash;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

public class LumineCrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String saved = getIntent() != null ? getIntent().getStringExtra(LumineCrashLog.EXTRA_REPORT) : null;
        if (saved == null) {
            saved = LumineCrashLog.readSaved(this);
        }
        final String report = saved != null ? saved : "";
        final float density = getResources().getDisplayMetrics().density;
        final int pad = (int) (16 * density);

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF14181F);
        root.setFitsSystemWindows(true);
        root.setPadding(pad, pad, pad, pad);

        final TextView title = new TextView(this);
        title.setText(R.string.LumineCrashTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        final TextView hint = new TextView(this);
        hint.setText(R.string.LumineCrashHint);
        hint.setTextColor(0xFFB6BDC8);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        final LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hintParams.topMargin = (int) (6 * density);
        hintParams.bottomMargin = (int) (12 * density);
        root.addView(hint, hintParams);

        final TextView body = new TextView(this);
        body.setText(report);
        body.setTextColor(0xFFE3E6EB);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        body.setTextIsSelectable(true);
        final ScrollView scroll = new ScrollView(this);
        scroll.addView(body, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        final LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.END);
        final Button close = new Button(this);
        close.setText(R.string.LumineCrashClose);
        close.setOnClickListener(v -> finishAndRemoveTask());
        final Button copy = new Button(this);
        copy.setText(R.string.LumineCrashCopy);
        copy.setOnClickListener(v -> {
            final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("luminegram crash", report));
                Toast.makeText(this, R.string.LumineCrashCopied, Toast.LENGTH_SHORT).show();
            }
        });
        buttons.addView(close);
        buttons.addView(copy);
        root.addView(buttons, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }
}
