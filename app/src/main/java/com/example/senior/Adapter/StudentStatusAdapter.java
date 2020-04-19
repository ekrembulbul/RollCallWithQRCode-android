package com.example.senior.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.senior.R;

import java.util.ArrayList;

public class StudentStatusAdapter extends RecyclerView.Adapter<StudentStatusAdapter.MyViewHolder> {

    private ArrayList<String> _dates;
    private LayoutInflater inflater;

    public StudentStatusAdapter(Context context, ArrayList<String> dates) {
        inflater = LayoutInflater.from(context);
        _dates = dates;
    }

    @NonNull
    @Override
    public StudentStatusAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lesson_item, parent, false);
        StudentStatusAdapter.MyViewHolder holder = new StudentStatusAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentStatusAdapter.MyViewHolder holder, int position) {
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
        }

        public void setData(final String selected) {
            this._date.setText(selected);
        }
    }
}
