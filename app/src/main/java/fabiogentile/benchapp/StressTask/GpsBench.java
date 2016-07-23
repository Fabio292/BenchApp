package fabiogentile.benchapp.StressTask;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;

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
    private boolean gps_enabled = false;

    public GpsBench(MainActivityI listener) {
        callbackInterface = listener;
    }

    public boolean getLocation(Context context) {

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
        timer1.schedule(new GetLastLocation(), 60000);

        return true;
    }

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try {
                lm.removeUpdates(locationListenerGps);

                Location gps_loc = null;
                if (gps_enabled)
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                if (gps_loc != null) {
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

