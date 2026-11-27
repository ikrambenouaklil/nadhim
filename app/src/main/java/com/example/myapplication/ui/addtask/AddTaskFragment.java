package com.example.myapplication.ui.addtask;
import com.example.myapplication.R;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.util.UiUtils;
import com.example.myapplication.util.DateUtils;
import com.example.myapplication.data.TaskStore;
import com.example.myapplication.data.Task;
import com.example.myapplication.data.Priority;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class AddTaskFragment extends Fragment {
    private static final int REQUEST_REVIEW = 51;
    private static final String EXTRA_TASK = "review_task";
    private int BLUE;
    private int BLUE_CHIP;
    private int DARK;

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
    private Priority priority = Priority.MEDIUM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_add_task, container, false);
        BLUE = getActivity().getColor(R.color.brand_blue);
        BLUE_CHIP = getActivity().getColor(R.color.brand_blue_pale);
        DARK = getActivity().getColor(R.color.text_primary);
        bindViews();
        setupDeadlinePickers();
        setupPriority();
        setupDurationPicker();
        setupSaveButton();
        return contentView;
    }

    private void bindViews() {
        descriptionInput = contentView.findViewById(R.id.descriptionInput);
        detailsInput = contentView.findViewById(R.id.detailsInput);
        dateText = contentView.findViewById(R.id.deadlineDateField);
        timeText = contentView.findViewById(R.id.deadlineTimeField);
        durationText = contentView.findViewById(R.id.durationText);
        priorityLow = contentView.findViewById(R.id.priorityLow);
        priorityMedium = contentView.findViewById(R.id.priorityMedium);
        priorityHigh = contentView.findViewById(R.id.priorityHigh);
    }

    public void setInitialDate(String dateKey) {
        if (dateKey == null || contentView == null) return;
        try {
            Date date = DateUtils.parseDateKey(dateKey);
            if (date == null) return;
            deadline.setTime(date);
            dateSet = true;
            dateText.setText(DateUtils.formatLong(DateUtils.dateKey(deadline)));
            dateText.setTextColor(DARK);
        } catch (ParseException ignored) { }
    }

    private void setupDeadlinePickers() {
        contentView.findViewById(R.id.deadlineDateField).setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (picker, year, month, day) -> {
                deadline.set(java.util.Calendar.YEAR, year);
                deadline.set(java.util.Calendar.MONTH, month);
                deadline.set(java.util.Calendar.DAY_OF_MONTH, day);
                dateSet = true;
                dateText.setText(DateUtils.formatLong(DateUtils.dateKey(deadline)));
                dateText.setTextColor(DARK);
            }, deadline.get(java.util.Calendar.YEAR), deadline.get(java.util.Calendar.MONTH), deadline.get(java.util.Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        contentView.findViewById(R.id.deadlineTimeField).setOnClickListener(v ->
                new TimePickerDialog(getActivity(), (picker, hour, minute) -> {
                    deadline.set(java.util.Calendar.HOUR_OF_DAY, hour);
                    deadline.set(java.util.Calendar.MINUTE, minute);
                    timeSet = true;
                    timeText.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
                    timeText.setTextColor(DARK);
                }, deadline.get(java.util.Calendar.HOUR_OF_DAY), deadline.get(java.util.Calendar.MINUTE), true).show());
    }

    private void setupPriority() {
        priorityLow.setOnClickListener(v -> selectPriority(Priority.LOW));
        priorityMedium.setOnClickListener(v -> selectPriority(Priority.MEDIUM));
        priorityHigh.setOnClickListener(v -> selectPriority(Priority.HIGH));
        selectPriority(priority);
    }

    private void selectPriority(Priority value) {
        priority = value;
        styleChip(priorityLow, value == Priority.LOW);
        styleChip(priorityMedium, value == Priority.MEDIUM);
        styleChip(priorityHigh, value == Priority.HIGH);
    }

    private void setupDurationPicker() {
        updateDurationText();
        durationText.setOnClickListener(v -> {
            View pickers = getLayoutInflater().inflate(R.layout.dialog_duration_picker, null);
            android.widget.NumberPicker hours = pickers.findViewById(R.id.durationHoursPicker);
            hours.setMinValue(0);
            hours.setMaxValue(72);
            hours.setValue(duration / 60);
            android.widget.NumberPicker minutes = pickers.findViewById(R.id.durationMinutesPicker);
            String[] minuteOptions = {"00", "15", "30", "45"};
            minutes.setMinValue(0);
            minutes.setMaxValue(minuteOptions.length - 1);
            minutes.setDisplayedValues(minuteOptions);
            minutes.setValue((duration % 60) / 15);
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
        chip.setBackgroundResource(selected ? R.drawable.bg_priority_chip_selected
                : R.drawable.bg_priority_chip_unselected);
        chip.setTextColor(selected ? BLUE : DARK);
        chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void setupSaveButton() {
        TextView button = contentView.findViewById(R.id.saveTaskButton);
        button.setBackgroundResource(R.drawable.bg_primary_button);
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
                    DateUtils.dateKey(deadline),
                    deadline.get(java.util.Calendar.HOUR_OF_DAY) * 60 + deadline.get(java.util.Calendar.MINUTE),
                    duration, detailsInput.getText().toString().trim(), "", priority.storageValue(), false);
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
        String today = DateUtils.todayKey();
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
        dateText.setTextColor(getActivity().getColor(R.color.text_hint));
        timeText.setText("Select time");
        timeText.setTextColor(getActivity().getColor(R.color.text_hint));
        duration = 30;
        updateDurationText();
        selectPriority(Priority.MEDIUM);
    }
}
