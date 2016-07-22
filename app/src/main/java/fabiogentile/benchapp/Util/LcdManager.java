package fabiogentile.benchapp.Util;


import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;

public class LcdManager {
    private static LcdManager ourInstance = new LcdManager();
    private int currentTimeout = 0;

    private LcdManager() {
    }

    public static LcdManager getInstance() {
        return ourInstance;
    }

    public void saveLcdTimeout(ContentResolver content) {
        try {
            currentTimeout = Settings.System.getInt(content, Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void restoreLcdTimeout(ContentResolver content) {
        Settings.System.putInt(content, Settings.System.SCREEN_OFF_TIMEOUT, currentTimeout);
    }

    public void turnScreenOn(Window window) {
        // TODO: 22/07/16 Trovare un modo per accendere lo schermo
        /*window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);*/
    }

    public void turnScreenOff(ContentResolver content) {
        this.saveLcdTimeout(content);
        //Set screen timeout at 10 milliseconds
        Settings.System.putInt(content, Settings.System.SCREEN_OFF_TIMEOUT, 10);
    }
}
