package com.example.rollcall.Teacher.TeacherStatus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.R;
import com.example.rollcall.Teacher.TeacherLessons.TeacherLessonsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeacherStatusAdapter extends RecyclerView.Adapter<TeacherStatusAdapter.MyViewHolder> {

    private ArrayList<String> _idsStatusNames;
    private String lesCode;
    private String week;
    private String date;
    private String time;
    private LayoutInflater inflater;
    private Context context;

    public TeacherStatusAdapter(Context context, String lesCode, String week, String date, String time) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.lesCode = lesCode;
        this.week = week;
        this.date = date;
        this.time = time;
        setData();
    }

    private void setData() {
        ((TeacherStatusActivity)context).screenLock();

        String path = "lessons/" + lesCode + "/dates/" + week + "/" + date + "/" + time + "/status";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> idsStatusNames = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    idsStatusNames.add(ds.getKey() + "," + ds.getValue(Integer.class));
                }
                _idsStatusNames = idsStatusNames;

                String path = "users/students";
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            for (int i = 0; i < _idsStatusNames.size(); i++) {
                                String[] split = _idsStatusNames.get(i).split(",");
                                String sId = ds.child("sId").getValue(String.class);
                                if (sId.equals(split[0])) {
                                    _idsStatusNames.remove(i);
                                    String name = ds.child("name").getValue(String.class);
                                    String surname = ds.child("surname").getValue(String.class);
                                    _idsStatusNames.add(i, split[0] + "," + split[1] + "," + name + " " + surname);
                                }
                            }
                        }

                        notifyDataSetChanged();
                        ((TeacherStatusActivity)context).screenUnlock();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        ((TeacherStatusActivity)context).screenUnlock();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                ((TeacherStatusActivity)context).screenUnlock();
            }
        });
    }

    @NonNull
    @Override
    public TeacherStatusAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.teacher_lesson_status_item, parent, false);
        TeacherStatusAdapter.MyViewHolder holder = new TeacherStatusAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherStatusAdapter.MyViewHolder holder, int position) {
        if (_idsStatusNames != null) {
            String[] split = _idsStatusNames.get(0).split(",");
            if (split.length > 2) holder.setData(_idsStatusNames.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        if (_idsStatusNames != null) {
            String[] split = _idsStatusNames.get(0).split(",");
            if (split.length > 2) return _idsStatusNames.size();
            else return 0;
        }
        else return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView id;
        ImageView status;
        TextView number;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.student_name_teacherStatus);
            id = itemView.findViewById(R.id.student_id_teacherStatus);
            status = itemView.findViewById(R.id.imageView_student_status_teacherStatus);
            number = itemView.findViewById(R.id.student_number_teacherStatus);
        }

        public void setData(final String selected, int position) {
            number.setText(String.valueOf(position + 1));
            Log.d("statusT", _idsStatusNames.toString());
            String[] split = selected.split(",");
            id.setText(split[0]);
            name.setText(split[2]);

            if (Integer.parseInt(split[1]) == 0) {
                status.setImageResource(R.drawable.ic_remove_grey_24dp);
            }
            else if (Integer.parseInt(split[1]) == 1) {
                status.setImageResource(R.drawable.ic_done_darkgreen_24dp);
            }
            else if (Integer.parseInt(split[1]) == -1) {
                status.setImageResource(R.drawable.ic_close_darkred_24dp);
            }
        }
    }
}
