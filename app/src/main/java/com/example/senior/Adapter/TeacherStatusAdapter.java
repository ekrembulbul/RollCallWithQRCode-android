package com.example.senior.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.senior.R;
import com.example.senior.Teacher.TeacherStatusActivity;

import java.util.ArrayList;

public class TeacherStatusAdapter extends RecyclerView.Adapter<TeacherStatusAdapter.MyViewHolder> {

    private Context _context;
    private ArrayList<String> _names;
    private LayoutInflater inflater;
    private String _lesCode;

    public TeacherStatusAdapter(Context context, ArrayList<String> names) {
        inflater = LayoutInflater.from(context);
        _names = names;
        _context = context;
    }

    @NonNull
    @Override
    public TeacherStatusAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lesson_item, parent, false);
        TeacherStatusAdapter.MyViewHolder holder = new TeacherStatusAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherStatusAdapter.MyViewHolder holder, int position) {
        holder.setData(_names.get(position));
    }

    @Override
    public int getItemCount() {
        return _names.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView _name;

        public MyViewHolder(View itemView) {
            super(itemView);
            _name = itemView.findViewById(R.id.lesson_code_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        public void setData(final String selected) {
            this._name.setText(selected);
        }
    }
}
