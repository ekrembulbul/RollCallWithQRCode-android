package com.example.rollcall.Teacher.TeacherValidateAdmin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.RegisterLessonStudent.RegisterLessonStudentActivity;
import com.example.rollcall.Teacher.TeacherDate.TeacherDateActivity;
import com.example.rollcall.Teacher.TeacherLessons.TeacherLessonsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TeacherValidateAdminAdapter extends RecyclerView.Adapter<TeacherValidateAdminAdapter.MyViewHolder>  {

    private ArrayList<String> teachers;
    private LayoutInflater inflater;
    private Context context;

    public TeacherValidateAdminAdapter(Context context, ArrayList<String> teachers) {
        inflater = LayoutInflater.from(context);
        this.teachers = teachers;
        this.context = context;
    }

    @NonNull
    @Override
    public TeacherValidateAdminAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.teacher_validate_item, parent, false);
        TeacherValidateAdminAdapter.MyViewHolder holder = new TeacherValidateAdminAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherValidateAdminAdapter.MyViewHolder holder, int position) {
        holder.setData(teachers.get(position));
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        FloatingActionButton fab;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.teacher_validate_name);
            fab = itemView.findViewById(R.id.fab_teacher_validate);

            fab.setOnClickListener(view -> {
                updateDatabase();
            });
        }

        public void setData(final String selected) {
            this.name.setText(selected);
        }

        private void updateDatabase() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(context, LoginActivity.class);
                ((Activity)context).finish();
                context.startActivity(intent);
            }
            String path = "teachers/" + name.getText().toString() + "/validate";
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Teacher account validation successful", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Teacher account validation failure\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
