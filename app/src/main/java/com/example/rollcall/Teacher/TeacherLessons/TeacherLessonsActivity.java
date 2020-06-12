package com.example.rollcall.Teacher.TeacherLessons;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.MainStudentActivity;
import com.example.rollcall.Student.RegisterStudentActivity;
import com.example.rollcall.Teacher.AddLessonTeacherActivity;
import com.example.rollcall.Teacher.TeacherValidateAdmin.TeacherValidateAdminActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class TeacherLessonsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_teacher);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        if (user.getEmail().equals("ekrem.bulbul.52@gmail.com")) {
            inflater.inflate(R.menu.menu_teacher_lessons, menu);
        }
        else {
            inflater.inflate(R.menu.menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.validate_teahcer:
                Intent intent = new Intent(this, TeacherValidateAdminActivity.class);
                startActivity(intent);
                return true;
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        mProgressBar = findViewById(R.id.progress_bar_teacher_lesson);
        mProgressBar.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recyclerView_teacher);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TeacherLessonsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TeacherLessonsAdapter(TeacherLessonsActivity.this);
        recyclerView.setAdapter(adapter);

        setListeners();
        setNameAndId();
        setDateAndTime();
    }

    private void setNameAndId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        TextView nameTW = findViewById(R.id.teacher_main_name);
        nameTW.setText(user.getDisplayName());
    }

    private void setDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        TextView dateTW = findViewById(R.id.teacher_main_dateAndTime);

        String dayName = "";
        int dayNameI = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayNameI == 1) dayName = "Sunday";
        else if (dayNameI == 2) dayName = "Monday";
        else if (dayNameI == 3) dayName = "Tuesday";
        else if (dayNameI == 4) dayName = "Wednesday";
        else if (dayNameI == 5) dayName = "Thursday";
        else if (dayNameI == 6) dayName = "Friday";
        else if (dayNameI == 7) dayName = "Saturday";

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        dateTW.setText(dayName + " " + day + "/" + month + "/" + year);
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_teacher);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(TeacherLessonsActivity.this, AddLessonTeacherActivity.class);
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
