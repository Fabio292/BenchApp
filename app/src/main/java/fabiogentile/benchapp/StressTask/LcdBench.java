package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.LcdActivityI;


public class LcdBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "LcdBench";
    private LcdActivityI listener;

    public LcdBench(LcdActivityI listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Wait for notification bar to disappear
            Thread.sleep(1000);

            Log.i(TAG, "doInBackground: launch script");
            // TODO: 28/07/16 marker
            //USAGE: STEP_DURATION (in ms) VALUE_INCREMENT(0-255)
            Process process = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/lcd_test.sh");
            process.waitFor();
            Log.i(TAG, "doInBackground: script terminated");

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        listener.LcdTaskCompleted();
    }

}