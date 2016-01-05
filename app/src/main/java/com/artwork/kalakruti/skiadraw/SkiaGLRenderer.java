package com.artwork.kalakruti.skiadraw;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.artwork.kalakruti.DrawGestureDetector;
import com.artwork.kalakruti.DrawGestureHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLRenderer for Skia drawing.
 */
class SkiaGLRenderer implements GLSurfaceView.Renderer, DrawGestureHandler {
    private static final String TAG = "SkiaGLRenderer";
    private static final int STROKE_WIDTH = 5;

    private GLSurfaceView view;
    private DrawGestureDetector gestureDetector;
    private SkiaPath path = new SkiaPath();
    private Matrix viewToModel = new Matrix();

    // Multi-threaded drawing support
    private Lock lock = new ReentrantLock();
    private Condition updateDone = lock.newCondition();
    private RectF dirtyRect = new RectF();
    private float prevX;
    private float prevY;
    private volatile long totalDrawingTime;
    private volatile int drawCount;
    private volatile long totalPathUpdateTime;
    private volatile int pathUpdateCount;

    /**
     * Initialize renderer with GLSurfaceView.
     *
     * @param view Target view.
     */
    SkiaGLRenderer(final GLSurfaceView view) {
        this.gestureDetector = new DrawGestureDetector(this);
        this.view = view;
        view.setEGLConfigChooser(8, 8, 8, 8, 0, 8);
        view.setEGLContextClientVersion(2);
        view.setRenderer(this);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        this.view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                return SkiaGLRenderer.this.gestureDetector.onTouchEvent(event);
            }
        });
    }

    private static native void init();

    private static native void destroy();

    private static native void setSize(final int width, final int height);

    private static native void draw(final SkiaPath path, final float left, final float top, final float right, final float bottom);

    /**
     * Called when the surface is created or recreated.
     * <p>
     * Called when the rendering thread
     * starts and whenever the EGL context is lost. The EGL context will typically
     * be lost when the Android device awakes after going to sleep.
     * <p>
     * Since this method is called at the beginning of rendering, as well as
     * every time the EGL context is lost, this method is a convenient place to put
     * code to create resources that need to be created when the rendering
     * starts, and that need to be recreated when the EGL context is lost.
     * Textures are an example of a resource that you might want to create
     * here.
     * <p>
     * Note that when the EGL context is lost, all OpenGL resources associated
     * with that context will be automatically deleted. You do not need to call
     * the corresponding "glDelete" methods such as glDeleteTextures to
     * manually delete these lost resources.
     * <p>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     */
    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        init();
    }

    /**
     * Called when the surface changed size.
     * <p>
     * Called after the surface is created and whenever
     * the OpenGL ES surface size changes.
     * <p>
     * Typically you will set your viewport here. If your camera
     * is fixed then you could also set your projection matrix here:
     * <pre class="prettyprint">
     * void onSurfaceChanged(GL10 gl, int width, int height) {
     * gl.glViewport(0, 0, width, height);
     * // for a fixed camera, set the projection too
     * float ratio = (float) width / height;
     * gl.glMatrixMode(GL10.GL_PROJECTION);
     * gl.glLoadIdentity();
     * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
     * }
     * </pre>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param width  Surface width.
     * @param height Surface height.
     */
    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        Matrix modelToView = new Matrix();
        modelToView.setValues(new float[]{
                1, 0, width / 2,
                0, -1, height / 2,
                0, 0, 1
        });
        modelToView.invert(this.viewToModel);
        this.gestureDetector.setTransform(this.viewToModel);
        setSize(width, height);
    }

    /**
     * Called to draw the current frame.
     * <p>
     * This method is responsible for drawing the current frame.
     * <p>
     * The implementation of this method typically looks like this:
     * <pre class="prettyprint">
     * void onDrawFrame(GL10 gl) {
     * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
     * //... other gl calls to render the scene ...
     * }
     * </pre>
     *
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame(final GL10 gl) {
        float left, top, right, bottom;
        try {
            lock.lock();
            left = this.dirtyRect.left;
            top = this.dirtyRect.top;
            right = this.dirtyRect.right;
            bottom = this.dirtyRect.bottom;
            this.dirtyRect.setEmpty();
        } finally {
            lock.unlock();
        }

        if (this.path != null) {
            long startTime = System.nanoTime();
            draw(this.path, left, top, right, bottom);
            this.totalDrawingTime += System.nanoTime() - startTime;
            ++this.drawCount;
        }

        try {
            lock.lock();
            this.updateDone.signal();
        } finally {
            lock.unlock();
        }
    }

    private void requestRender() {
        this.view.requestRender();
    }

    /**
     * Start a new stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    public boolean beginStroke(final float x, final float y) {
        this.path = new SkiaPath();
        this.prevX = x;
        this.prevY = y;

        long startTime = System.nanoTime();
        this.path.moveTo(x, y);
        this.totalPathUpdateTime = System.nanoTime() - startTime;
        this.pathUpdateCount = 1;

        this.drawCount = 0;
        this.totalDrawingTime = 0;

        return true;
    }

    /**
     * Add a new new point to stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    public boolean updateStroke(final float x, final float y) {
        if (this.path != null) {
            long startTime = System.nanoTime();
            this.path.lineTo(x, y);
            this.totalPathUpdateTime = System.nanoTime() - startTime;
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
                requestRender();
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
     * Complete the stroke.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    @Override
    public boolean endStroke(final float x, final float y) {
        return updateStroke(x, y);
    }

    /**
     * Wait for drawing to complete.
     */
    @Override
    public void waitForUpdate() {
        try {
            lock.lock();
            while (!this.dirtyRect.isEmpty()) {
                updateDone.await();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Inturrupted waiting main thread.", e);
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
    double getDrawingFPS() {
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
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
