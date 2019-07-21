package id.inditech.facerecognitionapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {

    private ImageView btnEdit;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private ListView mHistory;
    List<Absent> listAbsent, temp;
    private DatabaseReference mDatabase;
    HistoryListAdapter adapter;
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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateFormatter = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        listAbsent = new ArrayList<>();
        temp = new ArrayList<>();

        adapter = new HistoryListAdapter(this, listAbsent);
        mHistory.setAdapter(adapter);

        mDatabase.child("absents").addValueEventListener(postListener);

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
                mTitle.setText(dateFormatter.format(newDate.getTime()));
                listAbsent.clear();
                for(Absent absent : temp){
                    if(absent.getTanggal().equals(dateFormatter.format(newDate.getTime()))){
                        listAbsent.add(absent);
                    }
                }
                adapter.notifyDataSetChanged();
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();


    }

    public String getTanggal(){
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        return formatter.format(today);
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            listAbsent.clear();
            temp.clear();
            Log.d("tes data", dataSnapshot.getKey());
            for(DataSnapshot data : dataSnapshot.getChildren()){
              Absent absent = new Absent();
              absent.setName(data.child("nama").getValue(String.class));
              absent.setTanggal(data.child("tanggal").getValue(String.class));
              absent.setTime(data.child("waktu").getValue(String.class));
              if(getTanggal().equals(absent.getTanggal())){
                  listAbsent.add(absent);
              }
                temp.add(absent);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
