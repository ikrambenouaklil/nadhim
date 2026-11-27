package com.example.myapplication;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskReviewActivity extends Activity {
    static final String EXTRA_TASK = "review_task";
    private Task task ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_review);

        task = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
        if (task == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.reviewTaskTitle)).setText(task.title);
        ((TextView) findViewById(R.id.reviewTaskDate)).setText(formatDate(task.date));
        ((TextView) findViewById(R.id.reviewTaskTime)).setText(formatTime(task.minutes));
        ((TextView) findViewById(R.id.reviewTaskDuration)).setText(formatDuration(task.duration));
        String priority = task.priority == null || task.priority.trim().isEmpty()
                ? "No priority" : task.priority;
        ((TextView) findViewById(R.id.reviewTaskPriority)).setText(priority);
        String notes = task.notes == null || task.notes.trim().isEmpty()
                ? "No additional notes" : task.notes;
        ((TextView) findViewById(R.id.reviewTaskNotes)).setText(notes);

        View accept = findViewById(R.id.acceptSuggestion);
        View keep = findViewById(R.id.keepOriginal);
        View cancel = findViewById(R.id.cancelReview);
        accept.setOnClickListener(v -> returnTask());
        keep.setOnClickListener(v -> returnTask());
        cancel.setOnClickListener(v -> finish());
    }

    private void returnTask() {
        android.content.Intent result = new android.content.Intent();
        result.putExtra(EXTRA_TASK, task);
        setResult(RESULT_OK, result);
        finish();
    }

    private String formatDate(String value) {
        try {
            return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH)
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value));
        } catch (ParseException | NullPointerException ignored) {
            return value;
        }
    }

    private String formatTime(int minutes) {
        return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60);
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (hours == 0) return remaining + " min";
        if (remaining == 0) return hours + (hours == 1 ? " hour" : " hours");
        return hours + " h " + remaining + " min";
    }
}