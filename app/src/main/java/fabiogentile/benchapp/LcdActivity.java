package fabiogentile.benchapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.IOException;

interface OnTaskCompleted {
    void onTaskCompleted();
}

public class LcdActivity extends Activity implements OnTaskCompleted {
    private final String TAG = "LcdActivity";
    private int STATE = 0;
    private FrameLayout layout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd);

        layout = (FrameLayout) findViewById(R.id.lcd_activity);

        Log.i(TAG, "onCreate: launch lcd bench");
        new LcdBench(this).execute();
    }

    @Override
    public void onTaskCompleted() {
        switch (STATE) {
            case 0:
                STATE++;
                layout.setBackgroundColor(Color.GREEN);
                new LcdBench(this).execute();
                break;

            case 1:
                STATE++;
                layout.setBackgroundColor(Color.BLUE);
                new LcdBench(this).execute();
                break;

            default:
                break;
        }
    }


    class LcdBench extends AsyncTask<Void, Void, Void> {
        private final String TAG = "LcdBench";
        private OnTaskCompleted listener;

        public LcdBench(OnTaskCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "doInBackground: launch script");
                Process process = Runtime.getRuntime().exec("su -c sh /sdcard/BENCHMARK/lcd_test.sh");
                process.waitFor();
                Log.i(TAG, "doInBackground: script terminated");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            listener.onTaskCompleted();
        }


    }
}