package com.example.senior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
        findViewById(R.id.verify_button_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenLock();
                verify();
            }
        });

        findViewById(R.id.send_verification_button_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerification();
            }
        });

        findViewById(R.id.signout_button_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private void verify() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) return;
                if (currentUser.isEmailVerified()) {
                    currentUser.getIdToken(true);
                    Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    screenUnlock();
                    Toast.makeText(VerifyActivity.this, "Check your email!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(VerifyActivity.this, "Verification email sent.", Toast.LENGTH_LONG).show();
                }
                else if (task.isCanceled()) {
                    Log.d(TAG, "Email could not be sent.");
                    Toast.makeText(VerifyActivity.this, "Verification email could not be sent.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure:Email could not be sent." + e.getMessage());
                Toast.makeText(VerifyActivity.this, "Try again later!", Toast.LENGTH_SHORT).show();
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
