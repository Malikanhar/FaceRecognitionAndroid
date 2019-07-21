package id.inditech.facerecognitionapp;

import android.media.FaceDetector;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        startActivityForResult(new Intent(FaceRecognitionAppActivity.this, LoginActivity.class), 101);

        //loadFace();
        //startActivity(new Intent(this, LoadingActivity.class));

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(FaceRecognitionAppActivity.this, DaftarUserActivity.class);
                startActivityForResult(intent, 101);
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

    @Override
    protected void onResume() {
        super.onResume();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 111){
            finish();
        }
        switch (requestCode){
            case 101:
                startActivity(new Intent(this, LoadingActivity.class));
                break;
        }
    }

    private void loadFace(){
        TinyDB tinyDB = new TinyDB(this);
        ArrayList<Mat> matList = tinyDB.getListMat("images");
        ArrayList<String> stringList = tinyDB.getListString("imagesLabels");
        File file = getFilesDir();
        for(File user : file.listFiles()){
            for(File ff : user.listFiles()){
                Toast.makeText(this, ff.getAbsolutePath(), Toast.LENGTH_LONG);
                Log.e("90909090", ff.getAbsolutePath());
                Mat mat = Imgcodecs.imread(ff.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                matList.add(mat);
                stringList.add(user.getName());
            }
        }
        tinyDB.putListMat( "images", matList);
        tinyDB.putListString("imagesLabels", stringList);

    }
}
