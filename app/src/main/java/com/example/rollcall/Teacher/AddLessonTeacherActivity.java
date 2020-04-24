package com.example.rollcall.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.VerifyActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddLessonTeacherActivity extends AppCompatActivity {

    EditText lessonCode;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);

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

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);
        lessonCode = findViewById(R.id.lesson_code_input);

        Button add = findViewById(R.id.add_button);
        add.setOnClickListener(v -> addLesson());
    }

    private void addLesson() {
        if (lessonCode.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter lesson code!", Toast.LENGTH_SHORT).show();
            return;
        }

        screenLock();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(AddLessonTeacherActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        String path = "lessons/" + lessonCode.getText().toString() + "/teacher";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.setValue(user.getDisplayName());

        path = "teachers/" + user.getDisplayName() + "/registeredLesson";
        final DatabaseReference regLessonRef = FirebaseDatabase.getInstance().getReference(path);
        regLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> alRegisteredLessons = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    alRegisteredLessons.add(ds.getValue(String.class));
                }

                for(String lesson : alRegisteredLessons) {
                    if (lesson.compareTo(lessonCode.getText().toString()) == 0) {
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson already exist!", Toast.LENGTH_SHORT).show();
                        screenUnlock();
                        return;
                    }
                }

                alRegisteredLessons.add(lessonCode.getText().toString());
                regLessonRef.setValue(alRegisteredLessons).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson added", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        screenUnlock();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void screenLock() {
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void screenUnlock() {
        mProgressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
