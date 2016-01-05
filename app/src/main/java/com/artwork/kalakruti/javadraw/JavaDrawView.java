package com.artwork.kalakruti.javadraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.artwork.kalakruti.DrawGestureDetector;
import com.artwork.kalakruti.DrawGestureHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Android SurfaceView implementation for drawing with Canvas.
 */
public class JavaDrawView extends SurfaceView implements DrawGestureHandler {
    private static final String TAG = "JavaDrawView";
    private static final int STROKE_WIDTH = 5;
    private DrawGestureDetector gestureDetector;

    private Thread drawThread;
    private Lock lock = new ReentrantLock();
    private Condition needsUpdate = lock.newCondition();
    private Condition updateDone = lock.newCondition();
    private volatile boolean continueDrawing;
    private RectF dirtyRect = new RectF();
    private Picture picture = new Picture();
    private volatile Path currentPath;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float prevX, prevY;
    private Matrix modelToView = new Matrix();
    private Matrix viewToModel = new Matrix();
    private volatile long totalDrawingTime;
    private volatile int drawCount;
    private volatile long totalPathUpdateTime;
    private volatile int pathUpdateCount;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public JavaDrawView(final Context context) {
        super(context);
        init();
    }

    /**
     * Constructor.
     *
     * @param context Android context.
     * @param attrs   View attributes.
     */
    public JavaDrawView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor.
     *
     * @param context      Android context.
     * @param attrs        View attributes.
     * @param defStyleAttr Default attributes.
     */
    public JavaDrawView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Starts drawing thread. Typically called from Activity.onResume method.
     */
    public void startDrawThread() {
        if (this.drawThread == null) {
            this.drawThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runDrawThread();
                }
            });
            this.continueDrawing = true;
            this.drawThread.start();
        }
    }

    /**
     * Stops drawing thread. Typically called from Activity.onPause method.
     */
    public void stopDrawThread() {
        if (this.drawThread != null) {
            this.continueDrawing = false;
            this.drawThread.interrupt();
            try {
                this.drawThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to stop drawing thread gracefully.");
            }
            this.drawThread = null;
        }
    }

    /**
     * Touch event handler. Supports drawing with finger/stylus.
     *
     * @param event Touch event.
     * @return True if handled.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    /**
     * Begin drawing.
     *  @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    @SuppressWarnings("MagicNumber")
    public boolean beginStroke(final float x, final float y) {
        if (this.currentPath == null) {
            this.paint.setColor(Color.rgb(
                    (int) Math.round(255 * Math.random()),
                    (int) Math.round(255 * Math.random()),
                    (int) Math.round(255 * Math.random())));
            this.currentPath = new Path();
            this.prevX = x;
            this.prevY = y;
            long startTime = System.nanoTime();
            this.currentPath.moveTo(x, y);
            this.totalPathUpdateTime = System.nanoTime() - startTime;
            this.pathUpdateCount = 1;

            this.drawCount = 0;
            this.totalDrawingTime = 0;
            return true;
        }
        return false;
    }

    /**
     * Update current stroke.
     *  @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    public boolean updateStroke(final float x, final float y) {
        if (this.currentPath != null) {
            long startTime = System.nanoTime();
            this.currentPath.lineTo(x, y);
            this.totalPathUpdateTime += System.nanoTime() - startTime;
            ++this.pathUpdateCount;

            try {
                lock.lock();
                if (this.dirtyRect.isEmpty()) {
                    this.dirtyRect.set(this.prevX, this.prevY, x, y);
                    this.dirtyRect.sort();
                } else {
                    this.dirtyRect.union(x, y);
                }
                this.dirtyRect.inset(-STROKE_WIDTH, -STROKE_WIDTH);
                needsUpdate.signal();
            } finally {
                lock.unlock();
            }
            this.prevX = x;
            this.prevY = y;
            return true;
        }
        return false;
    }

    /**
     * Finish drawing current stroke.
     *  @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    public boolean endStroke(final float x, final float y) {
        if (this.currentPath != null) {
            updateStroke(x, y);
            Canvas canvas = picture.beginRecording(this.getWidth(), this.getHeight());
            int restorePoint = canvas.save();
            canvas.concat(this.modelToView);
            canvas.drawPath(this.currentPath, this.paint);
            canvas.restoreToCount(restorePoint);
            picture.endRecording();
            this.currentPath = null;
            return true;
        }
        return false;
    }

    /**
     * Wait for drawing to complete.
     */
    @Override
    public void waitForUpdate() {
        try {
            lock.lock();
            while (!this.dirtyRect.isEmpty() && this.continueDrawing) {
                updateDone.await();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "waitForUpdate interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Compute drawing FPS.
     *
     * @return drawing FPS.
     */
    @SuppressWarnings("MagicNumber")
    public double getDrawingFPS() {
        if (this.drawCount > 0) {
            // Convert nanos to seconds.
            return 1000.0 * 1000.0 * 1000.0 * this.drawCount / this.totalDrawingTime;
        }
        return 0;
    }

    /**
     * Compute average (in nono-seconds) time taken to update path.
     *
     * @return Time in nano seconds.
     */
    public double getPathUpdateNanos() {
        if (this.pathUpdateCount > 0) {
            return this.totalPathUpdateTime / this.pathUpdateCount;
        }
        return 0;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.modelToView.setValues(new float[]{
                1, 0, w / 2,
                0, -1, h / 2,
                0, 0, 1
        });
        this.modelToView.invert(this.viewToModel);
        this.gestureDetector.setTransform(this.viewToModel);
    }

    private void init() {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(STROKE_WIDTH);
        this.paint.setColor(Color.RED);
        this.gestureDetector = new DrawGestureDetector(this);
    }

    private void runDrawThread() {
        RectF drawRectF = new RectF();
        Rect drawRect = new Rect();
        SurfaceHolder holder = getHolder();
        while (this.continueDrawing) {
            try {
                lock.lock();
                while (this.dirtyRect.isEmpty()) {
                    this.needsUpdate.await();
                }
                this.modelToView.mapRect(drawRectF, this.dirtyRect);
                this.dirtyRect.setEmpty();
            } catch (InterruptedException e) {
                Log.d(TAG, "Draw thread inturrupted.", e);
                break;
            } finally {
                lock.unlock();
            }

            long startTime = System.nanoTime();
            drawRect.set(Math.round(drawRectF.left), Math.round(drawRectF.top),
                    Math.round(drawRectF.right), Math.round(drawRectF.bottom));
            Canvas canvas = holder.lockCanvas(drawRect);
            if (canvas != null) {
                int savePoint = canvas.save();
                canvas.drawColor(Color.WHITE);
                this.picture.draw(canvas);
                canvas.concat(this.modelToView);
                if (this.currentPath != null) {
                    canvas.drawPath(this.currentPath, this.paint);
                }
                canvas.restoreToCount(savePoint);
                holder.unlockCanvasAndPost(canvas);
            }
            this.totalDrawingTime += System.nanoTime() - startTime;
            ++this.drawCount;

            signalUpdateDone();
        }
        // Just in case, empty rect was not cleared.
        signalUpdateDone();
    }

    private void signalUpdateDone() {
        try {
            lock.lock();
            updateDone.signal();
        } finally {
            lock.unlock();
        }
    }
}
