package com.example.senior.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.senior.Adapter.StudentLessonsAdapter;
import com.example.senior.Adapter.StudentStatusAdapter;
import com.example.senior.LoginActivity;
import com.example.senior.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentStatusActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_status);
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

        recyclerView = findViewById(R.id.recycleView_student_status);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StudentStatusActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        setListeners();
    }

    private void setListeners() {
        Intent intent = getIntent();
        String lesCode = intent.getStringExtra("lesCode");

        setTitle(lesCode);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String path = "students/" + user.getDisplayName() + "/status/" + lesCode;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> dates = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    dates.add(ds.getValue(String.class));
                }

                RecyclerView.Adapter adapter = new StudentStatusAdapter(StudentStatusActivity.this, dates);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentStatusActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
