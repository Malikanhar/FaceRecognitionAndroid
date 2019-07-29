package id.inditech.facerecognitionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FileUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    private static final String TAG = "LOADING";
    private TinyDB tinyDB;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Toast mToast;
    private List<User> userList;
    private int total = 0, success = 0, failed = 0;
    private ArrayList<Mat> faces;
    private ArrayList<String> labels;

    private void showToast(String message) {
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug(true)) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    List<File> filesDownloaded;
    int totalFiles = 0;
    int totalFailed = 0;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case SUCCESS:
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            filesDownloaded = new ArrayList<>();
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                final String nama = data.child("nama").getValue(String.class);
                                String key = data.getKey();
                                for (DataSnapshot face : data.child("faces").getChildren()) {
                                    String faceKey = face.getValue(String.class);
                                    final File file = new File(getCacheDir(), faceKey);
                                    totalFiles++;
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


//                showToast(userList.get(0).getFaces().size());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    break;
                    default:
                        super.onManagerConnected(status);
                        break;
            }
        }
    };

    public void Finishing(){
        for(File f : filesDownloaded){
            f.delete();
        }
        showToast( "Number of images: " + faces.size()  + ". Number of labels: " + labels.size());

        tinyDB.putListString("imagesLabels", labels);
        tinyDB.putListMat("images", faces);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //clearCache();
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

    private void clearCache(){
        try {
            File file = new File(getFilesDir().getAbsolutePath());
            FileUtils.deleteDirectory(file);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void tes(){
        File file = new File(getFilesDir().getAbsolutePath());
        String str = "";
        for(File f : file.listFiles()){
            str += f.getName() + " - ";
        }
        showToast(str);
        Log.i("-=-=-=-==-", str);
    }

    private void DownloadFromFile(String url, String key){
        try {
            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            File cache = new File(getFilesDir(), key);
            cache.mkdir();
            File file = File.createTempFile("face-"+key+"-", "jpg", cache);
            Uri uri = Uri.fromFile(file);
            reference.getFile(uri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    success++;
                    showToast("Success: " + success + ", Failed: " + failed + ", Total: " + total);
                    if (success + failed >= total) {
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    failed++;
                    showToast("Success: " + success + ", Failed: " + failed + ", Total: " + total);
                    if (success + failed >= total) {
                        finish();
                    }
                }
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
