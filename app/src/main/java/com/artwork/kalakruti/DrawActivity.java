package com.artwork.kalakruti;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.artwork.kalakruti.command.DrawSpiralCommand;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Base activity class for all drawing activities.
 */
public abstract class DrawActivity extends AppCompatActivity {
    private static final int REFRESH_INTERVAL = 1000;

    private ScheduledThreadPoolExecutor timer;
    private DrawSpiralCommand command;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.timer = new ScheduledThreadPoolExecutor(1);
        this.timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                onUpdateFPS();
            }
        }, 0, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onPause() {
        if (this.timer != null) {
            this.timer.shutdown();
            this.timer = null;
        }

        if (this.command != null) {
            this.command.cancel(false);
            this.command = null;
        }
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
            this.command = new DrawSpiralCommand(getDrawGestureHandler());
            this.command.execute();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Called on a timer, to compute and update FPS stats.
     */
    protected abstract void onUpdateFPS();

    /**
     * Returns Draw Gesture Handler of activity.
     *
     * @return DrawGestureHandler instance of activity.
     */
    protected abstract DrawGestureHandler getDrawGestureHandler();
}
