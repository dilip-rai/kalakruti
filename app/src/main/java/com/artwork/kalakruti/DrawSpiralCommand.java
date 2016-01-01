package com.artwork.kalakruti;

import android.util.Log;

/**
 * A utility class to draw spiral.
 */
public class DrawSpiralCommand {
    private static final String TAG = "DrawSpiralCommand";
    private static final double MAX_ANGLE = 64 * Math.PI;
    private static final double ALPHA = 4.0;
    private static final double STEP = 2.0f;
    private CommandTarget target;

    /**
     * Create new object.
     *
     * @param target Target view or activity.
     */
    public DrawSpiralCommand(final CommandTarget target) {
        this.target = target;
    }

    /**
     * Excecute the command.
     */
    public void execute() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                double theta = stepAngle(0);
                target.beginStroke(getX(theta), getY(theta));
                theta += stepAngle(theta);
                for (; theta < MAX_ANGLE; theta += stepAngle(theta)) {
                    target.updateStroke(getX(theta), getY(theta));
                    target.waitForUpdate();
                }
                target.endStroke(getX(MAX_ANGLE), getY(MAX_ANGLE));
                Log.d(TAG, "Exiting DrawSpiralCommand thread.");
            }
        };
        new Thread(runnable).start();
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

    /**
     * Interface to be implemented by DrawSpiralCommand target.
     */
    public interface CommandTarget {
        /**
         * Start a new stroke.
         *
         * @param x X co-ordinate.
         * @param y Y co-ordinate.
         */
        void beginStroke(float x, float y);

        /**
         * Add a new new point to stroke.
         *
         * @param x X co-ordinate.
         * @param y Y co-ordinate.
         */
        void updateStroke(float x, float y);

        /**
         * Complete the stroke.
         *
         * @param x X co-ordinate.
         * @param y Y co-ordinate.
         */
        void endStroke(float x, float y);

        /**
         * Wait for drawing to complete.
         */
        void waitForUpdate();
    }
}
