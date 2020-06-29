package com.example.rollcall.Student;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rollcall.Camera.CameraActivity;
import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.RegisterLessonStudent.RegisterLessonStudentActivity;
import com.example.rollcall.Student.StudentLesson.StudentLessonsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;

public class MainStudentActivity extends AppCompatActivity {

    private final static int CAMERA_REQUEST_CODE = 1;
    private final static int QR_CODE_REQUEST_CODE = 1;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

        TextView nameTW = findViewById(R.id.student_main_name);
        TextView idTW = findViewById(R.id.student_main_id);

        String path = "users/students";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String id = ds.child("sId").getValue(String.class);
                    if (id.compareTo(user.getDisplayName()) == 0) {
                        idTW.setText(id);
                        String name = ds.child("name").getValue(String.class);
                        String surname = ds.child("surname").getValue(String.class);
                        nameTW.setText(name + " " + surname);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainStudentActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        TextView dateTW = findViewById(R.id.student_main_dateAndTime);

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
        FloatingActionButton fab = findViewById(R.id.fab_student);
        fab.setOnClickListener(view -> {

            if(ContextCompat.checkSelfPermission(MainStudentActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainStudentActivity.this, new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
            }
            else {
                Intent intentList = new Intent(MainStudentActivity.this, CameraActivity.class);
                startActivity(intentList);

                /*
                IntentIntegrator integrator = new IntentIntegrator(MainStudentActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
                */
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainStudentActivity.this, CameraActivity.class);
                    startActivityForResult(intent, QR_CODE_REQUEST_CODE);

                    /*
                    IntentIntegrator integrator = new IntentIntegrator(MainStudentActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    integrator.setOrientationLocked(false);
                    integrator.initiateScan();
                    */
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
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
        */
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_CODE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String result= data.getStringExtra("result");
                updateDatabase(result);
            }
        }
    }

    private void updateDatabase(String result) {
        //Log.d("MainStudentActivity", result);
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        String[] res = result.split(" ");
        if (!res[0].equals("$RC$")){
            Toast.makeText(this, "QR Code is invalid!", Toast.LENGTH_LONG).show();
            return;
        }
        String lesCode = res[1];
        String week = res[2];
        String date = res[3];
        String time = res[4];

        screenLock();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainStudentActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        String disName = user.getDisplayName();
        String path = "students/" + disName + "/registeredLesson";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
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
                String path = "lessons/" + lesCode + "/dates/" + week + "/" + date + "/" + time;
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
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

                        if (dataSnapshot.child("status").child(disName).getValue(Integer.class) == 1) {
                            Toast.makeText(MainStudentActivity.this, "Already done!", Toast.LENGTH_LONG).show();
                            screenUnlock();
                            return;
                        }

                        reference.child("status").child(disName).setValue(1).addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                String path = "students/" + disName + "/status/" + lesCode + "/" + week + "/" + date + "/" + time;

                                final DatabaseReference refStudent = FirebaseDatabase.getInstance().getReference(path);

                                refStudent.setValue(1).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        Toast.makeText(MainStudentActivity.this, "You were recorded in the poll", Toast.LENGTH_LONG).show();
                                        screenUnlock();
                                    }
                                    else {
                                        Toast.makeText(MainStudentActivity.this, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        screenUnlock();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(MainStudentActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                screenUnlock();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainStudentActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        screenUnlock();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainStudentActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
