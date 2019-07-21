package id.inditech.facerecognitionapp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.io.FileUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case SUCCESS:
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                String nama = data.child("nama").getValue(String.class);
//                                String key = data.child("key").getValue(String.class);
                                for (DataSnapshot face : data.child("faces").getChildren()) {
                                    String faceData = face.child("data").getValue(String.class);
                                    byte[] buffer = Base64.decode(faceData, Base64.DEFAULT);
                                    Mat mat = new Mat(buffer.length, 1, CvType.CV_8U);
                                    mat.put(0, 0, buffer);
                                    Log.d("onDATACHANGE ", data.getKey());

                                    faces.add(mat);
                                    labels.add(nama);
                                }


                            }
                            showToast( "Number of images: " + faces.size()  + ". Number of labels: " + labels.size());

                            tinyDB.putListString("imagesLabels", labels);
                            tinyDB.putListMat("images", faces);
                            finish();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //clearCache();
        tinyDB = new TinyDB(this);
        faces = new ArrayList<>();
        labels = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        loadOpenCV();
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
