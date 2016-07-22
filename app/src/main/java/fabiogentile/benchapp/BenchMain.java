package fabiogentile.benchapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.StressTask.CpuBench;
import fabiogentile.benchapp.Util.LcdEventReceiver;
import fabiogentile.benchapp.Util.LcdManager;


public class BenchMain extends AppCompatActivity implements View.OnClickListener, MainActivityI {
    private final String TAG = "BenchMain";
    private BroadcastReceiver mReceiver = null;
    private LcdManager lcdManager = LcdManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bench_main);

        lcdManager.saveLcdTimeout(getContentResolver());

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LcdEventReceiver();
        registerReceiver(mReceiver, filter);

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
                lcdManager.turnScreenOff(getContentResolver());
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

        //Get notification Sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Create notification
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon_basic)
                        .setContentTitle("My notification")
                        .setSound(alarmSound)
                        .setContentText("Hello World!");

        //Empty intent (if user click on notification nothing will happen
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(), // add this
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        Random r = new Random();
        int notificationId = r.nextInt(50000) + 100;
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

}



