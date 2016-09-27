package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

import fabiogentile.benchapp.CallbackInterfaces.SortActivityI;
import it.polito.softeng.sort.PowerSortExperiment;

/**
 * Created by Fabio Gentile on 23/09/16.
 */
public class SortBench extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SorctBench";
    private static final String CONFIG_FILE_PATH = "/sdcard/SortTest/config.json";
    private PowerSortExperiment sorter;
    private SortActivityI callbackI;
    private String sortAlgorithm;
    private int arraySize;
    private int markerLength;
    private int runsNumber;

    public SortBench(SortActivityI iface) {
        this.callbackI = iface;

        // read config.json
        JSONParser parser = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader(CONFIG_FILE_PATH));
            JSONObject jsonObject = (JSONObject) obj;

//            JSONArray companyList = (JSONArray) jsonObject.get("Company List");

            this.sortAlgorithm = (String) jsonObject.get("method");
            this.arraySize = (int) (long) jsonObject.get("array_size");
            this.markerLength = (int) (long) jsonObject.get("marker_length");
            this.runsNumber = (int) (long) jsonObject.get("runs_number");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.i(TAG, "doInBackground: marker_length: " + this.markerLength
                + " algo: \"" + this.sortAlgorithm + "\" item_number: "
                + this.arraySize + " repetitions: " + this.runsNumber);

        try {
            sorter = new PowerSortExperiment(sortAlgorithm, this.arraySize,
                    PowerSortExperiment.SortingType.RANDOM1);

            sorter.setNRuns(this.runsNumber);
            sorter.setMarkerLength(this.markerLength);
            Log.i(TAG, "doInBackground: Lancio l'esecuzione ");
            long time = sorter.runExperiment();

            Log.i(TAG, "doInBackground: " + (-time));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "doInBackground: cannot found algorithm + " + this.sortAlgorithm);
        }



        return null;
    }

    protected void onPostExecute(Void result) {
        callbackI.sortTaskCompleted();
    }


}
