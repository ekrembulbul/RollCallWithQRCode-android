package com.example.rollcall.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.R;
import com.example.rollcall.Teacher.TeacherStatusActivity;

import java.util.ArrayList;

public class TeacherDateAdapter extends RecyclerView.Adapter<TeacherDateAdapter.MyViewHolder> {

    private Context _context;
    private ArrayList<String> _dates;
    private LayoutInflater inflater;
    private String _lesCode;

    public TeacherDateAdapter(Context context, ArrayList<String> dates, String lesCode) {
        inflater = LayoutInflater.from(context);
        _dates = dates;
        _context = context;
        _lesCode = lesCode;
    }

    @NonNull
    @Override
    public TeacherDateAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lesson_item, parent, false);
        TeacherDateAdapter.MyViewHolder holder = new TeacherDateAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherDateAdapter.MyViewHolder holder, int position) {
        holder.setData(_dates.get(position));
    }

    @Override
    public int getItemCount() {
        return _dates.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView _date;

        public MyViewHolder(View itemView) {
            super(itemView);
            _date = itemView.findViewById(R.id.lesson_code_text);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(_context, TeacherStatusActivity.class);
                intent.putExtra("lesCode", _lesCode);
                intent.putExtra("date", _date.getText().toString());
                _context.startActivity(intent);
            });
        }

        public void setData(final String selected) {
            this._date.setText(selected);
        }
    }
}
