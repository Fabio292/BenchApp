package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class SocketBench extends AsyncTask<String, Void, Void> {
    private final String TAG = "SocketBench";
    private MainActivityI listener;
    private Object syncToken;

    public SocketBench(MainActivityI listener, Object token) {
        this.listener = listener;
        this.syncToken = token;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            //Wait for screen to turn off
            synchronized (syncToken) {
                try {
                    syncToken.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String netInterface = params[0];
            Log.i(TAG, "doInBackground: start script on " + netInterface);
            // TODO: 28/07/16 marker

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            //USAGE: IP PORT START_RATE END_RATE PACKET_PER_RATE PAYLOAD_SIZE INTERFACE
            outputStream.writeBytes("/system/xbin/SocketBench 192.168.1.20 29000 10 20 10 20 " + netInterface
                    + " 2>&1 >> /sdcard/BENCHMARK/wifi_output\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            su.waitFor();
            Log.i(TAG, "doInBackground: script ended");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        listener.WiFiTaskCompleted();
    }

}
