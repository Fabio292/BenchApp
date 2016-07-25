package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


/**
 * Created by Fabio Gentile on 25/07/16.
 */
public class WiFiBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "WiFiBench";
    private MainActivityI listener;
    private String payload = "";

    public WiFiBench(MainActivityI listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Load parameters from settings
        int startRate = 5;
        int endRate = 15;
        int packetPerRate = 5;

        try {
            payload = generatePayload(20); // TODO: 25/07/16 size variabile?
            Socket socket = new Socket("192.168.1.20", 29000);

            if (!socket.isConnected()) {
                Log.e(TAG, "doInBackground: non connesso");
                return null;
            }

            socket.setTcpNoDelay(true);
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.println(generatePayload(200));
            output.flush();
            Thread.sleep(1000);

            for (int rate = startRate; rate <= endRate; rate++) {
                //Calculate the time in ms between each packet
                int delayBetweenPacket = ((int) 1000.0 / rate);
                Log.i(TAG, "doInBackground: Rate = " + rate + "pps (" + delayBetweenPacket + " ms)");

                for (int i = 0; i < packetPerRate; i++) {
                    //Send packet
                    output.println(payload);
                    output.flush();

                    //Wait for the calculated time
                    Log.i(TAG, "doInBackground: a");
                    Thread.sleep(delayBetweenPacket, 0);
                    //Log.i(TAG, "doInBackground: b");
                }
            }

            //Clean up object
            output.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate a random string used as payload that match [a-z]*
     *
     * @param size length of generated payload
     */
    private String generatePayload(int size) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    protected void onPostExecute(Void result) {
        listener.WiFiTaskCompleted();
    }

}
