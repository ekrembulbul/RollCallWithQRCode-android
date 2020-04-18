package com.example.senior.Student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.senior.Camera.CameraActivity;
import com.example.senior.LoginActivity;
import com.example.senior.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainStudentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_student);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_student, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainStudentActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.list_lesson:
                Intent intentList = new Intent(MainStudentActivity.this, StudentLessonsActivity.class);
                startActivity(intentList);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        setListeners();
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_student);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentList = new Intent(MainStudentActivity.this, CameraActivity.class);
                startActivity(intentList);
            }
        });


    }
}
