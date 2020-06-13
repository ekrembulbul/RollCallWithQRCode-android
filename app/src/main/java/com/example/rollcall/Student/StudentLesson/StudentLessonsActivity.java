package com.example.rollcall.Student.StudentLesson;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.RegisterLessonStudent.RegisterLessonStudentActivity;
import com.example.rollcall.Student.StudentLessonChartActivity;
import com.example.rollcall.Teacher.TeacherLessonChartActivity;
import com.example.rollcall.Teacher.TeacherLessons.TeacherLessonsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentLessonsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lessons);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_student_lesson_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chart:
                Intent intentChart = new Intent(this, StudentLessonChartActivity.class);
                intentChart.putStringArrayListExtra("lesCodes", ((StudentLessonsAdapter) adapter)._registeredLessonList);
                intentChart.putStringArrayListExtra("attendance", ((StudentLessonsAdapter) adapter).attendanceRate);
                startActivity(intentChart);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.progress_bar_student_lesson);
        mProgressBar.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recycleView_student);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StudentLessonsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentLessonsAdapter(StudentLessonsActivity.this);
        recyclerView.setAdapter(adapter);

        setListeners();
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_student_add);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(StudentLessonsActivity.this, RegisterLessonStudentActivity.class);
            startActivity(intent);
        });
    }

    public void screenLock() {
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void screenUnlock() {
        mProgressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
