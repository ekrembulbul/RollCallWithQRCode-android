package com.example.rollcall.Student.StudentStatus;

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
import com.example.rollcall.Student.StudentLesson.StudentLessonsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentStatusAdapter extends RecyclerView.Adapter<StudentStatusAdapter.MyViewHolder> {

    private Context _context;
    private ArrayList<String> _weeks;
    private LayoutInflater inflater;
    private String _lesCode;
    private String _day;

    public StudentStatusAdapter(Context context, String lesCode, String day) {
        inflater = LayoutInflater.from(context);
        _lesCode = lesCode;
        _day = day;
        _context = context;
        setData();
    }

    private void setData() {
        ((StudentStatusActivity)_context).screenLock();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intentL = new Intent(_context, LoginActivity.class);
            ((StudentStatusActivity)_context).finish();
            _context.startActivity(intentL);
        }
        String path = "students/" + user.getDisplayName() + "/status/" + _lesCode;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> weeks = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    weeks.add("Week " + (Integer.parseInt(ds.getKey()) + 1));
                }

                _weeks = weeks;
                Log.d("rty", _weeks.toString());
                notifyDataSetChanged();
                ((StudentStatusActivity)_context).screenUnlock();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(_context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                ((StudentStatusActivity)_context).screenUnlock();
            }
        });
    }

    @NonNull
    @Override
    public StudentStatusAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.student_lesson_dates_item, parent, false);
        StudentStatusAdapter.MyViewHolder holder = new StudentStatusAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentStatusAdapter.MyViewHolder holder, int position) {
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
            week = itemView.findViewById(R.id.lesson_name_studentLessonDates);
            dynamicLinearDate = itemView.findViewById(R.id.linearLayout_date);
        }

        public void setData(final String selected, int position) {
            week.setText(selected);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Intent intentL = new Intent(_context, LoginActivity.class);
                ((StudentStatusActivity)_context).finish();
                _context.startActivity(intentL);
            }
            String path = "students/" + user.getDisplayName() + "/status/" + _lesCode + "/" + position;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dynamicLinearDate.removeAllViews();
                    String[] daySplit = _day.split("\n");
                    int iDay = 0;

                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        View view = ((Activity)_context).getLayoutInflater().inflate(R.layout.student_lesson_status_date_item, dynamicLinearDate, false);
                        dynamicLinearDate.addView(view);
                        TextView date = view.findViewById(R.id.lesson_date_studentLessonDates);
                        String dateS = ds.getKey();
                        //date.setText(printDateVer(dateS) + "\n" + daySplit[iDay]);
                        date.setText(printDateVer(dateS));

                        LinearLayout dynamicLinearTime = view.findViewById(R.id.linearLayout_time);
                        dynamicLinearTime.removeAllViews();
                        for (DataSnapshot dsC: ds.getChildren()) {
                            View viewC = ((Activity)_context).getLayoutInflater().inflate(R.layout.student_lesson_status_time_item, dynamicLinearTime, false);
                            dynamicLinearTime.addView(viewC);
                            TextView time = viewC.findViewById(R.id.lesson_time_studentLessonDates);

                            String timeS = dsC.getKey();
                            String[] startTimeSplit = timeS.split("-");
                            int iEndTime = Integer.parseInt(startTimeSplit[0]) + 1;
                            if (iEndTime >= 24) iEndTime -= 24;
                            String endTimeS = iEndTime + "-" + startTimeSplit[1];
                            time.setText(printTimeVer(timeS) + "-" + printTimeVer(endTimeS));

                            ImageView imageView = viewC.findViewById(R.id.imageView_lesson_status_dates);
                            int status = dsC.getValue(Integer.class);
                            if (status == 0) {
                                imageView.setImageResource(R.drawable.ic_remove_grey_24dp);
                            }
                            else if (status == -1) {
                                imageView.setImageResource(R.drawable.ic_close_darkred_24dp);
                            }
                            else if (status == 1) {
                                imageView.setImageResource(R.drawable.ic_done_darkgreen_24dp);
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
