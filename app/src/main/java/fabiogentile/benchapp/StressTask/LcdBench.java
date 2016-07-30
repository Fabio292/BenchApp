package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fabiogentile.benchapp.CallbackInterfaces.LcdActivityI;


public class LcdBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "LcdBench";
    private LcdActivityI listener;
    private int stepDuration;
    private int increment;

    public LcdBench(LcdActivityI listener, SharedPreferences prefs) {
        this.listener = listener;
        this.stepDuration = Integer.parseInt(prefs.getString("lcd_step_duration", "2000"));
        this.increment = Integer.parseInt(prefs.getString("lcd_step_increment", "5"));
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Wait for notification bar to disappear
            Thread.sleep(1000);

            Log.i(TAG, "doInBackground: launch script");
            // TODO: 28/07/16 marker

            //USAGE: STEP_DURATION (in ms) VALUE_INCREMENT(0-255)
            String cmd = "su -c sh /sdcard/BENCHMARK/lcd_test.sh "
                    + this.stepDuration + " "
                    + this.increment;
            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "doInBackground: " + line);
            }

            process.waitFor();
            Log.i(TAG, "doInBackground: script terminated");
            Thread.sleep(1000);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        listener.LcdTaskCompleted();
    }

}