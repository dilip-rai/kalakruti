package com.artwork.kalakruti.skiadraw;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

import com.artwork.kalakruti.DrawActivity;
import com.artwork.kalakruti.DrawGestureHandler;
import com.artwork.kalakruti.R;

/**
 * Skia drawing activity.
 */
public class SkiaDrawActivity extends DrawActivity {
    static {
        System.loadLibrary("skia_android");
        System.loadLibrary("kalakruti");
    }

    private SkiaGLRenderer renderer;
    private TextView fpsCounter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skia_draw);
        this.fpsCounter = (TextView) findViewById(R.id.fps_couter_view);
        this.renderer = new SkiaGLRenderer((GLSurfaceView) findViewById(R.id.draw_view));
    }

    @Override
    protected void onUpdateFPS() {
        fpsCounter.post(new Runnable() {
            @Override
            public void run() {
                fpsCounter.setText(getString(R.string.fps_counter_2,
                        renderer.getDrawingFPS(), renderer.getPathUpdateNanos()));

            }
        });
    }

    @Override
    protected DrawGestureHandler getDrawGestureHandler() {
        return this.renderer;
    }
}
