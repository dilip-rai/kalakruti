package com.artwork.kalakruti;

/**
 * Interface for supporting drawing gesture.
 */
public interface DrawGestureHandler {
    /**
     * Start a new stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @return True if handled.
     */
    boolean beginStroke(float x, float y);

    /**
     * Add a new new point to stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @return True if handled.
     */
    boolean updateStroke(float x, float y);

    /**
     * Complete the stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @return True if handled.
     */
    boolean endStroke(float x, float y);

    /**
     * Wait for drawing to complete.
     */
    void waitForUpdate();
}
