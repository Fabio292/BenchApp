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
    private int coreNumbers = 1;
    private MainActivityI listener;
    private Object syncToken;
    private CpuManager cpuManager = CpuManager.getInstance();

    public CpuBench(MainActivityI listener, Object token, SharedPreferences prefs) {
        this.listener = listener;
        this.syncToken = token;
        this.frequencyDuration = Integer.parseInt(prefs.getString("cpu_state_duration", "5"));
        this.waitLcdOff = prefs.getBoolean("general_turn_off_monitor", true);
        this.coreNumbers = Integer.parseInt(prefs.getString("cpu_core_number", "1")) - 1;   // subtract 1 for zero-based value

        //Limit coreNumbers range
        if (coreNumbers < 0)
            coreNumbers = 0;
        else if (coreNumbers > cpuManager.coreNumberZERO)
            coreNumbers = cpuManager.coreNumberZERO;

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

            Log.i(TAG, "doInBackground: CORE " + coreNumbers);

            cpuManager.marker();
            cpuManager.turnOnCores(coreNumbers);

            for (CpuManager.AVAILABLE_FREQUENCY f : CpuManager.AVAILABLE_FREQUENCY.values()) {

                for (int i = 0; i <= coreNumbers; i++) {
                    cpuManager.setFrequency(f, i);
                }

                String cmd = "su -c sh /sdcard/BENCHMARK/cpu_test.sh "
                        + frequencyDuration + " 2>&1";
                Process su = Runtime.getRuntime().exec(cmd);

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(su.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.i(TAG, "doInBackground: " + cpuManager.getfrequency(0) + " " + line);
                }
                su.waitFor();

            }

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
