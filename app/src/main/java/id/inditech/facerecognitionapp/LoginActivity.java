package id.inditech.facerecognitionapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText mPin;
    private Button btnMasuk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPin = findViewById(R.id.etPin);
        btnMasuk = findViewById(R.id.btnLogin);

        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Integer.parseInt(mPin.getText().toString()) == 111 || Integer.parseInt(mPin.getText().toString()) == 222 ){
                    finish();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Pin salah", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
