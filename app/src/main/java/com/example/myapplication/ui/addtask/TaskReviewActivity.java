package com.example.myapplication.ui.addtask;
import com.example.myapplication.R;
import com.example.myapplication.data.Task;
import com.example.myapplication.util.DateUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


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
        ((TextView) findViewById(R.id.reviewTaskDate)).setText(DateUtils.formatLong(task.date));
        ((TextView) findViewById(R.id.reviewTaskTime)).setText(DateUtils.formatTime(task.minutes));
        ((TextView) findViewById(R.id.reviewTaskDuration)).setText(DateUtils.formatDuration(task.duration));
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

}
