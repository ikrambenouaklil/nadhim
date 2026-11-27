package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Shared edit form used by task actions on Today. */
final class TodayTaskEditor {
    private static final int BLUE = Color.rgb(37, 99, 235);

    private TodayTaskEditor() { }

    static void show(Activity activity, Task task, Runnable onSaved) {
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 20);
        form.setPadding(pad, dp(activity, 8), pad, 0);

        form.addView(label(activity, "Task name"));
        EditText titleInput = new EditText(activity);
        titleInput.setSingleLine(true);
        titleInput.setText(task.title);
        form.addView(titleInput);

        form.addView(label(activity, "Notes"));
        EditText notesInput = new EditText(activity);
        notesInput.setMinLines(2);
        notesInput.setGravity(Gravity.TOP | Gravity.START);
        notesInput.setText(task.notes == null ? "" : task.notes);
        form.addView(notesInput);

        form.addView(label(activity, "Priority"));
        String[] priorities = {"None", "Low", "Medium", "High"};
        Spinner priorityPicker = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priorityPicker.setAdapter(adapter);
        String current = task.priority == null ? "" : task.priority.toUpperCase(Locale.ENGLISH);
        priorityPicker.setSelection("LOW".equals(current) ? 1 : "MEDIUM".equals(current) ? 2
                : "HIGH".equals(current) ? 3 : 0);
        form.addView(priorityPicker);

        Calendar date = Calendar.getInstance();
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(task.date);
            if (parsed != null) date.setTime(parsed);
        } catch (java.text.ParseException ignored) { }
        int[] minutes = {task.minutes};

        form.addView(label(activity, "Deadline date"));
        TextView dateButton = pickerButton(activity, "Date: " + formatDate(date));
        dateButton.setOnClickListener(v -> new DatePickerDialog(activity, (picker, year, month, day) -> {
            date.set(year, month, day);
            dateButton.setText("Date: " + formatDate(date));
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show());
        form.addView(dateButton);

        form.addView(label(activity, "Deadline time"));
        TextView timeButton = pickerButton(activity, "Time: " + formatTime(minutes[0]));
        timeButton.setOnClickListener(v -> new TimePickerDialog(activity, (picker, hour, minute) -> {
            minutes[0] = hour * 60 + minute;
            timeButton.setText("Time: " + formatTime(minutes[0]));
        }, minutes[0] / 60, minutes[0] % 60, true).show());
        form.addView(timeButton);

        form.addView(label(activity, "Estimated duration"));
        LinearLayout durationRow = new LinearLayout(activity);
        durationRow.setGravity(Gravity.CENTER);
        NumberPicker hours = new NumberPicker(activity);
        hours.setMinValue(0);
        hours.setMaxValue(72);
        hours.setValue(task.duration / 60);
        NumberPicker durationMinutes = new NumberPicker(activity);
        String[] minuteOptions = {"00", "15", "30", "45"};
        durationMinutes.setMinValue(0);
        durationMinutes.setMaxValue(3);
        durationMinutes.setDisplayedValues(minuteOptions);
        durationMinutes.setValue((task.duration % 60) / 15);
        durationRow.addView(hours);
        durationRow.addView(durationMinutes);
        form.addView(durationRow);

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Edit task")
                .setView(scroll)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            int duration = hours.getValue() * 60 + durationMinutes.getValue() * 15;
            if (title.isEmpty()) {
                titleInput.setError("Enter a task name");
                return;
            }
            if (duration == 0) {
                Toast.makeText(activity, "Duration must be at least 15 minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            task.title = title;
            task.notes = notesInput.getText().toString().trim();
            task.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime());
            task.minutes = minutes[0];
            task.duration = duration;
            int selected = priorityPicker.getSelectedItemPosition();
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

    private static TextView label(Activity activity, String text) {
        TextView label = new TextView(activity);
        label.setText(text);
        label.setTextColor(Color.rgb(51, 65, 85));
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, dp(activity, 12), 0, dp(activity, 2));
        return label;
    }

    private static TextView pickerButton(Activity activity, String text) {
        TextView button = new TextView(activity);
        button.setText(text);
        button.setTextColor(Color.rgb(51, 65, 85));
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(activity, 12), 0, dp(activity, 12), 0);
        button.setBackground(roundBackground(activity, Color.WHITE, Color.rgb(37, 99, 235)));
        button.setMinHeight(dp(activity, 44));
        return button;
    }

    private static GradientDrawable roundBackground(Activity activity, int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(activity, 8));
        drawable.setStroke(dp(activity, 1), stroke);
        return drawable;
    }

    private static String formatDate(Calendar date) {
        return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(date.getTime());
    }

    private static String formatTime(int minutes) {
        return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60);
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
