package com.artwork.kalakruti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.artwork.kalakruti.javadraw.JavaDrawActivity;

public class KalakrutiActivity extends AppCompatActivity {

    private static final int JAVA_DRAW_ACTIVITY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalakruti);

        TextView textView = (TextView) findViewById(R.id.drawing_android_java);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(KalakrutiActivity.this, JavaDrawActivity.class), JAVA_DRAW_ACTIVITY);
            }
        });
    }
}
