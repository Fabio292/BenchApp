package fabiogentile.benchapp.StressTask;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.IOException;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;

/**
 * Created by Fabio Gentile on 27/07/16.
 */
public class AudioBench extends AsyncTask<Void, Void, Void> {
    private final String TAG = "CpuBench";
    private MainActivityI listener;
    private Context context;
    private String[] audioFiles = {"Tone - ogg.ogg", "Tone - 32.mp3"};

    public AudioBench(MainActivityI listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        for (String fname : audioFiles) {
            playAudio(fname, 2000, 1000); // TODO: 27/07/16 leggere i numeri da impostazioni
        }

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
