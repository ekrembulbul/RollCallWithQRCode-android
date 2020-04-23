package com.example.senior.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.senior.R;
import com.example.senior.Teacher.TeacherDateActivity;

import java.util.ArrayList;

public class MainTeacherAdapter extends RecyclerView.Adapter<MainTeacherAdapter.MyViewHolder> {

    private ArrayList<String> _registeredLessonList;
    private LayoutInflater inflater;
    private Context _context;

    public MainTeacherAdapter(Context context, ArrayList<String> registeredLessonList) {
        inflater = LayoutInflater.from(context);
        _registeredLessonList = registeredLessonList;
        _context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lesson_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(_registeredLessonList.get(position));
    }

    @Override
    public int getItemCount() {
        return _registeredLessonList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView _lessonCode;

        public MyViewHolder(View itemView) {
            super(itemView);
            _lessonCode = itemView.findViewById(R.id.lesson_code_text);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(_context, TeacherDateActivity.class);
                intent.putExtra("lesCode", _lessonCode.getText().toString());
                _context.startActivity(intent);
            });
        }

        public void setData(final String selected) {
            this._lessonCode.setText(selected);
        }
    }
}
