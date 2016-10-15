package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fabiogentile.benchapp.CallbackInterfaces.SortActivityI;
import it.polito.softeng.sort.PowerSortExperiment;
import it.polito.softeng.sort.PowerSortExperiment.SortingType;

/**
 * Created by Fabio Gentile on 23/09/16.
 */
public class SortBench extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SortBench";
    private static final String CONFIG_FILE_PATH = "/sdcard/SortTest/config.json";
    private PowerSortExperiment sorter;
    private SortActivityI callbackI;

    private int markerLength;
    private int pauseTime;
    private List<SortRunConfig> configList;

    public SortBench(SortActivityI iface) {
        this.callbackI = iface;

        try {
            String json = getStringFromFile(CONFIG_FILE_PATH);
            JSONObject jsonObject = new JSONObject(json);

            JSONArray runsArray = (JSONArray) jsonObject.get("runs");
            int len = runsArray.length();
            this.configList = new ArrayList<>(len);

            // Parse element and insert into list
            for (int i = 0; i < len; i++) {
                JSONObject configObj = runsArray.optJSONObject(i);
                if(configObj ==null)
                    continue;

                String sortAlgorithm = (String) configObj.get("method");
                int arraySize = (int) configObj.get("array_size");
                SortingType generationMethod = parseSortType((String) configObj.get("sort_type"));
                int runsNum = (int) configObj.get("runs_number");

                SortRunConfig conf =
                        new SortRunConfig(sortAlgorithm, generationMethod, arraySize, runsNum);

                this.configList.add(conf);
            }

            this.markerLength = (int) jsonObject.get("marker_length");
            this.pauseTime = (int) jsonObject.get("pause_time");
            Thread.sleep(this.markerLength);

            Log.i(TAG, "SortBench: MARKER: " + markerLength + " PAUSE: " + pauseTime);

        } catch (Exception e) {
            Log.e(TAG, "SortBench: Error parsing JSON: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            // This sleep is used to provide visual separation on the chart
            Thread.sleep(this.pauseTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (SortRunConfig s : this.configList) {

            try {
                Log.i(TAG, "doInBackground: [ALGO]: " + s.sortAlgorithm + " [SORT_TYPE]: " + s.sortType +
                        " [ARRAY_SIZE]: " + s.arraySize + " [REP]: " + s.repNumber);

                sorter = new PowerSortExperiment(s.sortAlgorithm, s.arraySize, s.sortType);
                sorter.setNRuns(s.repNumber);
                sorter.setMarkerLength(this.markerLength);

                long time = sorter.runExperiment();
                Log.i(TAG, "doInBackground: " + (-time));

            } catch (IllegalArgumentException e) {
                Log.e(TAG, "doInBackground: cannot found algorithm + " + s.sortAlgorithm);
            }
            //Ending marker

            try {
                Thread.sleep(this.pauseTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PowerSortExperiment.marker(this.markerLength);



//        try {
//            sorter = new PowerSortExperiment(sortAlgorithm, this.arraySize,
//                    PowerSortExperiment.SortingType.RANDOM1);
//
//            sorter.setNRuns(this.runsNumber);
//            sorter.setMarkerLength(this.markerLength);
//            Log.i(TAG, "doInBackground: Lancio l'esecuzione ");
//            long time = sorter.runExperiment();
//
//            Log.i(TAG, "doInBackground: " + (-time));
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "doInBackground: cannot found algorithm + " + this.sortAlgorithm);
//        }



        return null;
    }

    protected void onPostExecute(Void result) {
        callbackI.sortTaskCompleted();
    }

    /**
     * Parse the value og generation method string and return the proper enum value
     */
    private SortingType parseSortType(String generationMethod){
        switch (generationMethod.toLowerCase()){
            case "r1":
                return SortingType.RANDOM1;
            case "r2":
                return SortingType.RANDOM2;
            case "r3":
                return SortingType.RANDOM3;
            case "sort":
                return SortingType.SORTED;
            case "rev":
                return SortingType.REVERSE;
            default:
                return SortingType.RANDOM1;
        }
    }

    //<editor-fold desc="File to string">
    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
    //</editor-fold>
}


class SortRunConfig {
    public String sortAlgorithm;
    public SortingType sortType;
    public int arraySize;
    public int repNumber;

    public SortRunConfig(String p_sortAlgorithm, SortingType p_generationMethod, int p_arraySize, int p_repNumber){
        this.sortAlgorithm = p_sortAlgorithm;
        this.sortType = p_generationMethod;
        this.arraySize = p_arraySize;
        this.repNumber = p_repNumber;
    }
}
