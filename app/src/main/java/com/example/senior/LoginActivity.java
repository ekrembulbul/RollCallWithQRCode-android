package com.example.senior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.senior.Student.RegisterStudentActivity;
import com.example.senior.Teacher.RegisterTeacherActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenLock();
                EditText etEmail = findViewById(R.id.email_input_login);
                String email = etEmail.getText().toString();
                EditText etPassword = findViewById(R.id.password_input_login);
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    screenUnlock();
                    Toast.makeText(LoginActivity.this, "Fill in all blank!", Toast.LENGTH_SHORT).show();
                    return;
                }

                signIn(email, password);
            }
        });

        findViewById(R.id.login_to_register_button_student).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterStudentActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.login_to_register_button_teacher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterTeacherActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;
                    if (!user.isEmailVerified()) {
                        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                        startActivity(intent);
                        screenUnlock();
                    }
                    else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
                else {
                    screenUnlock();
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
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
