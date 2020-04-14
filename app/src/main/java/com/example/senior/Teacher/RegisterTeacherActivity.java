package com.example.senior.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.senior.R;
import com.example.senior.Data.User;
import com.example.senior.VerifyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegisterTeacherActivity extends AppCompatActivity {
    private final String TAG = "RegisterTeacherActivity";

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    ProgressBar mProgressBar;
    Spinner sDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_teacher);

        init();
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mProgressBar = findViewById(R.id.progress_bar_register);
        mProgressBar.setVisibility(View.INVISIBLE);

        sDegree = findViewById(R.id.degree_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.degree_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDegree.setAdapter(adapter);

        buttonOnClickListener();
    }

    private void buttonOnClickListener() {
        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenLock();

                String degree = sDegree.getSelectedItem().toString();
                EditText etName = findViewById(R.id.name_input_register);
                String name = etName.getText().toString();
                EditText etSurname = findViewById(R.id.surname_input_register);
                String surname = etSurname.getText().toString();
                EditText etEmail = findViewById(R.id.email_input_register);
                String email = etEmail.getText().toString();
                EditText etPassword = findViewById(R.id.password_input_register);
                String password = etPassword.getText().toString();

                if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    screenUnlock();
                    Toast.makeText(RegisterTeacherActivity.this, "Fill in all blank!", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User(null, degree, name, surname, email);
                readDataAndRegister(user, password);
            }
        });
    }

    private void readDataAndRegister(final User user, final String password) {
        DatabaseReference reference = mDatabase.getReference("/users/teachers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> readUsers = new ArrayList<>();

                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    User tmpUser = new User();
                    tmpUser.degree = ds.child("degree").getValue(String.class);
                    tmpUser.email = ds.child("email").getValue(String.class);
                    tmpUser.sId = ds.child("sId").getValue(String.class);
                    tmpUser.name = ds.child("name").getValue(String.class);
                    tmpUser.surname = ds.child("surname").getValue(String.class);
                    readUsers.add(tmpUser);
                }
                Log.d(TAG, "reading: success.");

                register(readUsers, user, password);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void register(final ArrayList<User> readUsers, final User user, final String password) {

        for (User tmpUser: readUsers) {
            if (tmpUser.degree.compareTo(user.degree) == 0 && tmpUser.name.compareTo(user.name) == 0 && tmpUser.surname.compareTo(user.surname) == 0) {
                Toast.makeText(RegisterTeacherActivity.this, "Already exist teacher", Toast.LENGTH_SHORT).show();
                screenUnlock();
                return;
            }
            if (tmpUser.email.compareTo(user.email) == 0) {
                Toast.makeText(RegisterTeacherActivity.this, "Already exist EMAIL", Toast.LENGTH_SHORT).show();
                screenUnlock();
                return;
            }
        }
        createAccount(user, password);
    }

    private void createAccount(final User user, final String password) {
        mAuth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");

                    updateAccount(user.degree + " " + user.name + " " + user.surname);
                    sendEmailVerification();
                    addUserToDatabase(user);

                    Intent intent = new Intent(RegisterTeacherActivity.this, VerifyActivity.class);
                    startActivity(intent);
                    screenUnlock();
                }
                else {
                    screenUnlock();
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegisterTeacherActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void addUserToDatabase(final User user) {
        DatabaseReference reference = mDatabase.getReference();
        FirebaseUser fUser = mAuth.getCurrentUser();
        reference.child("users/teachers").child(fUser.getUid()).setValue(user);
        Log.d(TAG, "addUserToDatabase: success");
    }

    private void updateAccount(final String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User profile updated.");
                }
                else {
                    Log.d(TAG, "User profile could not be updated.");
                }
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email sent.");
                    Toast.makeText(RegisterTeacherActivity.this, "Verification email sent.", Toast.LENGTH_LONG).show();
                }
                else if (task.isCanceled()) {
                    Log.d(TAG, "Email could not be sent.");
                    Toast.makeText(RegisterTeacherActivity.this, "Verification email could not be sent.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure: " + e.getMessage());
                Toast.makeText(RegisterTeacherActivity.this, "Try again later!", Toast.LENGTH_SHORT).show();
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
