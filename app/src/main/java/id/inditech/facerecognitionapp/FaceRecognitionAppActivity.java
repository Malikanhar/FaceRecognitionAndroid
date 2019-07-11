package id.inditech.facerecognitionapp;

import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class FaceRecognitionAppActivity extends AppCompatActivity {

    private LinearLayout btnRecord, btnAbsent, btnHistory, btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecord = findViewById(R.id.llRekamWajah);
        btnAbsent = findViewById(R.id.llAbsen);
        btnHistory = findViewById(R.id.llRiwayat);
        btnAbout = findViewById(R.id.llTentang);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(FaceRecognitionAppActivity.this, RegisterFaceActivity.class);
                startActivity(intent);
            }
        });

        btnAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(FaceRecognitionAppActivity.this, RecognizeActivity.class);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(FaceRecognitionAppActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(FaceRecognitionAppActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}
