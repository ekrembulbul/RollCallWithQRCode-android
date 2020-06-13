package com.example.rollcall.Teacher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import com.example.rollcall.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class TeacherEachLessonChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_each_lesson_chart);

        Intent intent = getIntent();
        ArrayList<String> xLabel = intent.getStringArrayListExtra("xLabel");
        ArrayList<String> attendance = intent.getStringArrayListExtra("attendance");

        BarChart chart = findViewById(R.id.teacher_each_lesson_chart);

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < xLabel.size(); i++) {
            if (attendance.get(i).compareTo("-") != 0) {
                String[] splitXLabel = xLabel.get(i).split("_");
                xLabel.set(i, splitXLabel[0] + " " + splitXLabel[1] + " " + splitXLabel[2]);

                String[] splitAtt = attendance.get(i).split("/");
                float success = Float.parseFloat(splitAtt[0]);
                float failed = Float.parseFloat(splitAtt[1]) - Float.parseFloat(splitAtt[0]);
                entries.add(new BarEntry(i, new float[]{success, failed}));
            }
        }
        int[] colorClassArray = new int[] {Color.parseColor("#80ff80"), Color.parseColor("#ff8080")};

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-90);
        xAxis.setLabelCount(xLabel.size());
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

        BarDataSet dataSet = new BarDataSet(entries, "Attendance");
        String[] labelClassArray = new String[] {"+", "-"};
        dataSet.setStackLabels(labelClassArray);
        dataSet.setColors(colorClassArray);

        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }
}
