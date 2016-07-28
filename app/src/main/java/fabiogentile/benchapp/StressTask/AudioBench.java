package fabiogentile.benchapp.StressTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;

public class AudioBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    private MainActivityI listener;
    private Context context;
    private Object syncToken;
    private int playDuration;
    private int silenceDuration;
    private String[] audioFiles = {"Tone - ogg.ogg", "Tone - vbr.mp3", "Tone - wav.wav",
            "Tone - 32.mp3", "Tone - 128.mp3", "Tone - 192.mp3", "Tone - 320.mp3"};

    public AudioBench(MainActivityI listener, Context context, Object token, SharedPreferences prefs) {
        this.listener = listener;
        this.context = context;
        this.syncToken = token;
        this.playDuration = prefs.getInt("audio_play_duration", 5);
        this.silenceDuration = prefs.getInt("audio_pause_duration", 2);
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Wait for screen to turn off
        synchronized (syncToken) {
            try {
                syncToken.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "doInBackground: launch script");
        // TODO: 28/07/16  marker
        for (String fname : audioFiles) {
            playAudio(fname, 1000 * this.playDuration, 1000 * this.silenceDuration); // TODO: 27/07/16 leggere i numeri da impostazioni
        }
        Log.i(TAG, "doInBackground: script ended");

        return null;
    }

    /**
     * Play an audio file for the specified ammount of time
     *
     * @param fname           file path unsed the ASSETS folder
     * @param duration        ms of play
     * @param silenceDuration ms of silence after the sound
     */
    private void playAudio(String fname, int duration, int silenceDuration) {
        try {
            Log.i(TAG, "playAudio: playing " + fname);
            AssetFileDescriptor afd = context.getAssets().openFd(fname);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();

            Thread.sleep(duration);
            player.stop();
            Thread.sleep(silenceDuration);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void onPostExecute(Void result) {
        listener.AudioTaskCompleted();
    }
}
