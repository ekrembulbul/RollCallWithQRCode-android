package com.example.rollcall.Teacher.TeacherDate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.example.rollcall.Student.StudentStatus.StudentStatusActivity;
import com.example.rollcall.Teacher.TeacherStatus.TeacherStatusActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeacherDateAdapter extends RecyclerView.Adapter<TeacherDateAdapter.MyViewHolder> {

    private Context _context;
    private ArrayList<String> _weeks;
    private LayoutInflater inflater;
    private String _lesCode;
    private String _day;

    public TeacherDateAdapter(Context context, String lesCode, String day) {
        inflater = LayoutInflater.from(context);
        _context = context;
        _day = day;
        _lesCode = lesCode;
        setData();
    }

    private void setData() {
        ((TeacherDateActivity)_context).screenLock();

        String path = "lessons/" + _lesCode + "/dates";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> week = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    week.add("Week " + (Integer.parseInt(ds.getKey()) + 1));
                }

                _weeks = week;
                notifyDataSetChanged();
                ((TeacherDateActivity)_context).screenUnlock();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                ((TeacherDateActivity)_context).screenUnlock();
            }
        });
    }

    @NonNull
    @Override
    public TeacherDateAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.teacher_lesson_dates_item, parent, false);
        TeacherDateAdapter.MyViewHolder holder = new TeacherDateAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherDateAdapter.MyViewHolder holder, int position) {
        if (_weeks != null) holder.setData(_weeks.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (_weeks != null) return _weeks.size();
        else return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView week;
        LinearLayout dynamicLinearDate;

        public MyViewHolder(View itemView) {
            super(itemView);
            week = itemView.findViewById(R.id.lesson_name_teacherLessonDates);
            dynamicLinearDate = itemView.findViewById(R.id.linearLayout_date_teacher);
        }

        public void setData(final String selected, int position) {
            week.setText(selected);

            String path = "lessons/" + _lesCode + "/dates/" + position;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dynamicLinearDate.removeAllViews();
                    String[] daySplit = _day.split("\n");
                    int iDay = 0;

                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        View view = ((Activity)_context).getLayoutInflater().inflate(R.layout.teacher_lesson_status_date_item, dynamicLinearDate, false);
                        dynamicLinearDate.addView(view);
                        TextView date = view.findViewById(R.id.lesson_date_teacherLessonDates);
                        String dateS = ds.getKey();
                        //date.setText(printDateVer(dateS) + "\n" + daySplit[iDay]);
                        date.setText(printDateVer(dateS));

                        LinearLayout dynamicLinearTime = view.findViewById(R.id.linearLayout_time_teacher);
                        dynamicLinearTime.removeAllViews();
                        for (DataSnapshot dsC: ds.getChildren()) {
                            View viewC = ((Activity)_context).getLayoutInflater().inflate(R.layout.teacher_lesson_status_time_item, dynamicLinearTime, false);
                            dynamicLinearTime.addView(viewC);
                            TextView time = viewC.findViewById(R.id.lesson_time_teacherLessonDates);

                            String timeS = dsC.getKey();
                            String[] startTimeSplit = timeS.split("-");
                            int iEndTime = Integer.parseInt(startTimeSplit[0]) + 1;
                            if (iEndTime >= 24) iEndTime -= 24;
                            String endTimeS = iEndTime + "-" + startTimeSplit[1];
                            time.setText(printTimeVer(timeS) + "-" + printTimeVer(endTimeS));

                            TextView timeAttendance = viewC.findViewById(R.id.lesson_attendance_time_teacherLessonDates);

                            FloatingActionButton fab = viewC.findViewById(R.id.fab_lesson_status_dates_teacher);
                            fab.setOnClickListener(v -> {
                                Intent intent = new Intent(_context, TeacherStatusActivity.class);
                                intent.putExtra("lesCode", _lesCode);
                                intent.putExtra("week", dataSnapshot.getKey());
                                intent.putExtra("date", ds.getKey());
                                intent.putExtra("time", dsC.getKey());
                                _context.startActivity(intent);
                            });

                            String[] result = getAttendance(dsC);
                            if (result[0].equals("0") && result[1].equals("0")) {
                                timeAttendance.setText("-");
                                fab.setVisibility(View.INVISIBLE);
                            }
                            else {
                                timeAttendance.setText(result[0] + "/" + result[1]);
                            }
                        }
                        iDay++;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        private String[] getAttendance(DataSnapshot dataSnapshot) {
            boolean done = false;
            if (dataSnapshot.child("done").getValue(Boolean.class) != null)
                done = dataSnapshot.child("done").getValue(Boolean.class);

            if (done) {
                int sum = 0;
                int ok = 0;
                for (DataSnapshot ds: dataSnapshot.child("status").getChildren()) {
                    sum++;
                    if (ds.getValue(Integer.class) == 1) ok++;
                }
                String[] ret = {String.valueOf(ok), String.valueOf(sum)};
                return ret;
            }
            else {
                String[] ret = {"0", "0"};
                return ret;
            }
        }

        private String printDateVer(String data) {
            String[] dataSplit = data.split("-");
            if (dataSplit[1].length() == 1) dataSplit[1] = "0" + dataSplit[1];
            if (dataSplit[2].length() == 1) dataSplit[2] = "0" + dataSplit[2];
            return dataSplit[2] + "/" + dataSplit[1] + "/" + dataSplit[0];
        }

        private String printTimeVer(String data) {
            String[] dataSplit = data.split("-");
            if (dataSplit[0].length() == 1) dataSplit[0] = "0" + dataSplit[0];
            if (dataSplit[1].length() == 1) dataSplit[1] = "0" + dataSplit[1];
            return dataSplit[0] + ":" + dataSplit[1];
        }
    }
}
