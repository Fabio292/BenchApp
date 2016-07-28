package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class CpuBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    public int frequencyDuration;
    private MainActivityI listener;
    private Object syncToken;

    public CpuBench(MainActivityI listener, Object token, SharedPreferences pref) {
        this.listener = listener;
        this.syncToken = token;
        this.frequencyDuration = Integer.parseInt(pref.getString("cpu_state_duration", "5"));
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Wait for screen to turn off
            synchronized (syncToken) {
                try {
                    syncToken.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "doInBackground: start script");
            // TODO: 22/07/16 Insert marker
            //USAGE: duration for each frequency (seconds)
            Process su = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/cpu_test.sh " +
                    this.frequencyDuration + " > /sdcard/BENCHMARK/cpu_output");
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
