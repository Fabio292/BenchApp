package fabiogentile.benchapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;

import fabiogentile.benchapp.CallbackInterfaces.LcdActivityI;
import fabiogentile.benchapp.StressTask.LcdBench;
import fabiogentile.benchapp.Util.LcdManager;
import fabiogentile.benchapp.Util.SimpleNotification;


public class LcdActivity extends Activity implements LcdActivityI {
    private final String TAG = "LcdActivity";
    private FrameLayout layout = null;
    private SharedPreferences prefs;
    private LcdManager lcdManager = LcdManager.getInstance();
    private int colorIndex = 0;
    private int[] backgroundColorArray = {Color.BLUE, Color.GREEN, Color.RED};
    private SimpleNotification simpleNotificationManager = SimpleNotification.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd);

        layout = (FrameLayout) findViewById(R.id.lcd_activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        simpleNotificationManager.setContext(this.getApplicationContext());
        simpleNotificationManager.setNotificationService(
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        Log.i(TAG, "onCreate: launch lcd bench");
        lcdManager.setContentResolver(getContentResolver());
        lcdManager.saveLuminosity();
        lcdManager.setLuminosity(0);
        applyColor(colorIndex);
        new LcdBench(this, prefs).execute(true);
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

}

