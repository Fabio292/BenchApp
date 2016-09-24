package fabiogentile.benchapp.StressTask;

import android.os.AsyncTask;
import android.util.Log;

import it.polito.softeng.sort.PowerSortExperiment;

/**
 * Created by Fabio Gentile on 23/09/16.
 */
public class SortBench extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SorctBench";
    private PowerSortExperiment sorter;

    public SortBench() {

    }

    @Override
    protected Void doInBackground(Void... params) {

        sorter = new PowerSortExperiment("bubble", 10000, PowerSortExperiment.SortingType.RANDOM1);

        sorter.setMarkerLength(3000);
        long time = sorter.runExperiment();

        Log.i(TAG, "doInBackground: " + time);

        return null;
    }


}
