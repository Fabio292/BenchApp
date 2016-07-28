package fabiogentile.benchapp.StressTask;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;


public class GpsBench extends AsyncTask<Void, Void, Void> implements LocationResolver.LocationResult {
    private final String TAG = "GpsBench";
    private Context context;
    private MainActivityI callbackInterface;
    private Location location = null;
    private Object syncToken;
    private int requestTimeout;

    public GpsBench(MainActivityI callbackI, Object token, Context context, SharedPreferences prefs) {
        this.context = context;
        this.callbackInterface = callbackI;
        this.syncToken = token;
        this.requestTimeout = Integer.parseInt(prefs.getString("gps_request_timeout", "30"));
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Wait for screen to turn off
            if (syncToken != null)
                synchronized (syncToken) {
                    try {
                        syncToken.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            Looper.prepare();
            LocationResolver locationResolver = new LocationResolver();

            Log.i(TAG, "doInBackground: script started");
            // TODO: 28/07/16  marker
            if (!locationResolver.getLocation(context, this, 1000 * this.requestTimeout))
                Log.e(TAG, "doInBackground: error while requesting GPS position");
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
//        Log.i(TAG, "onPostExecute: script ended");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        callbackInterface.GpsTaskCompleted(location);
    }

    @Override
    public void gotLocation(Location location) {
        this.location = location;
        Log.i(TAG, "gotLocation");

        Looper.myLooper().quit();
    }

    @Override
    public void timeoutOccurred(Location location) {
        this.location = location;
        Log.i(TAG, "timeoutOccurred");

        Looper.myLooper().quit();
    }
}


class LocationResolver {
    private static LocationManager locationManager;
    private Timer timeoutTimer;
    private LocationResult locationCallbackInterface;
    private final LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {


            timeoutTimer.cancel();
            timeoutTimer.purge();
            try {
                locationManager.removeUpdates(this);
                locationCallbackInterface.gotLocation(location);
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
    private boolean gpsEnabled = false;

    public synchronized boolean getLocation(Context context, LocationResult result, int maxMillisToWait) {
        locationCallbackInterface = result;
        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // don't start listeners if no provider is enabled
        if (!gpsEnabled)
            return false;

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //Schedule the timeout
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new GetLastTimeout(), maxMillisToWait);
        return true;
    }

    public interface LocationResult {
        void gotLocation(Location location);

        void timeoutOccurred(Location location);
    }

    //Timeout timer
    private class GetLastTimeout extends TimerTask {

        @Override
        public void run() {
            try {

                locationManager.removeUpdates(locationListenerGps);

                Location gpsLocation = null;
                if (gpsEnabled)
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                locationCallbackInterface.timeoutOccurred(gpsLocation);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}