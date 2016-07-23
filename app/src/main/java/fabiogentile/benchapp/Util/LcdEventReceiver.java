package fabiogentile.benchapp.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LcdEventReceiver extends BroadcastReceiver {
    private final String TAG = "ScreenReceiver";
    private LcdManager lcdManager = LcdManager.getInstance();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "onReceive: Schermo spento");
            lcdManager.restoreLcdTimeout();
        }
    }
}
