package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.Util.SocketTypeEnum;


public class SocketBench extends AsyncTask<String, Void, Void> {
    private final String TAG = "SocketBench";
    private MainActivityI listener;
    private Object syncToken;
    private String serverIp;
    private int serverPort;
    private int startRate;
    private int endRate;
    private int packetPerRate;
    private int payloadSize;

    public SocketBench(MainActivityI listener, Object token, SharedPreferences prefs, SocketTypeEnum type) {
        this.listener = listener;
        this.syncToken = token;

        if (type == SocketTypeEnum.WIFI) {
            this.serverIp = prefs.getString("wifi_ip_address", "127.0.0.1");
            this.serverPort = Integer.parseInt(prefs.getString("wifi_server_port", "29000"));
            this.startRate = Integer.parseInt(prefs.getString("wifi_start_rate", "5"));
            this.endRate = Integer.parseInt(prefs.getString("wifi_end_rate", "20"));
            this.packetPerRate = Integer.parseInt(prefs.getString("wifi_packet_per_rate", "10"));
            this.payloadSize = Integer.parseInt(prefs.getString("wifi_payload_size", "256"));

        } else {
            this.serverIp = prefs.getString("threeG_ip_address", "127.0.0.1");
            this.serverPort = Integer.parseInt(prefs.getString("threeG_server_port", "8080"));
            this.startRate = Integer.parseInt(prefs.getString("threeG_start_rate", "5"));
            this.endRate = Integer.parseInt(prefs.getString("threeG_end_rate", "10"));
            this.packetPerRate = Integer.parseInt(prefs.getString("threeG_packet_per_rate", "10"));
            this.payloadSize = Integer.parseInt(prefs.getString("threeG_payload_size", "256"));
        }
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
            String cmd = "/system/xbin/SocketBench "
                    + this.serverIp + " "
                    + this.serverPort + " "
                    + this.startRate + " "
                    + this.endRate + " "
                    + this.packetPerRate + " "
                    + this.payloadSize + " "
                    + netInterface + " "
                    + "2>&1 >> /sdcard/BENCHMARK/wifi_output\n";
            Log.i(TAG, "doInBackground: " + cmd);
            outputStream.writeBytes(cmd);

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
