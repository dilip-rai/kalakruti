package com.artwork.kalakruti.javadraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.artwork.kalakruti.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JavaDrawActivity extends AppCompatActivity {

    @Bind(R.id.draw_view)
    JavaDrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_java_draw);
        ButterKnife.bind(this);
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
}
