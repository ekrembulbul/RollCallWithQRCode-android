package com.example.rollcall.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.rollcall.LoginActivity;
import com.example.rollcall.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class AddLessonTeacherActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_DOC = 45;

    ProgressBar mProgressBar;
    LinearLayout dynamicContent;
    ArrayList<View> viewList;
    Spinner numberOfLessonDay;
    int dayCount;
    ArrayList<ArrayList<String>> dateList;
    EditText lessonNumberOfWeek;
    ArrayList<ArrayList<String>> daysAndTimes;
    InputStream inputStream;
    TextView attachFile;
    ArrayList<String> studentIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);

        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        attachFile = findViewById(R.id.attach_file_textview);
        studentIds = new ArrayList<>();

        numberOfLessonDay = findViewById(R.id.spinner_number_of_lesson_day);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.number_of_lesson_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberOfLessonDay.setAdapter(adapter);

        dynamicContent = findViewById(R.id.lineerLayout_include);
        viewList = new ArrayList<>();

        numberOfLessonDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateView(position + 1);
                dayCount = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        Button add = findViewById(R.id.add_button);
        add.setOnClickListener(v -> {
            calculateDates();
            addLesson();
        });

        FloatingActionButton attach = findViewById(R.id.fab_attach);
        attach.setOnClickListener(v -> {
            browseDocuments();
        });
    }

    private void readExcel() {
        try {
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);

            studentIds.clear();
            for (int i = 8; i < sheet.getRows(); i++) {
                studentIds.add(sheet.getCell(2, i).getContents());
            }
        }
        catch (BiffException e) {
            Log.d("excelTest", e.getMessage());
        }
        catch (IOException e) {
            Log.d("excelTest", e.getMessage());
        }
    }

    private void browseDocuments(){
        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), REQUEST_CODE_DOC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DOC:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        inputStream = getContentResolver().openInputStream(uri);
                        Log.d("excelTest", getFileName(uri));
                        attachFile.setText(getFileName(uri));
                        readExcel();
                    }
                    catch (FileNotFoundException e) {
                        Log.d("ecxelTest", e.getMessage());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void generateView(int count) {
        viewList.clear();
        dynamicContent.removeAllViews();

        for (int i = 0; i < count; i++) {
            View view = getLayoutInflater().inflate(R.layout.lesson_time_input, dynamicContent, false);
            Spinner spinner = view.findViewById(R.id.spinner_number_of_lesson);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.number_of_lesson_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            dynamicContent.addView(view);
            viewList.add(view);

            FloatingActionButton fabDate = view.findViewById(R.id.fab_edit_lesson_date);
            FloatingActionButton fabStartHour = view.findViewById(R.id.fab_edit_lesson_start_time);

            TextView twDate = view.findViewById(R.id.lesson_date_tw);
            TextView twStartTime = view.findViewById(R.id.lesson_start_time_tw);

            fabDate.setOnClickListener(v -> {
                datePicker(twDate);
            });

            fabStartHour.setOnClickListener(v -> {
                timePicker(twStartTime);
            });
        }

        Log.d("qwer", String.valueOf(viewList.size()));
    }

    private void datePicker(TextView date) {
        final Calendar calendar = Calendar.getInstance();
        int _year = calendar.get(Calendar.YEAR);
        int _month = calendar.get(Calendar.MONTH);
        int _day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            month += 1;
            date.setText(dayOfMonth + "/" + month + "/" + year);
        }, _year, _month, _day);

        dpd.show();
    }

    private void timePicker(TextView time) {
        final Calendar calendar = Calendar.getInstance();
        int _hour = calendar.get(Calendar.HOUR_OF_DAY);
        int _minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            time.setText(hourOfDay + ":" + minute);
        }, _hour, _minute, true);

        tpd.show();
    }

    private void calculateDates() {
        lessonNumberOfWeek = findViewById(R.id.number_of_week_input);
        if (lessonNumberOfWeek.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter number of week of lesson!", Toast.LENGTH_LONG).show();
            return;
        }

        for (View view: viewList) {
            TextView date = view.findViewById(R.id.lesson_date_tw);
            if (date.getText().toString().compareTo("Date") == 0) {
                Toast.makeText(this, "Lessons date cannot be empty!", Toast.LENGTH_LONG).show();
                return;
            }

            TextView time = view.findViewById(R.id.lesson_start_time_tw);
            if (time.getText().toString().compareTo("Start Time") == 0) {
                Toast.makeText(this, "Lessons date cannot be empty!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        dateList = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        daysAndTimes = new ArrayList<>();

        for (int i = 0; i < Integer.parseInt((String) numberOfLessonDay.getSelectedItem()); i++) {
            TextView twDate = viewList.get(i).findViewById(R.id.lesson_date_tw);
            String sDate = twDate.getText().toString();
            String[] date = sDate.split("/");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(date[2]));
            calendar.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            int dayOFWeek = calendar.get(Calendar.DAY_OF_WEEK);

            sDate = date[2] + "-" + date[1] + "-" + date[0];
            dates.add(sDate);

            TextView twStartTime = viewList.get(i).findViewById(R.id.lesson_start_time_tw);
            String sStartTime = twStartTime.getText().toString();
            String[] time = sStartTime.split(":");
            sStartTime = time[0] + "-" + time[1];

            ArrayList<String> dayAndTime = new ArrayList<>();
            dayAndTime.add(String.valueOf(dayOFWeek));
            dayAndTime.add(sStartTime);
            Spinner spinner = viewList.get(i).findViewById(R.id.spinner_number_of_lesson);
            dayAndTime.add(String.valueOf(spinner.getSelectedItem()));
            daysAndTimes.add(dayAndTime);
        }

        dateList.add(dates);

        for (int i = 0; i < Integer.parseInt(lessonNumberOfWeek.getText().toString()) - 1; i++) {
            ArrayList<String> lastDates = dateList.get(dateList.size()-1);
            ArrayList<String> tmpDates = new ArrayList<>();
            for (String sDate: lastDates) {
                String[] date = sDate.split("-");
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                tmpDates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
            }
            dateList.add(tmpDates);
        }

        Log.d("dateListTest", dateList.toString());
        Log.d("daysAndTimes", daysAndTimes.toString());
    }

    private void addLesson() {
        EditText lessonCode = findViewById(R.id.lesson_code_input);
        EditText lessonName = findViewById(R.id.lesson_name_input);

        if (lessonCode.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter lesson code!", Toast.LENGTH_LONG).show();
            return;
        }
        else if (lessonName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter lesson name!", Toast.LENGTH_LONG).show();
            return;
        }

        screenLock();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(AddLessonTeacherActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        String path = "lessons/" + lessonCode.getText().toString() + "/teacher";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.setValue(user.getDisplayName()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        });

        path = "lessons/" + lessonCode.getText().toString() + "/allStudents";
        reference = FirebaseDatabase.getInstance().getReference(path);
        reference.setValue(studentIds).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        });

        path = "lessons/" + lessonCode.getText().toString() + "/name";
        reference = FirebaseDatabase.getInstance().getReference(path);
        reference.setValue(lessonName.getText().toString()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        });

        path = "lessons/" + lessonCode.getText().toString() + "/numberOfWeek";
        reference = FirebaseDatabase.getInstance().getReference(path);
        reference.setValue(lessonNumberOfWeek.getText().toString()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        });

        path = "lessons/" + lessonCode.getText().toString() + "/dates";
        reference = FirebaseDatabase.getInstance().getReference(path);
        for (int i = 0; i < dateList.size(); i++) {
            for (int j = 0; j < daysAndTimes.size(); j++) {
                for (int k = 0; k < Integer.parseInt(daysAndTimes.get(j).get(2)) ; k++) {
                    String time = daysAndTimes.get(j).get(1);
                    String[] splitTime = time.split("-");
                    splitTime[0] = String.valueOf(Integer.parseInt(splitTime[0]) + k);
                    time = splitTime[0] + "-" + splitTime[1];

                    reference.child(String.valueOf(i)).child(dateList.get(i).get(j)).child(time).child("active").setValue(false).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()){
                            Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            screenUnlock();
                            return;
                        }
                    });
                    reference.child(String.valueOf(i)).child(dateList.get(i).get(j)).child(time).child("done").setValue(false).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()){
                            Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            screenUnlock();
                            return;
                        }
                    });
                }
            }
        }

        path = "lessons/" + lessonCode.getText().toString() + "/dayAndTime";
        reference = FirebaseDatabase.getInstance().getReference(path);
        for (int i = 0; i < daysAndTimes.size(); i++) {
            reference.child(String.valueOf(i)).child("day").setValue(daysAndTimes.get(i).get(0)).addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    screenUnlock();
                    return;
                }
            });
            reference.child(String.valueOf(i)).child("time").setValue(daysAndTimes.get(i).get(1)).addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    screenUnlock();
                    return;
                }
            });
            reference.child(String.valueOf(i)).child("numberOfLesson").setValue(daysAndTimes.get(i).get(2)).addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    screenUnlock();
                    return;
                }
            });
        }

        path = "teachers/" + user.getDisplayName() + "/registeredLesson";
        final DatabaseReference regLessonRef = FirebaseDatabase.getInstance().getReference(path);
        regLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> alRegisteredLessons = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    alRegisteredLessons.add(ds.getValue(String.class));
                }

                for(String lesson : alRegisteredLessons) {
                    if (lesson.compareTo(lessonCode.getText().toString()) == 0) {
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson already exist!", Toast.LENGTH_SHORT).show();
                        screenUnlock();
                        return;
                    }
                }

                alRegisteredLessons.add(lessonCode.getText().toString());
                regLessonRef.setValue(alRegisteredLessons).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson added", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        screenUnlock();
                        return;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddLessonTeacherActivity.this, "Lesson could not be added\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                screenUnlock();
                return;
            }
        });
    }

    private void screenLock() {
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void screenUnlock() {
        mProgressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
