package com.artwork.kalakruti.skiadraw;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * View class for wrapping Skia drawing.
 */
public class SkiaGLSurfaceView extends GLSurfaceView {
    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     *
     * @param context Android context.
     */
    public SkiaGLSurfaceView(final Context context) {
        super(context);
        init();
    }

    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     *
     * @param context Android context.
     * @param attrs   View attributes.
     */
    public SkiaGLSurfaceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setRenderer(new SkiaGLRenderer());
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
