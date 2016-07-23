package fabiogentile.benchapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.StressTask.CpuBench;
import fabiogentile.benchapp.StressTask.GpsBench;
import fabiogentile.benchapp.Util.LcdEventReceiver;
import fabiogentile.benchapp.Util.LcdManager;
import fabiogentile.benchapp.Util.SimpleNotification;


public class BenchMain extends AppCompatActivity implements View.OnClickListener, MainActivityI {
    private final String TAG = "BenchMain";
    private BroadcastReceiver mReceiver = null;
    private GpsBench gpsBench = new GpsBench(this);
    private int gpsRequestNumber;
    private LcdManager lcdManager = LcdManager.getInstance();
    private SimpleNotification simpleNotificationManager = SimpleNotification.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bench_main);

        //<editor-fold desc="UTIL class setup">
        simpleNotificationManager.setContext(this.getApplicationContext());
        simpleNotificationManager.setNotificationService(
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        lcdManager.setContentResolver(getContentResolver());
        lcdManager.saveLcdTimeout();
        //</editor-fold>

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //<editor-fold desc="ACTON SCREEN events receiver">
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LcdEventReceiver();
        registerReceiver(mReceiver, filter);
        //</editor-fold>

        //<editor-fold desc="BTN click listener ADD">
        Button btn_cpu = (Button) findViewById(R.id.btn_cpu);
        if (btn_cpu != null)
            btn_cpu.setOnClickListener(this);
        Button btn_wifi = (Button) findViewById(R.id.btn_wifi);
        if (btn_wifi != null)
            btn_wifi.setOnClickListener(this);
        Button btn_3g = (Button) findViewById(R.id.btn_3g);
        if (btn_3g != null)
            btn_3g.setOnClickListener(this);
        Button btn_lcd = (Button) findViewById(R.id.btn_lcd);
        if (btn_lcd != null)
            btn_lcd.setOnClickListener(this);
        Button btn_gps = (Button) findViewById(R.id.btn_gps);
        if (btn_gps != null)
            btn_gps.setOnClickListener(this);
        //</editor-fold>

    }

    //<editor-fold desc="TOOLBAR management">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.i(TAG, "onOptionsItemSelected: Settings selected");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //<editor-fold desc="BTN click switch">
            case R.id.btn_cpu:
                Log.i(TAG, "onClick: CPU");
                lcdManager.turnScreenOff();
                new CpuBench(this).execute();
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
                lcdManager.turnScreenOff();
                gpsRequestNumber = 1;

                Log.i(TAG, "onClick: Start gps position acquiring {" + gpsRequestNumber + "}");
                if (!gpsBench.getLocation(this))
                    Log.e(TAG, "onClick: error during gps position acquiring");
                break;

            default:
                Log.e(TAG, "onClick: button not recognized");
                break;
            //</editor-fold>
        }
    }

    @Override
    public void CpuTaskCompleted() {
        Log.i(TAG, "CpuTaskCompleted: cpu end");
        //simpleNotificationManager.notify("CPU Task completed", "Cpu task has been completed");
        simpleNotificationManager.playSound();
    }

    @Override
    public void GpsTaskCompleted(Location location) {
        if (location != null)
            Log.i(TAG, "GpsTaskCompleted: Position{" + gpsRequestNumber++ + "}: " + location.getLatitude() + " " + location.getLongitude()
                    + " " + location.getAltitude() + " accuracy: " + location.getAccuracy());
        else
            Log.e(TAG, "GpsTaskCompleted: ERROR during GPS position acquiring");

        if (gpsRequestNumber == 2) {
            Log.i(TAG, "GpsTaskCompleted: Start gps position acquiring {" + gpsRequestNumber + "}");
            if (!gpsBench.getLocation(this))
                Log.e(TAG, "GpsTaskCompleted: error during gps position acquiring");
        } else {
            simpleNotificationManager.playSound();
        }
    }


}




