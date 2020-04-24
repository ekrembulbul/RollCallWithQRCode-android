package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyActivity extends AppCompatActivity {
    private final String TAG = "VerifyActivity";

    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        
        init();
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progress_bar_verify);
        mProgressBar.setVisibility(View.INVISIBLE);

        buttonOnClickListener();
    }

    private void buttonOnClickListener() {
        findViewById(R.id.verify_button_verify).setOnClickListener(v -> {
            screenLock();
            verify();
        });

        findViewById(R.id.send_verification_button_verify).setOnClickListener(v -> sendEmailVerification());

        findViewById(R.id.signout_button_verify).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        });
    }

    private void verify() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        currentUser.reload().addOnCompleteListener(task -> {
            FirebaseUser currentUser1 = mAuth.getCurrentUser();
            if (currentUser1 == null) return;
            if (currentUser1.isEmailVerified()) {
                currentUser1.getIdToken(true);
                Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                screenUnlock();
                Toast.makeText(VerifyActivity.this, "Check your email!", Toast.LENGTH_LONG).show();
            }

        });
    }

    private void sendEmailVerification() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        currentUser.sendEmailVerification().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Email sent.");
            Toast.makeText(VerifyActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
        }).addOnCanceledListener(() -> {
            Log.d(TAG, "Email could not be sent.");
            Toast.makeText(VerifyActivity.this, "Verification email could not be sent.", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Failure: " + e.getMessage());
            Toast.makeText(VerifyActivity.this, "Try again later!\n" + e.getMessage(), Toast.LENGTH_LONG).show();
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
