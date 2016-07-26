package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class WiFiBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "WiFiBench";
    private MainActivityI listener;
    private String payload = "";

    public WiFiBench(MainActivityI listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: launch script");
            //Process process = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/lcd_test.sh");
            Process process = Runtime.getRuntime().exec("/system/xbin/SocketBench 192.168.1.20 29000 10 50 10 20");
            process.waitFor();
            Log.i(TAG, "doInBackground: script terminated");

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        listener.WiFiTaskCompleted();
    }

}
