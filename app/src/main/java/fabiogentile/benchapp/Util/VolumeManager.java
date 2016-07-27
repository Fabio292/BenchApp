package fabiogentile.benchapp.Util;

import android.media.AudioManager;


public class VolumeManager {
    private static VolumeManager ourInstance = new VolumeManager();
    private static AudioManager am;
    public int currentVolume;

    private VolumeManager() {
    }

    public static VolumeManager getInstance() {
        return ourInstance;
    }

    public void setAudioManager(AudioManager amn) {
        am = amn;
    }

    public void saveVolume() {
        currentVolume = getVolume();
    }

    public void restoreVolume() {
        setVolume(currentVolume);
    }

    public int getVolume() {
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setVolume(int vol) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
    }

    public int getMax() {
        return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
}
/*
AudioManager am =
    (AudioManager) getSystemService(Context.AUDIO_SERVICE);

am.setStreamVolume(
    AudioManager.STREAM_MUSIC,
    am.getStreamMaxVolume(VolumeManager.STREAM_MUSIC),
    0);
 */