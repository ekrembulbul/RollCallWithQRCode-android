package com.example.senior.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;

import com.example.senior.LoginActivity;
import com.example.senior.R;
import com.example.senior.Teacher.AddLessonTeacherActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class MainStudentActivity extends AppCompatActivity {

    ProgressBar mProgressBar;

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
        mProgressBar = findViewById(R.id.progress_bar_student_main);
        mProgressBar.setVisibility(View.INVISIBLE);

        setListeners();
    }

    private void setListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_student);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Intent intentList = new Intent(MainStudentActivity.this, CameraActivity.class);
                startActivity(intentList);
                */
                IntentIntegrator integrator = new IntentIntegrator(MainStudentActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Log.d("MainStudent", result.getContents());
                updateDatabase(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateDatabase(String result) {
        String[] res = result.split(" ");
        String lesCode = res[0];
        String date = res[1];

        screenLock();

        String disName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String path = "students/" + disName + "/registeredLesson";
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> lessons = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    lessons.add(ds.getValue(String.class));
                }

                boolean lessonFlag = false;
                for(String lesson : lessons) {
                    if (lesson.compareTo(lesCode) == 0) {
                        lessonFlag = true;
                    }
                }
                if (!lessonFlag) {
                    Toast.makeText(MainStudentActivity.this, "You are not registered for the lesson!", Toast.LENGTH_LONG).show();
                    screenUnlock();
                    return;
                }

                String disName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                String path = "lessons/" + lesCode + "/dates/" + date;
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean active = false;
                        if (dataSnapshot.child("active").getValue(boolean.class) != null) {
                            active = dataSnapshot.child("active").getValue(boolean.class);
                        }

                        if (!active) {
                            Toast.makeText(MainStudentActivity.this, "QR code is inactive!", Toast.LENGTH_LONG).show();
                            screenUnlock();
                            return;
                        }

                        ArrayList<String> students = new ArrayList<>();
                        for (DataSnapshot ds: dataSnapshot.child("status").getChildren()) {
                            students.add(ds.getValue(String.class));
                        }

                        for(String student : students) {
                            if (student.compareTo(disName) == 0) {
                                Toast.makeText(MainStudentActivity.this, "Already done!", Toast.LENGTH_LONG).show();
                                screenUnlock();
                                return;
                            }
                        }

                        students.add(disName);
                        reference.child("status").setValue(students).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainStudentActivity.this, "You were recorded in the poll", Toast.LENGTH_LONG).show();
                                screenUnlock();
                            }
                        });

                        String path = "students/" + disName + "/status/" + lesCode;
                        final DatabaseReference refStudent = FirebaseDatabase.getInstance().getReference(path);
                        refStudent.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<String> dates = new ArrayList<>();
                                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                    dates.add(ds.getValue(String.class));
                                }

                                for(String d : dates) {
                                    if (d.compareTo(date) == 0) {
                                        //Toast.makeText(MainStudentActivity.this, "Already done!", Toast.LENGTH_LONG).show();
                                        screenUnlock();
                                        return;
                                    }
                                }

                                dates.add(date);
                                refStudent.setValue(dates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //Toast.makeText(MainStudentActivity.this, "You were recorded in the poll", Toast.LENGTH_SHORT).show();
                                        screenUnlock();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(MainStudentActivity.this, "Error: \n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                screenUnlock();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainStudentActivity.this, "Error: \n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        screenUnlock();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainStudentActivity.this, "Error: \n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
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
