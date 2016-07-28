package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class CpuBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    private MainActivityI listener;
    private Object syncToken;

    public CpuBench(MainActivityI listener, Object token) {
        this.listener = listener;
        this.syncToken = token;
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


            // TODO: 22/07/16 Insert marker

            Log.i(TAG, "doInBackground: start script");
            //USAGE: duration for each frequency (seconds)
            Process su = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/cpu_test.sh 1 >  /sdcard/BENCHMARK/result");
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
