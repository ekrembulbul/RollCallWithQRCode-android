package com.example.senior.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.senior.Adapter.TeacherDateAdapter;
import com.example.senior.Adapter.TeacherStatusAdapter;
import com.example.senior.LoginActivity;
import com.example.senior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeacherStatusActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_status);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TeacherStatusActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        recyclerView = findViewById(R.id.recycleView_teacher_status);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TeacherStatusActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        setListeners();
    }

    private void setListeners() {
        Intent intent = getIntent();
        final String lesCode = intent.getStringExtra("lesCode");
        final String date = intent.getStringExtra("date");

        setTitle(date);

        String path = "lessons/" + lesCode + "/dates/" + date + "/status";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> names = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    names.add(ds.getValue(String.class));
                }

                RecyclerView.Adapter adapter = new TeacherStatusAdapter(TeacherStatusActivity.this, names);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherStatusActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
