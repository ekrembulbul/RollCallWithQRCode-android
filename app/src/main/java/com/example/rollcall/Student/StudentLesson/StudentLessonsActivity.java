package com.example.rollcall.Student.StudentLesson;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.RegisterLessonStudent.RegisterLessonStudentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentLessonsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lessons);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycleView_student);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StudentLessonsActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        setListeners();
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_student_add);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(StudentLessonsActivity.this, RegisterLessonStudentActivity.class);
            startActivity(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(StudentLessonsActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        String path = "students/" + user.getDisplayName() + "/registeredLesson";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> regLessonList = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    regLessonList.add(ds.getValue(String.class));
                }

                RecyclerView.Adapter adapter = new StudentLessonsAdapter(StudentLessonsActivity.this, regLessonList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentLessonsActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
