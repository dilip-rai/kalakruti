package com.artwork.kalakruti.javadraw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.artwork.kalakruti.DrawSpiralCommand;
import com.artwork.kalakruti.R;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implements drawing with Android Canvas.
 */
public class JavaDrawActivity extends AppCompatActivity {
    private JavaDrawView drawView;
    private TextView fpsCounter;

    private ScheduledThreadPoolExecutor timer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_draw);

        this.drawView = (JavaDrawView) findViewById(R.id.draw_view);
        this.fpsCounter = (TextView) findViewById(R.id.fps_couter_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawView.startDrawThread();
        this.timer = new ScheduledThreadPoolExecutor(1);
        this.timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fpsCounter.post(new Runnable() {
                    @Override
                    public void run() {
                        fpsCounter.setText(getString(R.string.fps_counter_2,
                                drawView.getDrawingFPS(), drawView.getPathUpdateNanos()));
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onPause() {
        drawView.stopDrawThread();
        this.timer.shutdown();
        this.timer = null;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_draw_spiral) {
            DrawSpiralCommand command = new DrawSpiralCommand(this.drawView);
            command.execute();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
