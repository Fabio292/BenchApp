package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class CpuBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    public int frequencyDuration;
    private boolean waitLcdOff = true;
    private MainActivityI listener;
    private Object syncToken;

    public CpuBench(MainActivityI listener, Object token, SharedPreferences prefs) {
        this.listener = listener;
        this.syncToken = token;
        this.frequencyDuration = Integer.parseInt(prefs.getString("cpu_state_duration", "5"));
        this.waitLcdOff = prefs.getBoolean("general_turn_off_monitor", true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Wait for screen to turn off
            if (syncToken != null && waitLcdOff)
                synchronized (syncToken) {
                    try {
                        syncToken.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            // TODO: 22/07/16 Insert marker
            //USAGE: duration for each frequency (seconds)
            String cmd = "su -c sh /sdcard/BENCHMARK/cpu_test.sh " +
                    this.frequencyDuration + " 2>&1";
            Log.i(TAG, "doInBackground: start script " + cmd);
            Process su = Runtime.getRuntime().exec(cmd);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(su.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "doInBackground: " + line);
            }

            su.waitFor();
            Log.i(TAG, "doInBackground: script ended");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        listener.CpuTaskCompleted();
    }
}
