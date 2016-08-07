package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.Util.CpuManager;


public class CpuBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    public int frequencyDuration;
    private boolean waitLcdOff = true;
    private MainActivityI listener;
    private Object syncToken;
    private CpuManager cpuManager = CpuManager.getInstance();

    public CpuBench(MainActivityI listener, Object token, SharedPreferences prefs) {
        this.listener = listener;
        this.syncToken = token;
        this.frequencyDuration = Integer.parseInt(prefs.getString("cpu_state_duration", "5"));
        this.waitLcdOff = prefs.getBoolean("general_turn_off_monitor", true);

        this.cpuManager.setPreferences(prefs);
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

            cpuManager.marker();

            for (CpuManager.AVAILABLE_FREQUENCY f : CpuManager.AVAILABLE_FREQUENCY.values()) {

                for (int i = 0; i <= 3; i++) {
                    cpuManager.setFrequency(f, i);
                }

                String cmd = "su -c sh /sdcard/BENCHMARK/cpu_test.sh "
                        + frequencyDuration + " 2>&1";
                Process markerHigh = Runtime.getRuntime().exec(cmd);

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(markerHigh.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.i(TAG, "doInBackground: " + cpuManager.getfrequency(0) + " " + line);
                }
                markerHigh.waitFor();

            }
//            //USAGE: duration for each frequency (seconds)
//            String cmd = "su -c sh /sdcard/BENCHMARK/cpu_test.sh " +
//                    this.frequencyDuration + " 2>&1";
//            Log.i(TAG, "doInBackground: start script " + cmd);
//            Process su = Runtime.getRuntime().exec(cmd);
//
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(su.getInputStream()));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                Log.i(TAG, "doInBackground: " + line);
//            }
//
//            su.waitFor();
//            Log.i(TAG, "doInBackground: script ended");

            //Restore normal cpu profile
            cpuManager.setCpuProfile(CpuManager.CPU_PROFILE.APP_NORMAL);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        listener.CpuTaskCompleted();
    }
}
