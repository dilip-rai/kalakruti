package com.artwork.kalakruti.javadraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dilip on 24/12/15.
 */
public class JavaDrawView extends SurfaceView {
    private static final String TAG = "JavaDrawView";
    private static final int STROKE_WIDTH = 5;

    private Thread drawThread;
    private Lock lock = new ReentrantLock();
    private Condition needsUpdate = lock.newCondition();
    private volatile boolean continueDrawing;
    private Rect dirtyRect = new Rect();
    private Path path =new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public JavaDrawView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public JavaDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public JavaDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public JavaDrawView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();
            final float radius = (float) (Math.random() * 50);
            path.addCircle(x, y, radius, Path.Direction.CCW);
            try {
                lock.lock();
                this.dirtyRect.union(Math.round(x - radius), Math.round(y - radius),
                        Math.round(x + radius), Math.round(y + radius));
                this.dirtyRect.inset(-STROKE_WIDTH, -STROKE_WIDTH);
                needsUpdate.signal();
            } finally {
                lock.unlock();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void init(Context context, final AttributeSet attrs, final int defStyleAttr,  final int defStyleRes) {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(STROKE_WIDTH);
        this.paint.setColor(Color.RED);
    }

    private void runDrawThread() {
        Rect drawRect = new Rect();
        SurfaceHolder holder = getHolder();
        while(this.continueDrawing) {
            try {
                lock.lock();
                while (this.dirtyRect.isEmpty()) {
                    this.needsUpdate.await();
                }
                drawRect.set(this.dirtyRect);
                this.dirtyRect.setEmpty();
            } catch (InterruptedException e) {
                Log.d(TAG, "Draw thread inturrupted.", e);
                break;
            } finally {
                lock.unlock();
            }
            Canvas canvas = holder.lockCanvas(drawRect);
            if (canvas != null) {
                canvas.drawPath(this.path, this.paint);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
