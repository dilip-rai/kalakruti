package com.artwork.kalakruti;

import android.graphics.Matrix;
import android.view.MotionEvent;

/**
 * Converts touch events to drawing gesture commands.
 */
public class DrawGestureDetector {

    private DrawGestureHandler handler;
    private Matrix transform = new Matrix();
    private float[] pts = new float[2];

    /**
     * Intialize gesture dector with a view and gesture handler.
     *
     * @param handler Drawing gesture handler.
     */
    public DrawGestureDetector(final DrawGestureHandler handler) {
        this.handler = handler;
    }

    /**
     * Convert motion event to drawing gesture.
     *
     * @param event MotionEvent from view or activity.
     * @return True if handled.
     */
    public boolean onTouchEvent(final MotionEvent event) {
        pts[0] = event.getX();
        pts[1] = event.getY();
        this.transform.mapPoints(pts);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return DrawGestureDetector.this.handler.beginStroke(pts[0], pts[1]);
            case MotionEvent.ACTION_MOVE:
                return DrawGestureDetector.this.handler.updateStroke(pts[0], pts[1]);
            case MotionEvent.ACTION_UP:
                return DrawGestureDetector.this.handler.endStroke(pts[0], pts[1]);
            default:
                return false;
        }
    }

    /**
     * Set transform for MotionEvent points.
     *
     * @param mat
     */
    public void setTransform(final Matrix mat) {
        this.transform.set(mat);
    }
}
