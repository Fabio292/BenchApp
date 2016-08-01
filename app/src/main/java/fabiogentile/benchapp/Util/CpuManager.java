package fabiogentile.benchapp.Util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Fabio Gentile on 01/08/16.
 */
public class CpuManager {
    private static final String BASE_CPU_DIR = "/sys/devices/system/cpu";
    private static CpuManager ourInstance = new CpuManager();
    private static int[] frequencies = {300000, 422400, 652800, 729600, 883200, 960000, 1036800, 1190400, 1267200, 1497600, 1574400, 1728000, 1958400, 2265600};
    private static String[] governors = {"interactive", "conservative", "ondemand", "userspace", "powersave", "performance"};
    private static int coreNumber = -1;

    private CpuManager() {
        coreNumber = getCoreNumber() - 1;
    }

    public static CpuManager getInstance() {
        return ourInstance;
    }

    public void setFrequency(AVAILABLE_FREQUENCY freq) {

    }

    public int getfrequency() {
        return 0;
    }

    public String getGovernor() {
        return "";
    }

    public void setGovernor(AVAILABLE_GOVERNORS gov) {

    }

    /**
     * Turn on cores from 0 to index
     *
     * @param index
     */
    public void turnOnCores(int index) {

        if (index >= 0 && index < coreNumber) {
            try {
                Process process = Runtime.getRuntime().exec("echo 1 > " + BASE_CPU_DIR + "/cpu" + index + "/online");
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Check if specified core is online
     *
     * @param index core index
     * @return true if online
     */
    public boolean isCoreOnline(int index) {
        boolean ret = false;

        if (index >= 0 && index < coreNumber) {
            Process process = null;
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
     *
     * @return number of cores
     */
    public int getCoreNumber() {
        int ret = 1;


        Process process = null;
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
}
