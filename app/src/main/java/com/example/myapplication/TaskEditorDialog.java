package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Binds task data and actions to the shared XML task editor. */
final class TaskEditorDialog {
    private TaskEditorDialog() { }

    static void show(Activity activity, Task task, Runnable onSaved) {
        View form = activity.getLayoutInflater().inflate(R.layout.dialog_task_editor, null);
        EditText title = form.findViewById(R.id.editTaskTitle);
        EditText notes = form.findViewById(R.id.editTaskNotes);
        Spinner priority = form.findViewById(R.id.editTaskPriority);
        TextView dateButton = form.findViewById(R.id.editTaskDate);
        TextView timeButton = form.findViewById(R.id.editTaskTime);
        NumberPicker hours = form.findViewById(R.id.editTaskHours);
        NumberPicker minutesPicker = form.findViewById(R.id.editTaskMinutes);

        title.setText(task.title);
        notes.setText(task.notes == null ? "" : task.notes);
        String[] priorities = {"None", "Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priority.setAdapter(adapter);
        String current = task.priority == null ? "" : task.priority.toUpperCase(Locale.ENGLISH);
        priority.setSelection("LOW".equals(current) ? 1 : "MEDIUM".equals(current) ? 2
                : "HIGH".equals(current) ? 3 : 0);

        Calendar date = Calendar.getInstance();
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(task.date);
            if (parsed != null) date.setTime(parsed);
        } catch (java.text.ParseException ignored) { }
        int[] taskMinutes = {task.minutes};
        dateButton.setText("Date: " + formatDate(date));
        timeButton.setText("Time: " + formatTime(taskMinutes[0]));
        dateButton.setOnClickListener(v -> new DatePickerDialog(activity,
                (picker, year, month, day) -> {
                    date.set(year, month, day);
                    dateButton.setText("Date: " + formatDate(date));
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show());
        timeButton.setOnClickListener(v -> new TimePickerDialog(activity, (picker, hour, minute) -> {
            taskMinutes[0] = hour * 60 + minute;
            timeButton.setText("Time: " + formatTime(taskMinutes[0]));
        }, taskMinutes[0] / 60, taskMinutes[0] % 60, true).show());

        hours.setMinValue(0);
        hours.setMaxValue(72);
        hours.setValue(task.duration / 60);
        String[] minuteValues = {"00", "15", "30", "45"};
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(3);
        minutesPicker.setDisplayedValues(minuteValues);
        minutesPicker.setValue((task.duration % 60) / 15);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Edit task")
                .setView(form)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTitle = title.getText().toString().trim();
            int duration = hours.getValue() * 60 + minutesPicker.getValue() * 15;
            if (newTitle.isEmpty()) {
                title.setError("Enter a task name");
                return;
            }
            if (duration == 0) {
                Toast.makeText(activity, "Duration must be at least 15 minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            task.title = newTitle;
            task.notes = notes.getText().toString().trim();
            task.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime());
            task.minutes = taskMinutes[0];
            task.duration = duration;
            int selected = priority.getSelectedItemPosition();
            task.priority = selected == 0 ? "" : priorities[selected].toUpperCase(Locale.ENGLISH);
            if (!new TaskStore(activity).update(task)) {
                Toast.makeText(activity, "Could not save task changes", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(activity, "Task updated", Toast.LENGTH_SHORT).show();
            onSaved.run();
        }));
        dialog.show();
    }

    private static String formatDate(Calendar date) {
        return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(date.getTime());
    }

    private static String formatTime(int minutes) {
        return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60);
    }
}
