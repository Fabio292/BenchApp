package fabiogentile.benchapp.Util;


import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class CpuManager {
    private static CpuManager ourInstance = new CpuManager();
    private final String TAG = "CpuManager";
    private final String BASE_CPU_DIR = "/sys/devices/system/cpu";
    private int[] frequencies = {300000, 422400, 652800, 729600, 883200, 960000, 1036800, 1190400, 1267200, 1497600, 1574400, 1728000, 1958400, 2265600};
    private String[] governors = {"interactive", "conservative", "ondemand", "userspace", "powersave", "performance"};
    private int coreNumberZERO = -1;
    private SharedPreferences prefs;
    private double markerHigh = 0;
    private double markerLow = 0;

    private CpuManager() {
        coreNumberZERO = getCoreNumber() - 1;
//        try {
//            su = Runtime.getRuntime().exec("su");
//            su.wait();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static CpuManager getInstance() {
        return ourInstance;
    }

    public void setPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    /**
     * Set core frequency
     *
     * @param freq  requested operating frequency
     * @param index core number
     */
    public void setFrequency(AVAILABLE_FREQUENCY freq, int index) {
        if (index >= 0 && index <= coreNumberZERO && isCoreOnline(index)) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                out.writeBytes("echo " + frequencies[freq.ordinal()] + " > " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/scaling_setspeed\n");

                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set core min frequency
     *
     * @param freq  requested min frequency
     * @param index core number
     */
    public void setMinFrequency(AVAILABLE_FREQUENCY freq, int index) {
        if (index >= 0 && index <= coreNumberZERO && isCoreOnline(index)) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                out.writeBytes("echo " + frequencies[freq.ordinal()] + " > " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/scaling_min_freq\n");

                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set core max frequency
     *
     * @param freq  requested max frequency
     * @param index core number
     */
    public void setMaxFrequency(AVAILABLE_FREQUENCY freq, int index) {
        if (index >= 0 && index <= coreNumberZERO && isCoreOnline(index)) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                int f = freq.ordinal();
                f = frequencies[f];
                out.writeBytes("echo " + f + " > " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/scaling_max_freq\n");
                out.flush();
                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return current frequency for specified core
     *
     * @param index core number
     * @return core frequency in kHz
     */
    public int getfrequency(int index) {
        int ret = 0;
        if (index >= 0 && index <= coreNumberZERO) {
            try {
                String cmd = "su -c cat " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/cpuinfo_cur_freq";
                Process process = Runtime.getRuntime().exec(cmd);

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line = bufferedReader.readLine();
                ret = Integer.parseInt(line);

                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Set core governor
     *
     * @param gov   requested governor
     * @param index core number
     */
    public void setGovernor(AVAILABLE_GOVERNORS gov, int index) {
        if (index >= 0 && index <= coreNumberZERO && isCoreOnline(index)) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                out.writeBytes("echo " + governors[gov.ordinal()] + " > " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/scaling_governor\n");

                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return current governor for specified core
     *
     * @param index core number
     * @return core governor
     */
    public String getGovernor(int index) {
        String ret = "";
        if (index >= 0 && index <= coreNumberZERO) {
            try {
                Process process = Runtime.getRuntime().exec("cat " + BASE_CPU_DIR + "/cpu" + index + "/cpufreq/scaling_governor");

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                ret = bufferedReader.readLine();

                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Turn on cores
     * @param howMany the number of cores to be turned on
     */
    private void turnOnCores(int howMany) {
        if (howMany >= 0 && howMany <= coreNumberZERO) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                for (int i = 0; i <= howMany; i++) {
                    out.writeBytes("echo 1 > " + BASE_CPU_DIR + "/cpu" + i + "/online\n");
                    //Set min frequency
                    out.writeBytes("echo " + frequencies[0] + " > " + BASE_CPU_DIR + "/cpu" + i + "/scaling_min_freq\n");
                }
                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Turn off cores
     *
     * @param howMany the number of cores to be turned off
     */
    private void turnOffCores(int howMany) {
        if (howMany >= 0 && howMany <= coreNumberZERO) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                for (int i = 1; i <= coreNumberZERO; i++) {
                    out.writeBytes("echo 0 > " + BASE_CPU_DIR + "/cpu" + i + "/online\n");
                }
                out.writeBytes("exit\n");
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/*
// To disable MP decision
$ adb root
$ adb remount
$ adb shell stop mpdecision
$ adb shell mv /system/bin/mpdecision /system/bin/mpdecision-rm

// To re-enable MP decision
$ adb root
$ adb remount
$ adb shell mv /system/bin/mpdecision-rm /system/bin/mpdecision
$ adb shell start mpdecision
*/

    /**
     * Turn off mpdecision service
     */
    public void turnOffMPDecision() {
        try {
            //Stop decision service
            Process process = Runtime.getRuntime().exec("su -c stop mpdecision");
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * turn on mpdecision service
     */
    public void turnOnMPDecision() {
        try {
            //Start decision service
            Process process = Runtime.getRuntime().exec("su -c start mpdecision");
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if specified core is online
     * @param index core index
     * @return true if online
     */
    public boolean isCoreOnline(int index) {
        boolean ret = false;

        if (index >= 0 && index <= coreNumberZERO) {
            Process process;
            try {
                process = Runtime.getRuntime().exec("cat " + BASE_CPU_DIR + "/cpu" + index + "/online");

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line = bufferedReader.readLine();
                ret = line.compareTo("1") == 0;

                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Get number of cores
     * @return number of cores
     */
    public int getCoreNumber() {
        int ret = 1;

        Process process;
        try {
            process = Runtime.getRuntime().exec("grep -c ^processor /proc/cpuinfo");

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            ret = Integer.parseInt(line);

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Perform the marker script
     */
    public void marker() {
//        //Usage: STEP_DURATION_HIGH STEP_DURATION_LOW (in ms)
//        String cmd = "su -c sh /sdcard/BENCHMARK/marker.sh "
//                + markerHigh + " "
//                + markerLow + " 2>&1";
//        Log.i(TAG, "marker " + markerHigh + " " + markerLow);
//        Process su;
//
//        try {
//            su = Runtime.getRuntime().exec(cmd);
//
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(su.getInputStream()));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                Log.i(TAG, "marker: " + line);
//            }
//
//            su.waitFor();
//            Log.i(TAG, "marker ended");
//            this.setCpuProfile(CPU_PROFILE.APP_NORMAL);
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        double val = Double.parseDouble(prefs.getString("general_marker_duration_high", "500"));
//        markerHigh = 1000.0 / val;
//        val = Double.parseDouble(prefs.getString("general_marker_duration_low", "500"));
//        markerLow = 1000.0 / val;

        markerHigh = Integer.parseInt(prefs.getString("general_marker_duration_high", "500")) / 1000.0;
        markerLow = Integer.parseInt(prefs.getString("general_marker_duration_low", "500")) / 1000.0;


        try {
            this.setCpuProfile(CPU_PROFILE.LOW_POWER);

            Log.i(TAG, "marker: LOW");
            Process markerLow = Runtime.getRuntime().exec("/system/xbin/sleep " + this.markerLow);
            markerLow.waitFor();
            Log.i(TAG, "marker: LOW END");

            this.setCpuProfile(CPU_PROFILE.HIGH_POWER);

            Log.i(TAG, "marker: HIGH");
            String cmd = "su -c sh /sdcard/BENCHMARK/marker.sh "
                    + markerHigh + " 2>&1";
            Process markerHigh = Runtime.getRuntime().exec(cmd);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(markerHigh.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "marker: HIGH " + line);
            }
            markerHigh.waitFor();
            Log.i(TAG, "marker: HIGH END");


            Log.i(TAG, "marker ended");
            this.setCpuProfile(CPU_PROFILE.APP_NORMAL);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

    /*
        MAX freq must be set BEFORE
     */
    /**
     * Set the requested cpu power profile
     *
     * @param profile requested profile
     */
    public void setCpuProfile(CPU_PROFILE profile) {
        switch (profile) {
            case AUTO:
                Log.i(TAG, "setCpuProfile: requested AUTO profile");

                //Automatic governor
                for (int i = 0; i <= coreNumberZERO; i++) {
                    setGovernor(AVAILABLE_GOVERNORS.ONDEMAND, i);
                    setMaxFrequency(AVAILABLE_FREQUENCY._2265MHz, i);
                    setMinFrequency(AVAILABLE_FREQUENCY._300MHz, i);
                }

                break;

            case APP_NORMAL:
                Log.i(TAG, "setCpuProfile: requested APP_NORMAL profile");

                //Turn on all cores
                turnOnCores(coreNumberZERO);

                for (int i = 0; i <= coreNumberZERO; i++) {
                    setGovernor(AVAILABLE_GOVERNORS.USERSPACE, i);
                    setMaxFrequency(AVAILABLE_FREQUENCY._2265MHz, i);
                    setMinFrequency(AVAILABLE_FREQUENCY._300MHz, i);
                    setFrequency(AVAILABLE_FREQUENCY._1036MHz, i);
                }

                break;

            case HIGH_POWER:
                Log.i(TAG, "setCpuProfile: requested HIGH_POWER profile");

                //Turn on all cores
                turnOnCores(coreNumberZERO);

                for (int i = 0; i <= coreNumberZERO; i++) {
                    setGovernor(AVAILABLE_GOVERNORS.USERSPACE, i);
                    setMaxFrequency(AVAILABLE_FREQUENCY._2265MHz, i);
                    setMinFrequency(AVAILABLE_FREQUENCY._300MHz, i);
                    setFrequency(AVAILABLE_FREQUENCY._2265MHz, i);
                }
                break;

            case LOW_POWER:
                Log.i(TAG, "setCpuProfile: requested LOW_POWER profile");

                //Turn off all cores
                turnOffCores(coreNumberZERO);

                setGovernor(AVAILABLE_GOVERNORS.USERSPACE, 0);
                setMaxFrequency(AVAILABLE_FREQUENCY._2265MHz, 0);
                setMinFrequency(AVAILABLE_FREQUENCY._300MHz, 0);
                setFrequency(AVAILABLE_FREQUENCY._1036MHz, 0);
//                setMinFrequency(AVAILABLE_FREQUENCY._300MHz, 0);
//                setMaxFrequency(AVAILABLE_FREQUENCY._300MHz, 0);
//                setFrequency(AVAILABLE_FREQUENCY._300MHz, 0);

                break;


            default:
                Log.e(TAG, "setCpuProfile: error with requested profile: " + profile);
        }
    }

    public enum AVAILABLE_FREQUENCY {
        _300MHz,
        _422MHz,
        _652MHz,
        _729MHz,
        _883MHz,
        _960MHz,
        _1036MHz,
        _1190MHz,
        _1267MHz,
        _1497MHz,
        _1574MHz,
        _1728MHz,
        _1958MHz,
        _2265MHz,
    }

    public enum AVAILABLE_GOVERNORS {
        INTERACTIVE,
        CONSERVATIVE,
        ONDEMAND,
        USERSPACE,
        POWERSAVE,
        PERFORMANCE
    }

    public enum CPU_PROFILE {
        APP_NORMAL,  //all core at medium freq - all freq available
        HIGH_POWER, //all cores at max freq
        LOW_POWER,  //1 core at medium freq
        AUTO
    }
}
