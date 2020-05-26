package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.Student.MainStudentActivity;
import com.example.rollcall.Teacher.TeacherLessons.TeacherLessonsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate:success");
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart:success");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "inside");
            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                readDataTeachers();
                readDataStudent();
            }
            else {
                Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else {
            Log.d(TAG, "inside");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progress_bar_main);
    }

    private void readDataTeachers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("/users/teachers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("email").getValue(String.class).compareTo(FirebaseAuth.getInstance().getCurrentUser().getEmail()) == 0) {
                        Intent intent = new Intent(MainActivity.this, TeacherLessonsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error!\n" + databaseError.toException().getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void readDataStudent() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("/users/students");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("email").getValue(String.class).compareTo(FirebaseAuth.getInstance().getCurrentUser().getEmail()) == 0) {
                        Intent intent = new Intent(MainActivity.this, MainStudentActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error!\n" + databaseError.toException().getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
