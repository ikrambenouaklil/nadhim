package com.example.myapplication.ui.dialog;
import com.example.myapplication.R;
import com.example.myapplication.data.TaskStore;
import com.example.myapplication.data.Task;
import com.example.myapplication.data.Priority;
import com.example.myapplication.util.DateUtils;

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
public final class TaskEditorDialog {
    private TaskEditorDialog() { }

    public static void show(Activity activity, Task task, Runnable onSaved) {
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
        Priority current = Priority.fromString(task.priority);
        priority.setSelection(current == Priority.LOW ? 1 : current == Priority.MEDIUM ? 2
                : current == Priority.HIGH ? 3 : 0);

        Calendar date = Calendar.getInstance();
        try {
            Date parsed = DateUtils.parseDateKey(task.date);
            if (parsed != null) date.setTime(parsed);
        } catch (java.text.ParseException ignored) { }
        int[] taskMinutes = {task.minutes};
        dateButton.setText("Date: " + DateUtils.formatLong(DateUtils.dateKey(date)));
        timeButton.setText("Time: " + DateUtils.formatTime(taskMinutes[0]));
        dateButton.setOnClickListener(v -> new DatePickerDialog(activity,
                (picker, year, month, day) -> {
                    date.set(year, month, day);
                    dateButton.setText("Date: " + DateUtils.formatLong(DateUtils.dateKey(date)));
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show());
        timeButton.setOnClickListener(v -> new TimePickerDialog(activity, (picker, hour, minute) -> {
            taskMinutes[0] = hour * 60 + minute;
            timeButton.setText("Time: " + DateUtils.formatTime(taskMinutes[0]));
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
            task.date = DateUtils.dateKey(date);
            task.minutes = taskMinutes[0];
            task.duration = duration;
            int selected = priority.getSelectedItemPosition();
            task.priority = Priority.fromString(selected == 0 ? null : priorities[selected]).storageValue();
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

}
