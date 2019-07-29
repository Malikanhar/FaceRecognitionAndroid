package id.inditech.facerecognitionapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.FaceDetector;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionAppActivity extends AppCompatActivity {

    private LinearLayout btnRecord, btnAbsent, btnHistory, btnAbout;
    private static final String TAG = "LOADING";
    private TinyDB tinyDB;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Toast mToast;
    private List<User> userList;
    private int total = 0, success = 0, failed = 0;
    private ArrayList<Mat> faces;
    private ArrayList<String> labels;
    private ProgressDialog progressDialog;

    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug(true)) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void showToast(String message) {
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    List<File> filesDownloaded;
    int totalFiles = 0;
    int totalFailed = 0;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case SUCCESS:
                    progressDialog.setTitle("Sedang memuat data dari database");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    mDatabase.addValueEventListener(postListener);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            progressDialog.dismiss();
            filesDownloaded = new ArrayList<>();
            labels.clear();
            faces.clear();
            for(DataSnapshot data : dataSnapshot.getChildren()){
                final String nama = data.child("nama").getValue(String.class);
                String key = data.getKey();
                for (DataSnapshot face : data.child("faces").getChildren()) {
                    String faceKey = face.getValue(String.class);
                    final File file = new File(getCacheDir(), faceKey);
                    totalFiles++;
                    progressDialog.setTitle("Sedang memuat data dari database");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    mStorage.child(key).child(faceKey).getFile(file)
                            .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    filesDownloaded.add(file);
                                    labels.add(nama);

                                    Mat mat = Imgcodecs.imread(file.getAbsolutePath());
                                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
                                    Mat face = mat.reshape(0, (int)mat.total());
                                    faces.add(face);
                                    tinyDB.putListString("imagesLabels", labels);
                                    tinyDB.putListMat("images", faces);
                                    if(totalFiles - totalFailed == filesDownloaded.size()){
                                        Finishing();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    totalFailed++;
                                    e.printStackTrace();
                                }
                            });
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void Finishing(){
        for(File f : filesDownloaded){
            f.delete();
        }
        showToast( "Number of images: " + faces.size()  + ". Number of labels: " + labels.size());


        progressDialog.dismiss();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(FaceRecognitionAppActivity.this, LoginActivity.class), 102);
        //startActivity(new Intent(this, LoadingActivity.class));
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        tinyDB = new TinyDB(this);
        faces = new ArrayList<>();
        labels = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mStorage = FirebaseStorage.getInstance().getReference("users");

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                loadOpenCV();
            }
        } else {
            loadOpenCV();
        }

        btnRecord = findViewById(R.id.llRekamWajah);
        btnAbsent = findViewById(R.id.llAbsen);
        btnHistory = findViewById(R.id.llRiwayat);
        btnAbout = findViewById(R.id.llTentang);



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

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    loadOpenCV();
                } else {
                    //not granted
                    showToast("Please grant permission");
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
                //progressDialog.setTitle("Sedang memuat data dari database");
                //progressDialog.setCancelable(false);
                //progressDialog.show();
                //mDatabase.addListenerForSingleValueEvent(postListener);
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
