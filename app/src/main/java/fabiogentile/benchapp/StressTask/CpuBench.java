package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class CpuBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    private MainActivityI listener;

    public CpuBench(MainActivityI listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Process su = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/cpu_test.sh 2 >  /sdcard/BENCHMARK/result");
            Log.i(TAG, "doInBackground: script started");
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
