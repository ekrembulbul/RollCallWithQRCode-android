package com.example.rollcall.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class RegisterLessonAdapter extends RecyclerView.Adapter<RegisterLessonAdapter.MyViewHolder> {

    private ArrayList<ArrayList<String>> _registeredLessonList;
    private ArrayList<String> _regLessonListStudent;
    private LayoutInflater inflater;
    private Context _context;

    public RegisterLessonAdapter(Context context, ArrayList<ArrayList<String>> registeredLessonList, ArrayList<String> registeredLessonListStudent) {
        _context = context;
        inflater = LayoutInflater.from(context);
        _registeredLessonList = registeredLessonList;
        _regLessonListStudent = registeredLessonListStudent;
    }

    @NonNull
    @Override
    public RegisterLessonAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.register_lesson_item, parent, false);
        RegisterLessonAdapter.MyViewHolder holder = new RegisterLessonAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RegisterLessonAdapter.MyViewHolder holder, int position) {
        holder.setData(_registeredLessonList.get(position));
    }

    @Override
    public int getItemCount() {
        return _registeredLessonList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView _lessonCode;
        TextView _teacherName;
        CheckBox _checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            _lessonCode = itemView.findViewById(R.id.lesson_code_text_register);
            _teacherName = itemView.findViewById(R.id.lesson_code_text_register2);
            _checkBox = itemView.findViewById(R.id.checkBox_register);

            itemView.setOnClickListener(v -> {
                if (_checkBox.isEnabled()) {
                    _checkBox.toggle();
                }
            });

            _checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isEnabled() && isChecked) {
                    alertDialog();
                }
            });
        }

        public void setData(final ArrayList<String> selected) {
            this._lessonCode.setText(selected.get(0));
            this._teacherName.setText(selected.get(1));

            for (String lesson: _regLessonListStudent) {
                if (lesson.compareTo(_lessonCode.getText().toString()) == 0) {
                    this._checkBox.setEnabled(false);
                    this._checkBox.setChecked(true);
                }
            }
        }

        public void alertDialog() {
            new AlertDialog.Builder(_context)
                    .setTitle("Register to Lesson")
                    .setMessage("Are you sure you want to register this lesson?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> registerLesson())
                    .setNegativeButton(android.R.string.no, (dialog, which) -> _checkBox.setChecked(false))
                    .setOnCancelListener(dialog -> _checkBox.setChecked(false))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        public void registerLesson() {
            String path = "lessons/" + _lessonCode.getText().toString() + "/student";
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> alRegisteredLessons = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        alRegisteredLessons.add(ds.getValue(String.class));
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Intent intent = new Intent(_context, LoginActivity.class);
                        ((Activity) _context).finish();
                        _context.startActivity(intent);
                    }
                    alRegisteredLessons.add(user.getDisplayName());

                    reference.setValue(alRegisteredLessons).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String path = "students/" + user.getDisplayName() + "/registeredLesson";
                            DatabaseReference regLessonRef = FirebaseDatabase.getInstance().getReference(path);
                            regLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ArrayList<String> alRegisteredLessons = new ArrayList<>();
                                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                        alRegisteredLessons.add(ds.getValue(String.class));
                                    }

                                    alRegisteredLessons.add(_lessonCode.getText().toString());
                                    regLessonRef.setValue(alRegisteredLessons).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(_context, "Registered for lesson", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(_context, "Could not be register to lesson\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(_context, "Could not be register to lesson\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else {
                            Toast.makeText(_context, "Could not be register to lesson\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(_context, "Could not be register to lesson\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}