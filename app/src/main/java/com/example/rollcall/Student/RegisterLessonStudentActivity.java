package com.example.rollcall.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rollcall.Adapter.RegisterLessonAdapter;
import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegisterLessonStudentActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_lesson);
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

        recyclerView = findViewById(R.id.recyclerView_register_lesson);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RegisterLessonStudentActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        setListeners();
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_student_register);
        fab.setOnClickListener(v -> finish());

        String path = "teachers";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ArrayList<String>> regLessonList = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    for (DataSnapshot dsC: ds.child("registeredLesson").getChildren()) {
                        ArrayList<String> lessonTeacher = new ArrayList<>();
                        lessonTeacher.add(dsC.getValue(String.class));
                        lessonTeacher.add(ds.getKey());
                        regLessonList.add(lessonTeacher);
                    }
                }
                final ArrayList<ArrayList<String>> regLessonListTeacher = regLessonList;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(RegisterLessonStudentActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
                String path = "students/" + user.getDisplayName() + "/registeredLesson";
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> regLessonListStudent = new ArrayList<>();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            regLessonListStudent.add(ds.getValue(String.class));
                        }

                        RecyclerView.Adapter adapter = new RegisterLessonAdapter(RegisterLessonStudentActivity.this, regLessonListTeacher, regLessonListStudent);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(RegisterLessonStudentActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterLessonStudentActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
