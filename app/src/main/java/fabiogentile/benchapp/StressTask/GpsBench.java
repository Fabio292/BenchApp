package fabiogentile.benchapp.StressTask;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.Util.LcdManager;

// FIXME: 27/07/16 !!!!!!!
// http://stackoverflow.com/a/3145655
public class GpsBench {
    private final String TAG = "MyLocation";
    private Timer timer1;
    private LocationManager lm;
    private MainActivityI callbackInterface;
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            callbackInterface.GpsTaskCompleted(location);
            try {
                lm.removeUpdates(this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LcdManager lcdManager;
    private boolean gps_enabled = false;

    public GpsBench(MainActivityI listener, LcdManager lcdManager) {
        this.callbackInterface = listener;
        this.lcdManager = lcdManager;
    }

    public boolean getLocation(Context context, Object syncToken) {

        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled)
            return false;

        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }

        timer1 = new Timer();
        TimerTask task = new GetLastLocation(syncToken, lcdManager);
        timer1.schedule(task, 60000); // TODO: 27/07/16 leggere da settings
        lcdManager.turnScreenOff();

        return true;
    }


    class GetLastLocation extends TimerTask {
        Object syncToken;
        LcdManager lcdManager;

        public GetLastLocation(Object token, LcdManager lcdManager) {
            this.syncToken = token;
            this.lcdManager = lcdManager;
        }

        @Override
        public void run() {
            try {
                //Wait for screen to turn off
                Log.i(TAG, "run: dentro il thread");
                if (syncToken != null) {

                    synchronized (syncToken) {
                        try {
                            syncToken.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                lm.removeUpdates(locationListenerGps);

                Location gps_loc = null;
                Log.i(TAG, "run: Start GPS position acquiring");
                if (gps_enabled)
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (gps_loc != null) {
                    Log.i(TAG, "run: GPS position scquired: " + gps_loc.getLatitude() + " " + gps_loc.getLongitude()
                            + " " + gps_loc.getAltitude() + " accuracy: " + gps_loc.getAccuracy());
                    callbackInterface.GpsTaskCompleted(gps_loc);
                    return;
                }

                callbackInterface.GpsTaskCompleted(null);

            } catch (SecurityException e) {
                e.printStackTrace();
                callbackInterface.GpsTaskCompleted(null);
            }
        }
    }
}

