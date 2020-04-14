package com.example.senior;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.senior.Student.MainStudentActivity;
import com.example.senior.Teacher.MainTeacherActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                readDataTeachers();
                readDataStudent();
            }
            else {
                Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                finish();
                startActivity(intent);
            }
        }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mProgressBar = findViewById(R.id.progress_bar_main);
        buttonOnClickListener();
    }

    private void buttonOnClickListener() {

    }

    private void readDataTeachers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("/users/teachers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("email").getValue(String.class).compareTo(FirebaseAuth.getInstance().getCurrentUser().getEmail()) == 0) {
                        Intent intent = new Intent(MainActivity.this, MainTeacherActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
                        finish();
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
