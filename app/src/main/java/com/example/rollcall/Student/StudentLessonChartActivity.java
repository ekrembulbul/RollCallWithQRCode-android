package com.example.rollcall.Student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.rollcall.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StudentLessonChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lesson_chart);

        Intent intent = getIntent();
        ArrayList<String> lesCodes = intent.getStringArrayListExtra("lesCodes");
        ArrayList<String> attendance = intent.getStringArrayListExtra("attendance");

        BarChart chart = findViewById(R.id.student_lesson_chart);

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < lesCodes.size(); i++) {
            String[] splitAtt = attendance.get(i).split("/");
            float success = Float.parseFloat(splitAtt[0]);
            float failed = Float.parseFloat(splitAtt[1]) - Float.parseFloat(splitAtt[0]);
            entries.add(new BarEntry(i, new float[]{success, failed}));
        }
        int[] colorClassArray = new int[] {Color.parseColor("#80ff80"), Color.parseColor("#ff8080")};

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setLabelCount(lesCodes.size());
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(lesCodes));

        BarDataSet dataSet = new BarDataSet(entries, "Attendance");
        String[] labelClassArray = new String[] {"+", "-"};
        dataSet.setStackLabels(labelClassArray);
        dataSet.setColors(colorClassArray);

        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }
}
