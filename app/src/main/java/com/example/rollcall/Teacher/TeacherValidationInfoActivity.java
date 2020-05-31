package com.example.rollcall.Teacher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.VerifyActivity;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherValidationInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_validation_info);

        Button signOut = findViewById(R.id.button_teacher_validation_info_signout);
        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        });
    }
}
