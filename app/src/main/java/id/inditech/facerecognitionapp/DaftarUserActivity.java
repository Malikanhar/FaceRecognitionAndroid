package id.inditech.facerecognitionapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class DaftarUserActivity extends AppCompatActivity {
    private ListView lvUser;
    private Button btnTambah;
    List<User> listUser;
    private DatabaseReference mDatabase;
    UserListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_user);
        lvUser = findViewById(R.id.lv_user);
        btnTambah = findViewById(R.id.btn_tambah);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        listUser = new ArrayList<>();

        adapter = new UserListAdapter(this, listUser);
        lvUser.setAdapter(adapter);

        mDatabase.child("users").addValueEventListener(postListener);

        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUser();
            }
        });

        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DaftarUserActivity.this);
                builder.setTitle("Apakah anda ingin menambahkan jumlah wajah?");
                builder.setPositiveButton("TAMBAH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User user = adapter.getItem(position);
                        Intent intent = new Intent(DaftarUserActivity.this, RegisterFaceActivity.class);
                        intent.putExtra("key", user.getId());
                        intent.putExtra("nama", user.getNama());
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("BATAL", null);
                builder.create().show();
            }
        });
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            listUser.clear();
            for(DataSnapshot data : dataSnapshot.getChildren()){
                User user = new User(data.child("nama").getValue(String.class));
                user.setId(data.getKey());
                for(DataSnapshot face : data.child("faces").getChildren()){
                    String faceData = face.child("data").getValue(String.class);
                    byte[] buffer = Base64.decode(faceData, Base64.DEFAULT);
                    Mat mat = new Mat(buffer.length, 1, CvType.CV_8U);
                    mat.put(0, 0, buffer);
                    user.addFace(mat);
                }
                listUser.add(user);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    private void addNewUser(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Masukkan nama user yang baru:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", null); // Set up positive button, but do not provide a listener, so we can check the string before dismissing the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false); // User has to input a name
        AlertDialog dialog = builder.create();

        // Source: http://stackoverflow.com/a/7636468/2175837
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button mButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String string = input.getText().toString().trim();
                        if (!string.isEmpty()) { // Make sure the input is valid
                            // If input is valid, dismiss the dialog and add the label to the array

                            String key = mDatabase.child("users").push().getKey();
                            Intent intent = new Intent(DaftarUserActivity.this, RegisterFaceActivity.class);
                            mDatabase.child("users").child(key).child("nama").setValue(string);
                            intent.putExtra("key", key);
                            intent.putExtra("nama", string);
                            startActivity(intent);
//                            showToast(name);
                            dialog.dismiss();


                        }
                    }
                });
            }
        });

        // Show keyboard, so the user can start typing straight away
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();

    }
}
