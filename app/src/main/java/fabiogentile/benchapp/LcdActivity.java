package fabiogentile.benchapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import fabiogentile.benchapp.CallbackInterfaces.LcdActivityI;
import fabiogentile.benchapp.StressTask.LcdBench;


public class LcdActivity extends Activity implements LcdActivityI {
    private final String TAG = "LcdActivity";
    private FrameLayout layout = null;

    private int colorIndex = 0;
    private int[] backgroundColorArray = {Color.BLUE, Color.GREEN, Color.RED};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd);

        layout = (FrameLayout) findViewById(R.id.lcd_activity);

        Log.i(TAG, "onCreate: launch lcd bench");
        applyColor(colorIndex);
        new LcdBench(this).execute();
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
            applyColor(colorIndex);
            new LcdBench(this).execute();
        } else {
            //When there are no more color quit the activity
            this.finish();
        }
    }

}

