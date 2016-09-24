package fabiogentile.benchapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fabiogentile.benchapp.StressTask.SortBench;

public class SortTestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_test);

        new SortBench().execute();
    }
}
