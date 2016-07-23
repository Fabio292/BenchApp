package fabiogentile.benchapp.Util;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import java.util.Random;

import fabiogentile.benchapp.R;

public class SimpleNotification {
    private static SimpleNotification ourInstance = new SimpleNotification();
    private static Context context = null;
    private static NotificationManager notificationService = null;
    private static Random r = new Random();

    private SimpleNotification() {
    }

    public static SimpleNotification getInstance() {
        return ourInstance;
    }

    //<editor-fold desc="SET params">
    public void setContext(Context context) {
        SimpleNotification.context = context;
    }

    public void setNotificationService(NotificationManager manager) {
        SimpleNotification.notificationService = manager;
    }
    //</editor-fold>

    public boolean isReady() {
        return (context != null) && (notificationService != null);
    }

    public boolean notify(String title, String message) {

        if (!isReady())
            return false;

        //Get notification Sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Create notification
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon_basic)
                        .setSound(alarmSound)
                        .setContentTitle(title)
                        .setContentText(message);

        //Empty intent (if user click on notification nothing will happen)
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        //Generate random notification id
        int notificationId = r.nextInt(50000) + 100;

        //Send notification
        notificationService.notify(notificationId, mBuilder.build());

        return true;
    }

    public boolean playSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
