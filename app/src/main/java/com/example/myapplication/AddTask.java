package com.example.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddTask extends Fragment {
    private static final int REQUEST_REVIEW = 51;
    private static final String EXTRA_TASK = "review_task";
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_DARK = Color.rgb(29, 78, 216);
    private static final int BLUE_CHIP = Color.rgb(219, 234, 254);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int BORDER = Color.rgb(226, 232, 240);

    private View contentView;
    private EditText descriptionInput;
    private EditText detailsInput;
    private TextView dateText;
    private TextView timeText;
    private TextView durationText;
    private TextView priorityLow;
    private TextView priorityMedium;
    private TextView priorityHigh;
    private final java.util.Calendar deadline = java.util.Calendar.getInstance();
    private boolean dateSet;
    private boolean timeSet;
    private int duration = 30;
    private String priority = "MEDIUM";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_add_task, container, false);
        bindViews();
        setupDeadlinePickers();
        setupPriority();
        setupDurationPicker();
        setupSaveButton();
        return contentView;
    }

    private void bindViews() {
        descriptionInput = findViewById(R.id.descriptionInput);
        detailsInput = findViewById(R.id.detailsInput);
        dateText = findViewById(R.id.deadlineDateField);
        timeText = findViewById(R.id.deadlineTimeField);
        durationText = findViewById(R.id.durationText);
        priorityLow = findViewById(R.id.priorityLow);
        priorityMedium = findViewById(R.id.priorityMedium);
        priorityHigh = findViewById(R.id.priorityHigh);
    }

    private void setupDeadlinePickers() {
        findViewById(R.id.deadlineDateField).setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (picker, year, month, day) -> {
                deadline.set(java.util.Calendar.YEAR, year);
                deadline.set(java.util.Calendar.MONTH, month);
                deadline.set(java.util.Calendar.DAY_OF_MONTH, day);
                dateSet = true;
                dateText.setText(new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(deadline.getTime()));
                dateText.setTextColor(DARK);
            }, deadline.get(java.util.Calendar.YEAR), deadline.get(java.util.Calendar.MONTH), deadline.get(java.util.Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        findViewById(R.id.deadlineTimeField).setOnClickListener(v ->
                new TimePickerDialog(getActivity(), (picker, hour, minute) -> {
                    deadline.set(java.util.Calendar.HOUR_OF_DAY, hour);
                    deadline.set(java.util.Calendar.MINUTE, minute);
                    timeSet = true;
                    timeText.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
                    timeText.setTextColor(DARK);
                }, deadline.get(java.util.Calendar.HOUR_OF_DAY), deadline.get(java.util.Calendar.MINUTE), true).show());
    }

    private void setupPriority() {
        priorityLow.setOnClickListener(v -> selectPriority("LOW"));
        priorityMedium.setOnClickListener(v -> selectPriority("MEDIUM"));
        priorityHigh.setOnClickListener(v -> selectPriority("HIGH"));
        selectPriority(priority);
    }

    private void selectPriority(String value) {
        priority = value;
        styleChip(priorityLow, "LOW".equals(value));
        styleChip(priorityMedium, "MEDIUM".equals(value));
        styleChip(priorityHigh, "HIGH".equals(value));
    }

    private void setupDurationPicker() {
        updateDurationText();
        durationText.setOnClickListener(v -> {
            LinearLayout pickers = new LinearLayout(getActivity());
            pickers.setGravity(Gravity.CENTER);
            NumberPicker hours = new NumberPicker(getActivity());
            hours.setMinValue(0);
            hours.setMaxValue(72);
            hours.setValue(duration / 60);
            NumberPicker minutes = new NumberPicker(getActivity());
            String[] minuteOptions = {"00", "15", "30", "45"};
            minutes.setMinValue(0);
            minutes.setMaxValue(minuteOptions.length - 1);
            minutes.setDisplayedValues(minuteOptions);
            minutes.setValue((duration % 60) / 15);
            pickers.addView(hours);
            TextView hourLabel = new TextView(getActivity());
            hourLabel.setText(" hours   ");
            pickers.addView(hourLabel);
            pickers.addView(minutes);
            TextView minuteLabel = new TextView(getActivity());
            minuteLabel.setText(" minutes");
            pickers.addView(minuteLabel);
            new android.app.AlertDialog.Builder(getActivity())
                    .setTitle("Estimated duration")
                    .setView(pickers)
                    .setPositiveButton("Done", (dialog, which) -> {
                        duration = hours.getValue() * 60 + minutes.getValue() * 15;
                        if (duration == 0) duration = 15;
                        updateDurationText();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateDurationText() {
        int hours = duration / 60;
        int minutes = duration % 60;
        String label = hours == 0 ? "" : hours + (hours == 1 ? " hour" : " hours");
        if (minutes > 0) label += (label.isEmpty() ? "" : " ") + minutes + " min";
        durationText.setText(label.isEmpty() ? "15 min" : label);
        durationText.setTextColor(DARK);
    }

    private void styleChip(TextView chip, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(18));
        bg.setColor(selected ? BLUE_CHIP : Color.WHITE);
        if (!selected) bg.setStroke(dp(1), BORDER);
        chip.setBackground(bg);
        chip.setTextColor(selected ? BLUE : DARK);
        chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void setupSaveButton() {
        TextView button = findViewById(R.id.saveTaskButton);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE_DARK, BLUE});
        gradient.setCornerRadius(dp(16));
        button.setBackground(gradient);
        button.setOnClickListener(v -> {
            String title = descriptionInput.getText().toString().trim();
            if (title.isEmpty()) {
                descriptionInput.setError("Enter a task name");
                descriptionInput.requestFocus();
                return;
            }
            if (!dateSet || !timeSet) {
                Toast.makeText(getActivity(), "Choose both a date and a time", Toast.LENGTH_SHORT).show();
                return;
            }

            Task task = new Task(System.currentTimeMillis(), title,
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(deadline.getTime()),
                    deadline.get(java.util.Calendar.HOUR_OF_DAY) * 60 + deadline.get(java.util.Calendar.MINUTE),
                    duration, detailsInput.getText().toString().trim(), "", priority, false);
            Intent review = new Intent(getActivity(), TaskReviewActivity.class);
            review.putExtra(EXTRA_TASK, task);
            startActivityForResult(review, REQUEST_REVIEW);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_REVIEW || resultCode != Activity.RESULT_OK || data == null) return;

        Task task = (Task) data.getSerializableExtra(EXTRA_TASK);
        if (task == null) return;
        if (!new TaskStore(getActivity()).add(task)) {
            Toast.makeText(getActivity(), "Could not save task. Please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        resetForm();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
        if (today.equals(task.date)) {
            Toast.makeText(getActivity(), "Task added to Today", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).showTodayTab();
        } else {
            Toast.makeText(getActivity(), "Task added to your calendar", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).showCalendarTab(task.date);
        }
    }

    private void resetForm() {
        descriptionInput.setText("");
        detailsInput.setText("");
        deadline.setTimeInMillis(System.currentTimeMillis());
        dateSet = false;
        timeSet = false;
        dateText.setText("Select date");
        dateText.setTextColor(Color.rgb(148, 163, 184));
        timeText.setText("Select time");
        timeText.setTextColor(Color.rgb(148, 163, 184));
        duration = 30;
        updateDurationText();
        selectPriority("MEDIUM");
    }
    private int dp(int value) {
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}