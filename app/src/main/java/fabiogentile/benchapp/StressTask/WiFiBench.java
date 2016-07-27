package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class WiFiBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "WiFiBench";
    private MainActivityI listener;
    private Object syncToken;

    public WiFiBench(MainActivityI listener, Object token) {
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

            Log.i(TAG, "doInBackground: start script");

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(su.getInputStream()));

            //USAGE: IP PORT START_RATE END_RATE PACKET_PER_RATE PAYLOAD_SIZE
            outputStream.writeBytes("/system/xbin/SocketBench 192.168.1.20 29000 10 20 10 20 2>&1 >> /sdcard/BENCHMARK/output\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();

//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                Log.i(TAG, "doInBackground: " + line);
//            }
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
