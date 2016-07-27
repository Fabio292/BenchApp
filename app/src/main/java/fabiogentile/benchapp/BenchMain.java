package fabiogentile.benchapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
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
import fabiogentile.benchapp.StressTask.AudioBench;
import fabiogentile.benchapp.StressTask.CpuBench;
import fabiogentile.benchapp.StressTask.GpsBench;
import fabiogentile.benchapp.StressTask.WiFiBench;
import fabiogentile.benchapp.Util.LcdEventReceiver;
import fabiogentile.benchapp.Util.LcdManager;
import fabiogentile.benchapp.Util.SimpleNotification;
import fabiogentile.benchapp.Util.VolumeManager;


public class BenchMain extends AppCompatActivity implements View.OnClickListener, MainActivityI {
    private final String TAG = "BenchMain";
    Object syncToken = new Object();
    private GpsBench gpsBench;
    private int gpsRequestNumber;
    private BroadcastReceiver mReceiver = null;
    private LcdManager lcdManager = LcdManager.getInstance();
    private SimpleNotification simpleNotificationManager = SimpleNotification.getInstance();
    private VolumeManager volumeManager = VolumeManager.getInstance();

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

        volumeManager.setAudioManager((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        volumeManager.saveVolume();

        this.gpsBench = new GpsBench(this, lcdManager);
        //</editor-fold>

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //<editor-fold desc="ACTON SCREEN events receiver">
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LcdEventReceiver(syncToken);
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
        Button btn_audio = (Button) findViewById(R.id.btn_audio);
        if (btn_audio != null)
            btn_audio.setOnClickListener(this);
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
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
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
                new CpuBench(this, syncToken).execute();
                lcdManager.turnScreenOff();
                break;

            case R.id.btn_wifi:
                Log.i(TAG, "onClick: WIFI");
                // TODO: 27/07/16 check interface name
                new WiFiBench(this, syncToken).execute();
                lcdManager.turnScreenOff();
                break;

            case R.id.btn_3g:
                Log.i(TAG, "onClick: 3G");
                // TODO: 27/07/16 CHECK connectivity + interface name
                break;

            case R.id.btn_lcd:
                Log.i(TAG, "onClick: LCD");
                Intent i = new Intent(getApplicationContext(), LcdActivity.class);
                startActivity(i);
                break;

            case R.id.btn_gps:
                Log.i(TAG, "onClick: GPS");
                gpsRequestNumber = 1;

                if (!gpsBench.getLocation(this, syncToken))
                    Log.e(TAG, "onClick: error during gps position acquiring");
                break;

            case R.id.btn_audio:
                Log.i(TAG, "onClick: AUDIO");
                volumeManager.setVolume(volumeManager.getMax());
                new AudioBench(this, getApplicationContext(), syncToken).execute();
                lcdManager.turnScreenOff();

                volumeManager.restoreVolume();
                break;

            default:
                Log.e(TAG, "onClick: button not recognized");
                break;
            //</editor-fold>
        }
    }

    //<editor-fold desc="TASK COMPLETION callback">
    @Override
    public void GpsTaskCompleted(Location location) {
        if (location == null)
            Log.e(TAG, "GpsTaskCompleted: ERROR during GPS position acquiring");
        else {
            // TODO: 27/07/16 numero richieste nei parametri
            if (gpsRequestNumber == 2) {
                if (!gpsBench.getLocation(this, null))
                    Log.e(TAG, "GpsTaskCompleted: error during gps position acquiring");
            } else {
                simpleNotificationManager.playSound();
            }
        }
    }

    @Override
    public void CpuTaskCompleted() {
        Log.i(TAG, "CpuTaskCompleted: cpu completed");
        simpleNotificationManager.notify("CPU", "Cpu task completed");
    }

    @Override
    public void AudioTaskCompleted() {
        Log.i(TAG, "AudioTaskCompleted: audio completed");
        simpleNotificationManager.notify("Audio", "Audio task completed");
    }

    @Override
    public void WiFiTaskCompleted() {
        Log.i(TAG, "WiFiTaskCompleted: wifi completed");
        simpleNotificationManager.notify("WiFi", "WiFi task completed");
    }
    //</editor-fold>


}




