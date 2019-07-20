package id.inditech.facerecognitionapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {

    private ImageView btnEdit;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private ListView mHistory;
    List<User> listUser;
    private DatabaseReference mDatabase;
    UserListAdapter adapter;
    private TextView mTitle, mDate, mTime, mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btnEdit = findViewById(R.id.ivEdit);
        mHistory = findViewById(R.id.lvHistory);
        mTitle = findViewById(R.id.tvTanggal);
        mName = findViewById(R.id.tvName);
        mDate = findViewById(R.id.tvDate);
        mTime = findViewById(R.id.tvTime);

        dateFormatter = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        listUser = new ArrayList<>();

        adapter = new UserListAdapter(this, listUser);
        mHistory.setAdapter(adapter);

        mDatabase.child("users").addValueEventListener(postListener);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });
    }

    private void showDateDialog(){
        Calendar newCalendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                Toast.makeText(HistoryActivity.this, "Riwayat tanggal "+
                        dateFormatter.format(newDate.getTime()), Toast.LENGTH_SHORT).show();
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            listUser.clear();
            for(DataSnapshot data : dataSnapshot.getChildren()){
                User user = new User(data.child("nama").getValue(String.class));
                user.setId(data.getKey());
//                user.setWaktu(data.getKey());
//                user.setTanggal(data.getKey());
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
}
