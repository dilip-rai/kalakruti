package com.artwork.kalakruti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.artwork.kalakruti.javadraw.JavaDrawActivity;
import com.artwork.kalakruti.skiadraw.SkiaDrawActivity;

/**
 * Root activity. Provides navigation to other child activities.
 */
public class KalakrutiActivity extends AppCompatActivity {

    private static final int DRAW_ACTIVITY = 1;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalakruti);

        findViewById(R.id.drawing_java)
                .setOnClickListener(new OnClickHandler(JavaDrawActivity.class));
        findViewById(R.id.drawing_skia)
                .setOnClickListener(new OnClickHandler(SkiaDrawActivity.class));
    }

    /**
     * A utility class to handle ckick on buttons.
     */
    private class OnClickHandler implements View.OnClickListener {

        private Class activityClass;

        OnClickHandler(final Class activityClass) {
            this.activityClass = activityClass;
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(final View v) {
            startActivityForResult(new Intent(KalakrutiActivity.this, this.activityClass), DRAW_ACTIVITY);
        }
    }
}
