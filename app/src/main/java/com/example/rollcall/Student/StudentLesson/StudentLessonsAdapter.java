package com.example.rollcall.Student.StudentLesson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.StudentStatus.StudentStatusActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentLessonsAdapter extends RecyclerView.Adapter<StudentLessonsAdapter.MyViewHolder> {

    private Context _context;
    public ArrayList<String> _registeredLessonList;
    public ArrayList<String> attendanceRate;
    private LayoutInflater inflater;

    public StudentLessonsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        _context = context;
        setData();
    }

    private void setData() {
        ((StudentLessonsActivity)_context).screenLock();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(_context, LoginActivity.class);
            ((Activity)_context).finish();
            _context.startActivity(intent);
        }
        String path = "students/" + user.getDisplayName() + "/registeredLesson";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> regLessonList = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    regLessonList.add(ds.getValue(String.class));
                }

                _registeredLessonList = regLessonList;
                notifyDataSetChanged();
                attendanceRate = new ArrayList<>(_registeredLessonList.size());
                for (int i = 0; i < _registeredLessonList.size(); i++) {
                    attendanceRate.add("0/0");
                }

                ((StudentLessonsActivity)_context).screenUnlock();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                ((StudentLessonsActivity)_context).screenUnlock();
            }
        });
        path = "lessons";
        reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @NonNull
    @Override
    public StudentLessonsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.student_lesson_item, parent, false);
        StudentLessonsAdapter.MyViewHolder holder = new StudentLessonsAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentLessonsAdapter.MyViewHolder holder, int position) {
        if (_registeredLessonList != null) holder.setData(_registeredLessonList.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (_registeredLessonList != null) return _registeredLessonList.size();
        else return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView lessonCode;
        TextView lessonName;
        TextView lessonNumber;
        TextView lessonDay;
        TextView lessonTime;
        TextView lessonWeek;
        TextView lessonWeekResult;
        TextView lessonLesson;
        TextView lessonLessonResult;
        TextView lessonAttendanceRate;
        TextView lessonAttendanceRateResult;
        ImageView imageView;
        FloatingActionButton fab;

        public MyViewHolder(View itemView) {
            super(itemView);
            lessonName = itemView.findViewById(R.id.lesson_name_studentLesson);
            lessonNumber = itemView.findViewById(R.id.lesson_number_studentLesson);
            lessonCode = itemView.findViewById(R.id.lesson_code_studentLesson);
            lessonDay = itemView.findViewById(R.id.lesson_day_studentLesson);
            lessonTime = itemView.findViewById(R.id.lesson_time_studentLesson);
            lessonWeek = itemView.findViewById(R.id.lesson_week_studentLesson);
            lessonWeekResult = itemView.findViewById(R.id.lesson_week_result_studentLesson);
            lessonLesson = itemView.findViewById(R.id.lesson_lesson_studentLesson);
            lessonLessonResult = itemView.findViewById(R.id.lesson_lesson_result_studentLesson);
            lessonAttendanceRate = itemView.findViewById(R.id.lesson_attendanceRate_studentLesson);
            lessonAttendanceRateResult = itemView.findViewById(R.id.lesson_attendanceRate_result_studentLesson);
            imageView = itemView.findViewById(R.id.imageView_lesson_status);
            fab = itemView.findViewById(R.id.fab_student_lesson_status);

            fab.setOnClickListener(view -> {
                Intent intent = new Intent(_context, StudentStatusActivity.class);
                intent.putExtra("lesCode", lessonCode.getText().toString());
                intent.putExtra("day", lessonDay.getText().toString());
                _context.startActivity(intent);
            });
        }

        public void setData(final String selected, int position) {
            String path = "lessons/" + selected;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    lessonCode.setText(selected);
                    lessonName.setText(dataSnapshot.child("name").getValue(String.class));

                    int counter;
                    counter = 0;
                    lessonDay.setText("");
                    lessonTime.setText("");
                    for (DataSnapshot ds: dataSnapshot.child("dayAndTime").getChildren()) {
                        String startTime = ds.child("time").getValue(String.class);
                        int numberOfLesson = Integer.parseInt(ds.child("numberOfLesson").getValue(String.class));
                        String[] startTimeSplit = startTime.split("-");
                        int iEndTime = Integer.parseInt(startTimeSplit[0]) + numberOfLesson;
                        if (iEndTime >= 24) iEndTime -= 24;
                        startTimeSplit[0] = String.valueOf(iEndTime);
                        String endTime = startTimeSplit[0] + "-" + startTimeSplit[1];

                        startTime = printTimeVer(startTime);
                        endTime = printTimeVer(endTime);

                        String day = "";
                        int iDay = Integer.parseInt(ds.child("day").getValue(String.class));
                        if (iDay == 1) day = "Sunday";
                        else if (iDay == 2) day = "Monday";
                        else if (iDay == 3) day = "Tuesday";
                        else if (iDay == 4) day = "Wednesday";
                        else if (iDay == 5) day = "Thursday";
                        else if (iDay == 6) day = "Friday";
                        else if (iDay == 7) day = "Saturday";

                        if (counter == 0) {
                            lessonDay.append(day);
                            lessonTime.append(startTime + "-" + endTime);
                        }
                        else {
                            lessonDay.append("\n" + day);
                            lessonTime.append("\n" + startTime + "-" + endTime);
                        }

                        counter++;
                    }

                    lessonWeek.setText("Week:");
                    int week = Integer.parseInt(dataSnapshot.child("numberOfWeek").getValue(String.class));
                    lessonWeekResult.setText(getWeekStatus(dataSnapshot) + "/" + week);

                    lessonLesson.setText("Lesson (1 hour):");
                    String[] ret = getLessonStatus(dataSnapshot);
                    lessonLessonResult.setText(ret[0] + "/" + ret[1]);

                    lessonAttendanceRate.setText("Attendance Rate:");

                    String[] reta = getAttendanceRate(dataSnapshot);
                    int aRate = (int) ((Float.parseFloat(reta[0]) / Float.parseFloat(reta[1])) * 100);
                    lessonAttendanceRateResult.setText(reta[0] + "/" + reta[1] + "    " + "%" + aRate);
                    attendanceRate.set(position, reta[0] + "/" + reta[1]);

                    float globalARate = Float.parseFloat(ret[1]) * 3 / 10;

                    if ((Float.parseFloat(reta[1]) - Float.parseFloat(reta[0])) > globalARate) {
                        imageView.setImageResource(R.drawable.ic_close_darkred_24dp);
                    }
                    else {
                        imageView.setImageResource(R.drawable.ic_done_darkgreen_24dp);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            this.lessonNumber.setText(String.valueOf(position + 1));
        }

        private String[] getAttendanceRate(DataSnapshot dataSnapshot){
            int sum = 0;
            int ok = 0;
            for (DataSnapshot ds: dataSnapshot.child("dates").getChildren()) {
                for (DataSnapshot cDs: ds.getChildren()) {
                    for (DataSnapshot ccDs: cDs.getChildren()) {
                        boolean flag = false;
                        if (ccDs.child("done").getValue(Boolean.class) != null)
                            flag = ccDs.child("done").getValue(Boolean.class);
                        if (flag) {
                            sum++;
                            int status = (int) ccDs.child("status").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).getValue(Integer.class);
                            if (status == 1) ok++;
                        }
                    }
                }
            }
            String[] ret = {String.valueOf(ok), String.valueOf(sum)};
            return ret;
        }

        private String[] getLessonStatus(DataSnapshot dataSnapshot){
            int sumLesson = 0;
            int complateLesson = 0;
            for (DataSnapshot ds: dataSnapshot.child("dates").getChildren()) {
                for (DataSnapshot cDs: ds.getChildren()) {
                    for (DataSnapshot ccDs: cDs.getChildren()) {
                        boolean flag = false;
                        if (ccDs.child("done").getValue(Boolean.class) != null)
                            flag = ccDs.child("done").getValue(Boolean.class);
                        if (flag) complateLesson++;
                        sumLesson++;
                    }
                }
            }
            String[] ret = {String.valueOf(complateLesson), String.valueOf(sumLesson)};
            return ret;
        }

        private int getWeekStatus(DataSnapshot dataSnapshot){
            int counter = 0;
            for (DataSnapshot ds: dataSnapshot.child("dates").getChildren()) {
                int doneStatus = 0;
                for (DataSnapshot cDs: ds.getChildren()) {
                    for (DataSnapshot ccDs: cDs.getChildren()) {
                        boolean flag = false;
                        if (ccDs.child("done").getValue(Boolean.class) != null)
                            flag = ccDs.child("done").getValue(Boolean.class);
                        if (flag) doneStatus++;
                    }
                }
                if (doneStatus == 0) return counter;
                counter++;
            }
            return counter;
        }

        private String printTimeVer(String data) {
            String[] dataSplit = data.split("-");
            if (dataSplit[0].length() == 1) dataSplit[0] = "0" + dataSplit[0];
            if (dataSplit[1].length() == 1) dataSplit[1] = "0" + dataSplit[1];
            return dataSplit[0] + ":" + dataSplit[1];
        }
    }
}
