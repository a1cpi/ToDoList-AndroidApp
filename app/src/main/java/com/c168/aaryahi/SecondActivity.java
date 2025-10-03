package com.c168.aaryahi;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {

    private static final String DB_URL =
            "https://mad-assignment-2-97743-default-rtdb.asia-southeast1.firebasedatabase.app";

    private EditText taskNameEditText;
    private EditText dateEditText;
    private EditText timeEditText;
    private SeekBar prioritySeekBar;
    private Button createNewTaskButton;

    private DatabaseReference mDatabase;
    private final Calendar calendar = Calendar.getInstance();
    private String currentTaskId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");

        taskNameEditText = findViewById(R.id.newtask);
        dateEditText = findViewById(R.id.date);
        timeEditText = findViewById(R.id.time);
        prioritySeekBar = findViewById(R.id.seekBar);
        createNewTaskButton = findViewById(R.id.button);

        dateEditText.setOnClickListener(v -> showDatePickerDialog());
        timeEditText.setOnClickListener(v -> showTimePickerDialog());

        // Edit mode
        if (getIntent().hasExtra("TASK_ID")) {
            currentTaskId = getIntent().getStringExtra("TASK_ID");
            ((TextView) findViewById(R.id.newtasks)).setText("Edit Task");
            createNewTaskButton.setText("Update Task");
            taskNameEditText.setText(getIntent().getStringExtra("TASK_NAME"));
            dateEditText.setText(getIntent().getStringExtra("TASK_DATE"));
            timeEditText.setText(getIntent().getStringExtra("TASK_TIME"));
            float priority = getIntent().getFloatExtra("TASK_PRIORITY", 0f);
            prioritySeekBar.setProgress((int) (priority * prioritySeekBar.getMax()));
        } else {
            currentTaskId = null;
        }

        createNewTaskButton.setOnClickListener(v -> saveTask());
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeInView();
        };

        new TimePickerDialog(
                this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void updateDateInView() {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dateEditText.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeInView() {
        String myFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        timeEditText.setText(sdf.format(calendar.getTime()));
    }

    private void saveTask() {
        String name = taskNameEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        float priority = prioritySeekBar.getProgress() / (float) prioritySeekBar.getMax();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty() || "Select Date".contentEquals(date)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.isEmpty() || "Select Time".contentEquals(time)) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = (currentTaskId != null) ? currentTaskId : mDatabase.push().getKey();
        if (taskId == null) {
            Toast.makeText(this, "Failed to create task ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(name, date, time, priority);

        mDatabase.child(taskId).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SecondActivity.this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SecondActivity.this, "Failed to save task", Toast.LENGTH_SHORT).show()
                );
    }
}
