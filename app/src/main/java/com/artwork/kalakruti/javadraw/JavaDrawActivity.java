package com.artwork.kalakruti.javadraw;

import android.os.Bundle;
import android.widget.TextView;

import com.artwork.kalakruti.DrawActivity;
import com.artwork.kalakruti.DrawGestureHandler;
import com.artwork.kalakruti.R;

/**
 * Implements drawing with Android Canvas.
 */
public class JavaDrawActivity extends DrawActivity {
    private JavaDrawView drawView;
    private TextView fpsCounter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_draw);

        this.drawView = (JavaDrawView) findViewById(R.id.draw_view);
        this.fpsCounter = (TextView) findViewById(R.id.fps_couter_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawView.startDrawThread();
    }

    @Override
    protected void onPause() {
        drawView.stopDrawThread();
        super.onPause();
    }

    @Override
    protected void onUpdateFPS() {

        fpsCounter.post(new Runnable() {
            @Override
            public void run() {
                fpsCounter.setText(getString(R.string.fps_counter_2,
                        drawView.getDrawingFPS(), drawView.getPathUpdateNanos()));

            }
        });
    }

    @Override
    protected DrawGestureHandler getDrawGestureHandler() {
        return this.drawView;
    }
}
