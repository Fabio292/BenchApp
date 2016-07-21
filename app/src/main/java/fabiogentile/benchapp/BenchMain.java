package fabiogentile.benchapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class BenchMain extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "BenchMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bench_main);

        Button btn_cpu = (Button) findViewById(R.id.btn_cpu);
        btn_cpu.setOnClickListener(this);
        Button btn_wifi = (Button) findViewById(R.id.btn_wifi);
        btn_wifi.setOnClickListener(this);
        Button btn_3g = (Button) findViewById(R.id.btn_3g);
        btn_3g.setOnClickListener(this);
        Button btn_lcd = (Button) findViewById(R.id.btn_lcd);
        btn_lcd.setOnClickListener(this);
        Button btn_gps = (Button) findViewById(R.id.btn_gps);
        btn_gps.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_cpu:
                Log.i(TAG, "onClick: CPU");

                break;

            case R.id.btn_wifi:
                Log.i(TAG, "onClick: WIFI");
                break;

            case R.id.btn_3g:
                Log.i(TAG, "onClick: 3G");
                break;

            case R.id.btn_lcd:
                Log.i(TAG, "onClick: LCD");
                Intent i = new Intent(getApplicationContext(), LcdActivity.class);
                startActivity(i);
                break;

            case R.id.btn_gps:
                Log.i(TAG, "onClick: GPS");
                break;

            default:
                Log.e(TAG, "onClick: button not recognized");
                break;
        }
    }



}
