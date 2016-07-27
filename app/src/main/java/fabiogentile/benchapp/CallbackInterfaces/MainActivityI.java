package fabiogentile.benchapp.CallbackInterfaces;


import android.location.Location;

public interface MainActivityI {
    void CpuTaskCompleted();
    void GpsTaskCompleted(Location location);

    void AudioTaskCompleted();
    void WiFiTaskCompleted();
}
