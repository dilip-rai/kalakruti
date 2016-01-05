package com.artwork.kalakruti.command;

import android.os.AsyncTask;
import android.util.Log;

import com.artwork.kalakruti.DrawGestureHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class to draw spiral.
 */
public class DrawSpiralCommand extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "DrawSpiralCommand";
    private static final double MAX_ANGLE = 48 * Math.PI;
    private static final double ALPHA = 4.0;
    private static final double STEP = 2.0f;
    private DrawGestureHandler target;
    private List<Long> timestamps = new ArrayList<>(256);

    /**
     * Create new object.
     *
     * @param target Target view or activity.
     */
    public DrawSpiralCommand(final DrawGestureHandler target) {
        this.target = target;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.timestamps.clear();
    }

    @Override
    protected Void doInBackground(final Void... params) {
        long startTime = System.currentTimeMillis();
        int counter = 1;
        double theta = stepAngle(0);
        target.beginStroke(getX(theta), getY(theta));
        theta += stepAngle(theta);
        timestamps.add(0L);
        for (; theta < MAX_ANGLE && !this.isCancelled(); theta += stepAngle(theta), ++counter) {
            target.updateStroke(getX(theta), getY(theta));
            target.waitForUpdate();
            if (counter % 100 == 0) {
                timestamps.add(System.currentTimeMillis() - startTime);
            }
        }
        if (!this.isCancelled()) {
            target.endStroke(getX(MAX_ANGLE), getY(MAX_ANGLE));
            target.waitForUpdate();
            if (counter % 100 == 0) {
                timestamps.add(System.currentTimeMillis() - startTime);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, "Exiting DrawSpiralCommand thread.");
        Long[] ts = new Long[timestamps.size()];
        Log.d(TAG, Arrays.toString(timestamps.toArray(ts)));
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(TAG, "DrawSpiralCommand canceled.");
    }

    private double stepAngle(final double angle) {
        double radius = ALPHA * angle;
        if (radius < STEP / 2) {
            radius = STEP / 2;
        }
        return 2 * Math.asin(STEP / 2 / radius);
    }

    private float getX(final double angle) {
        return (float) (ALPHA * angle * Math.cos(angle));
    }

    private float getY(final double angle) {
        return (float) (ALPHA * angle * Math.sin(angle));
    }
}
