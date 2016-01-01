package com.artwork.kalakruti.skiadraw;

import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLRenderer for Skia drawing.
 */
class SkiaGLRenderer implements GLSurfaceView.Renderer {
    private static final int SIZE_OF_FLOAT = 4;
    private static final int VERTEX_SIZE = 3;
    private static final float[] VERTEX_ARRAY = new float[]{
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };

    private FloatBuffer vertexBuffer;

    SkiaGLRenderer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_ARRAY.length * SIZE_OF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        this.vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(VERTEX_ARRAY);
        vertexBuffer.position(0);
    }

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
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.0f);
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
        gl.glViewport(0, 0, width, height);
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
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(VERTEX_SIZE, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, VERTEX_SIZE);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
