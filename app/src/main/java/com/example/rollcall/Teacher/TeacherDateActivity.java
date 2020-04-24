package com.example.rollcall.Teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.Adapter.TeacherDateAdapter;
import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeacherDateActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_date);
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

        recyclerView = findViewById(R.id.recycleView_teacher_date);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TeacherDateActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        setListeners();
    }

    private void setListeners() {
        Intent intent = getIntent();
        final String lesCode = intent.getStringExtra("lesCode");

        setTitle(lesCode);

        String path = "lessons/" + lesCode + "/dates";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> dates = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    dates.add(ds.getKey());
                }

                RecyclerView.Adapter adapter = new TeacherDateAdapter(TeacherDateActivity.this, dates, lesCode);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherDateActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
