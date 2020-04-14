package com.example.senior.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.senior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddLessonActivity extends AppCompatActivity {

    EditText lessonCode;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);

        init();
    }

    private void init() {
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);
        lessonCode = findViewById(R.id.lesson_code_input);

        Button add = findViewById(R.id.add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenLock();
                addLesson();
                finish();
            }
        });
    }

    private void addLesson() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        reference.child(lessonCode.getText().toString()).child("teacher").setValue(user.getDisplayName());

        String path = user.getDisplayName() + "/registeredLesson";
        final DatabaseReference regLessonRef = FirebaseDatabase.getInstance().getReference(path);
        regLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> hmRegisteredLessons = new HashMap<>();
                int counter = 1;
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    hmRegisteredLessons.put(ds.getKey(), ds.getValue(String.class));
                    counter++;
                }

                hmRegisteredLessons.put(Integer.toString(counter), lessonCode.getText().toString());
                regLessonRef.setValue(hmRegisteredLessons);

                Toast.makeText(AddLessonActivity.this, "Lesson added", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddLessonActivity.this, "Lesson could not be added", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        //reference.child(user.getDisplayName()).child("registeredLessons").
    }

    private void screenLock() {
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
