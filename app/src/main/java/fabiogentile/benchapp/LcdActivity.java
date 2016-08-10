package fabiogentile.benchapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import fabiogentile.benchapp.CallbackInterfaces.LcdActivityI;
import fabiogentile.benchapp.StressTask.LcdBench;
import fabiogentile.benchapp.Util.CpuManager;
import fabiogentile.benchapp.Util.LcdManager;
import fabiogentile.benchapp.Util.SimpleNotification;


public class LcdActivity extends Activity implements LcdActivityI {
    private final String TAG = "LcdActivity";
    private FrameLayout layout = null;
    private SharedPreferences prefs;
    private int colorIndex = 0;
    private int[] backgroundColorArray = {Color.BLUE, Color.GREEN, Color.RED};
    private boolean colorTest = false;
    private LcdManager lcdManager = LcdManager.getInstance();
    private SimpleNotification simpleNotificationManager = SimpleNotification.getInstance();
    private CpuManager cpuManager = CpuManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd);

        Log.i(TAG, "onCreate: launch lcd bench");

        layout = (FrameLayout) findViewById(R.id.lcd_activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        simpleNotificationManager.setContext(this.getApplicationContext());
        simpleNotificationManager.setNotificationService(
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        lcdManager.setContentResolver(getContentResolver());
        lcdManager.saveLuminosity();

        colorTest = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("lcd_color_test", true);


        applyColor(colorIndex);
        if (colorTest) {
            lcdManager.setLuminosity(255);
            cpuManager.turnOffMPDecision();
            cpuManager.setCpuProfile(CpuManager.CPU_PROFILE.APP_NORMAL);
        } else {
            lcdManager.setLuminosity(0);
            new LcdBench(this, prefs).execute(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        Log.i(TAG, "onDestroy: ");

        cpuManager.setCpuProfile(CpuManager.CPU_PROFILE.APP_NORMAL);
    }

    private void applyColor(int index) {
        if (index >= 0 && index < backgroundColorArray.length)
            layout.setBackgroundColor(backgroundColorArray[index]);
    }

    @Override
    public void LcdTaskCompleted() {
        //Go to next color
        colorIndex++;

        //Check if there are other colors
        if (colorIndex >= 0 && colorIndex < backgroundColorArray.length) {
            LcdManager.getInstance().setLuminosity(0);
            applyColor(colorIndex);
            new LcdBench(this, prefs).execute(false);
        } else {
            //When there are no more color quit the activity
            Log.i(TAG, "LcdTaskCompleted");
            simpleNotificationManager.notify("LCD", "LCD Task completed");
            lcdManager.restoreLuminosity();

            this.finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        super.dispatchTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_UP && colorTest) {
            colorIndex = (colorIndex + 1) % backgroundColorArray.length;
            applyColor(colorIndex);
        }

        return false;
    }

}

