package fabiogentile.benchapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import fabiogentile.benchapp.CallbackInterfaces.SortActivityI;
import fabiogentile.benchapp.StressTask.SortBench;

public class SortTestActivity extends AppCompatActivity implements SortActivityI {
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_test);

        startTime = System.currentTimeMillis();
        new SortBench(this).execute();
    }

    @Override
    public void sortTaskCompleted() {
        long stopTime = System.currentTimeMillis();
        TextView t = (TextView) findViewById(R.id.txt_sort_result);

        if (t != null) {
            t.setText("Execution time: " + (stopTime - startTime) + " ms");
        }

    }
}
