package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.Student.RegisterStudentActivity;
import com.example.rollcall.Teacher.RegisterTeacherActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";

    FirebaseAuth mAuth;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate:success");

        init();
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progress_bar_login);
        mProgressBar.setVisibility(View.INVISIBLE);

        buttonOnClickListener();
    }

    private void buttonOnClickListener() {
        findViewById(R.id.login_button).setOnClickListener(v -> {
            screenLock();
            EditText etEmail = findViewById(R.id.email_input_login);
            String email = etEmail.getText().toString();
            EditText etPassword = findViewById(R.id.password_input_login);
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                screenUnlock();
                Toast.makeText(LoginActivity.this, "Fill in all blank!", Toast.LENGTH_LONG).show();
                return;
            }

            signIn(email, password);
        });

        findViewById(R.id.login_to_register_button_student).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterStudentActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.login_to_register_button_teacher).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterTeacherActivity.class);
            startActivity(intent);
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Login failed!", Toast.LENGTH_LONG).show();
                }
                if (!user.isEmailVerified()) {
                    Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            else {
                screenUnlock();
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Login failed!\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
