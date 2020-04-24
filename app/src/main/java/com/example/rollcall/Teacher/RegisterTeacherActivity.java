package com.example.rollcall.Teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.Data.User;
import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.VerifyActivity;
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
        findViewById(R.id.register_button).setOnClickListener(v -> {
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
                Toast.makeText(RegisterTeacherActivity.this, "Fill in all blank!", Toast.LENGTH_LONG).show();
                return;
            }

            User user = new User(null, degree, name, surname, email);
            readDataAndRegister(user, password);
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
                screenUnlock();
                Toast.makeText(RegisterTeacherActivity.this, "Database error!\n" + databaseError.toException().getMessage(), Toast.LENGTH_LONG).show();
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void register(@NonNull final ArrayList<User> readUsers, final User user, final String password) {
        for (User tmpUser: readUsers) {
            if (tmpUser.degree.compareTo(user.degree) == 0 && tmpUser.name.compareTo(user.name) == 0 && tmpUser.surname.compareTo(user.surname) == 0) {
                Toast.makeText(RegisterTeacherActivity.this, "Already exist teacher", Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
            if (tmpUser.email.compareTo(user.email) == 0) {
                Toast.makeText(RegisterTeacherActivity.this, "Already exist EMAIL", Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        }
        createAccount(user, password);
    }

    private void createAccount(@NonNull final User user, final String password) {
        mAuth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "createUserWithEmail:success");

                updateAccount(user.degree + " " + user.name + " " + user.surname);
                addUserToDatabase(user);
                sendEmailVerification();

                Toast.makeText(RegisterTeacherActivity.this, "Registration successful\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterTeacherActivity.this, VerifyActivity.class);
                startActivity(intent);
                finish();
                screenUnlock();
            }
            else {
                screenUnlock();
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                Toast.makeText(RegisterTeacherActivity.this, "Registration failed!\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void addUserToDatabase(final User user) {
        DatabaseReference reference = mDatabase.getReference();
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(RegisterTeacherActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        reference.child("users/teachers").child(fUser.getUid()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "addUserToDatabase: success");
            }
            else {
                Log.d(TAG, "addUserToDatabase: failed");
            }
        });
    }

    private void updateAccount(final String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(RegisterTeacherActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User profile updated.");
            }
            else {
                Log.d(TAG, "User profile could not be updated.");
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(RegisterTeacherActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        currentUser.sendEmailVerification().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Email sent.");
            Toast.makeText(RegisterTeacherActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
        }).addOnCanceledListener(() -> {
            Log.d(TAG, "Email could not be sent.");
            Toast.makeText(RegisterTeacherActivity.this, "Verification email could not be sent.", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Failure: " + e.getMessage());
            Toast.makeText(RegisterTeacherActivity.this, "Try again later!\n" + e.getMessage(), Toast.LENGTH_LONG).show();
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
