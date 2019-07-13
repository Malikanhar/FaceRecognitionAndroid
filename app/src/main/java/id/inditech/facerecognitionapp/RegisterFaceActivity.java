package id.inditech.facerecognitionapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegisterFaceActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = RegisterFaceActivity.class.getSimpleName();
    private ArrayList<Mat> images;
    private ArrayList<String> imagesLabels;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mGray;
    private Toast mToast;
    private int maximumImages = 3;
    private String name, key;
    private Button btnTakePicture;
    private File mCascadeFile;
    private CascadeClassifier mClassifier;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_register_face);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            name = extras.getString("nama");
            key = extras.getString("key");
        } else {
            showToast("Intent error");
            finish();
        }

        images = new ArrayList<>();
        imagesLabels = new ArrayList<>();

        btnTakePicture = findViewById(R.id.btn_take);
        showToast("Ambil " + maximumImages + " Gambar");
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                if (mGray.total() == 0) {
                    showToast("Tidak ada wajah yang terdeksi");
                    return;

                }

                Size imageSize = new Size(200, 200);
                Imgproc.resize(mGray, mGray, imageSize);
                Log.i(TAG, "Small gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                //SaveImage(mGray);

                Mat image = mGray.reshape(0, (int) mGray.total()); // Create column vector
                Log.i(TAG, "Vector height: " + image.height() + " Width: " + image.width() + " total: " + image.total());
                addFace(name, image);

                if (images.size() >= maximumImages) {
                    btnTakePicture.setEnabled(false);
                    finish();
                } else {
                    showToast("Ambil " + (maximumImages - images.size()) + " gambar lagi");
                }

            }
        });


        mOpenCvCameraView = findViewById(R.id.camera_view);
        mOpenCvCameraView.setCameraIndex(1); //front camera
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }
    List<File> files;


    private void showToast(String message) {
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void addFace(String string, Mat face) {
        String label = string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1).trim().toLowerCase(Locale.US); // Make sure that the name is always uppercase and rest is lowercase
        imagesLabels.add(label); // Add label to list of labels
        if(face.total() != 0){
            int size = (int) (face.total() * face.channels());
            byte[] data = new byte[size];
            face.get(0, 0, data);
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));
            mDatabase.child("users").child(key).child("faces").push().child("data").setValue(dataString);
            images.add(face);
        }
        Log.i(TAG, "Label: " + label);
    }



    private BaseLoaderCallback mBaseLoader = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while((bytesRead = is.read(buffer)) != -1){
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if(mClassifier.empty()){
                            Log.e(TAG, "Failed to load cascade classifier");
                            mClassifier = null;
                        }
                        cascadeDir.delete();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }

                    mOpenCvCameraView.setCameraIndex(1); // select front camera

                    mOpenCvCameraView.enableView();

                    break;

                default:
                    super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                InitOpenCV();
            }
        } else {
            InitOpenCV();
        }
    }

    private void InitOpenCV(){
        if(OpenCVLoader.initDebug()){
            mBaseLoader.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Toast.makeText(this, "Failed to load OpenCV", Toast.LENGTH_LONG).show();
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mBaseLoader);
        }

    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    InitOpenCV();
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
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray();
        Mat mRgbaTmp = inputFrame.rgba();

        Core.rotate(mRgbaTmp, mRgbaTmp, Core.ROTATE_90_COUNTERCLOCKWISE);
        Core.rotate(gray, gray, Core.ROTATE_90_COUNTERCLOCKWISE);

        MatOfRect faces = new MatOfRect();
        mClassifier.detectMultiScale(gray, faces, 1.1, 2, 2,
                new Size(200, 200), new Size());

        Rect[] facesArray = faces.toArray();
        Mat mGrayTmp = new Mat();
        if(facesArray.length > 0) {
            Imgproc.rectangle(mRgbaTmp, facesArray[0].tl(), facesArray[0].br(), new Scalar(0, 255, 0), 3);
            mGrayTmp = new Mat(gray, facesArray[0]);
        }

        Core.rotate(mRgbaTmp, mRgbaTmp, Core.ROTATE_90_CLOCKWISE);
        Core.rotate(mGrayTmp, mGrayTmp, Core.ROTATE_90_CLOCKWISE);

        // Flip image to get mirror effect
        int orientation = mOpenCvCameraView.getScreenOrientation();
        if (mOpenCvCameraView.isEmulator()) // Treat emulators as a special case
            Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
        else {
            switch (orientation) { // RGB image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    Core.flip(mRgbaTmp, mRgbaTmp, 0); // Flip along x-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
                    break;
            }
            switch (orientation) { // Grayscale image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image

                    Core.flip(mGrayTmp, mGrayTmp, -1); // Flip along both axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:

                    Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    Core.flip(mGrayTmp, mGrayTmp, 0); // Flip along x-axis
                    break;
            }
        }



        mGray = mGrayTmp;
        mRgba = mRgbaTmp;

        return mRgba;
    }
}
