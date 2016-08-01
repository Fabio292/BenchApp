package fabiogentile.benchapp.Util;


import android.content.ContentResolver;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LcdManager {
    private static final String TAG = "LcdManager";
    private static LcdManager ourInstance = new LcdManager();
    private static int currentTimeout = 0;
    private static int currentLuminosity = 1;
    private static ContentResolver content = null;
    private static PowerManager pm = null;

    private LcdManager() {
    }

    public static LcdManager getInstance() {
        return ourInstance;
    }

    public void setContentResolver(ContentResolver content) {
        LcdManager.content = content;
    }

    public void setPowerManager(PowerManager pm) {
        LcdManager.pm = pm;
    }
    /**
     * return current luminosity value
     *
     * @return 0-255 value of current luminosity
     */
    public int getLuminosity() {
        int ret = 1;
        String cmd = "cat /sys/class/leds/lcd-backlight/brightness";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            ret = Integer.parseInt(bufferedReader.readLine());

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Set luminosity of screen
     *
     * @param val 0-255 value
     */
    public void setLuminosity(int val) {
        String cmd = "su -c echo " + val + " > /sys/class/leds/lcd-backlight/brightness";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveLuminosity() {
        currentLuminosity = getLuminosity();
    }

    public void restoreLuminosity() {
        setLuminosity(currentLuminosity);
    }

    public void saveLcdTimeout() {
        try {
            currentTimeout = Settings.System.getInt(content, Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void restoreLcdTimeout() {
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

    public void turnScreenOff() {
        saveLcdTimeout();
        //Set screen timeout at 10 milliseconds
        Settings.System.putInt(content, Settings.System.SCREEN_OFF_TIMEOUT, 75);
        Log.i(TAG, "turnScreenOff");

    }
}
