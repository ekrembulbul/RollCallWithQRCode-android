package com.example.senior.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.senior.R;
import com.example.senior.Teacher.AddLessonTeacherActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = v.findViewById(R.id.checkBox_register);
                    if (cb.isEnabled()) {
                        _checkBox.toggle();
                    }
                }
            });

            _checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isEnabled() && isChecked) {
                        alertDialog();
                    }
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
                    .setTitle("Register Lesson")
                    .setMessage("Are you sure you want to register this lesson?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            registerLesson();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            _checkBox.setChecked(false);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            _checkBox.setChecked(false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        public void registerLesson() {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String path = "lessons/" + _lessonCode.getText().toString() + "/student";
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> alRegisteredLessons = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        alRegisteredLessons.add(ds.getValue(String.class));
                    }

                    alRegisteredLessons.add(user.getDisplayName());
                    reference.setValue(alRegisteredLessons).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(_context, "Registered for lesson", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(_context, "Could not be register to lesson\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            path = "students/" + user.getDisplayName() + "/registeredLesson";
            final DatabaseReference regLessonRef = FirebaseDatabase.getInstance().getReference(path);
            regLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> alRegisteredLessons = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        alRegisteredLessons.add(ds.getValue(String.class));
                    }

                    alRegisteredLessons.add(_lessonCode.getText().toString());
                    regLessonRef.setValue(alRegisteredLessons);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(_context, "Lesson could not be added\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}