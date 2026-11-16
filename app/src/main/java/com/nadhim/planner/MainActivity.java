package com.nadhim.planner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String PREFS = "nadhim_data";
    private static final String TASKS_KEY = "tasks";
    private static final int GREEN = Color.rgb(57, 123, 104);
    private static final int INK = Color.rgb(37, 49, 45);
    private static final int MUTED = Color.rgb(112, 124, 118);
    private static final int SURFACE = Color.WHITE;
    private static final int BACKGROUND = Color.rgb(245, 247, 244);

    private SharedPreferences preferences;
    private LinearLayout taskList;
    private TextView summaryText;
    private TextView focusText;
    private TextView progressText;
    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        ReminderReceiver.createChannel(this);
        requestNotificationPermission();
        buildScreen();
        refreshTasks();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(32));
        scroll.addView(page);

        TextView brand = text("N A D H I M", 12, GREEN, true);
        page.addView(brand);
        TextView heading = text("Make room for\nwhat matters.", 30, INK, true);
        heading.setPadding(0, dp(12), 0, dp(6));
        page.addView(heading);
        TextView date = text(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()), 14, MUTED, false);
        page.addView(date);

        LinearLayout summaryCard = card();
        LinearLayout.LayoutParams summaryParams = margins(0, 24, 0, 18);
        page.addView(summaryCard, summaryParams);
        TextView summaryTitle = text("YOUR DAY AT A GLANCE", 11, MUTED, true);
        summaryCard.addView(summaryTitle);
        summaryText = text("", 17, INK, true);
        summaryText.setPadding(0, dp(12), 0, dp(12));
        summaryCard.addView(summaryText);
        progressBar = new LinearLayout(this);
        progressBar.setOrientation(LinearLayout.HORIZONTAL);
        progressBar.setBackgroundColor(Color.rgb(231, 237, 232));
        summaryCard.addView(progressBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(7)));
        progressText = text("", 12, MUTED, false);
        progressText.setPadding(0, dp(9), 0, 0);
        summaryCard.addView(progressText);

        LinearLayout focusCard = card();
        page.addView(focusCard, margins(0, 0, 0, 26));
        TextView focusLabel = text("A GOOD TIME TO FOCUS", 11, GREEN, true);
        focusCard.addView(focusLabel);
        focusText = text("", 16, INK, true);
        focusText.setPadding(0, dp(8), 0, dp(12));
        focusCard.addView(focusText);
        Button availabilityButton = button("Set your available hours");
        availabilityButton.setOnClickListener(v -> chooseAvailability());
        focusCard.addView(availabilityButton);

        LinearLayout section = new LinearLayout(this);
        section.setGravity(Gravity.CENTER_VERTICAL);
        TextView tasksHeading = text("Today's tasks", 21, INK, true);
        section.addView(tasksHeading, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button addButton = button("+ Add task");
        addButton.setOnClickListener(v -> showAddTaskDialog());
        section.addView(addButton);
        page.addView(section, margins(0, 0, 0, 12));

        taskList = new LinearLayout(this);
        taskList.setOrientation(LinearLayout.VERTICAL);
        page.addView(taskList);
        setContentView(scroll);
    }

    private void refreshTasks() {
        JSONArray tasks = readTasks();
        ArrayList<JSONObject> ordered = new ArrayList<>();
        int completed = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) {
                continue;
            }
            ordered.add(task);
            if (task.optBoolean("completed")) {
                completed++;
            }
        }
        Collections.sort(ordered, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject first, JSONObject second) {
                int priorityOrder = Integer.compare(priorityRank(second), priorityRank(first));
                if (priorityOrder != 0) {
                    return priorityOrder;
                }
                return Long.compare(first.optLong("dueAt", Long.MAX_VALUE), second.optLong("dueAt", Long.MAX_VALUE));
            }
        });

        summaryText.setText(ordered.size() == 0
                ? "A fresh start. Add one small task."
                : completed + " of " + ordered.size() + " tasks complete");
        progressText.setText(ordered.size() == 0
                ? "Your progress will show here"
                : Math.round(completed * 100f / ordered.size()) + "% complete · " + (ordered.size() - completed) + " to go");
        progressBar.removeAllViews();
        if (ordered.size() > 0) {
            View fill = new View(this);
            fill.setBackgroundColor(GREEN);
            progressBar.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, completed));
            View remaining = new View(this);
            progressBar.addView(remaining, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, ordered.size() - completed));
        }

        updateFocusSuggestion(tasks);
        taskList.removeAllViews();
        if (ordered.isEmpty()) {
            TextView empty = text("Nothing on your list yet. Add a task to get started.", 14, MUTED, false);
            empty.setPadding(dp(4), dp(20), dp(4), dp(20));
            taskList.addView(empty);
        } else {
            for (JSONObject task : ordered) {
                taskList.addView(createTaskRow(task), margins(0, 0, 0, 10));
            }
        }
    }

    private View createTaskRow(JSONObject task) {
        LinearLayout row = card();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(13), dp(14), dp(13));

        String id = task.optString("id");
        boolean completed = task.optBoolean("completed");
        CheckBox checkBox = new CheckBox(this);
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(GREEN));
        checkBox.setChecked(completed);
        row.addView(checkBox, new LinearLayout.LayoutParams(dp(32), dp(48)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(8), 0, dp(8), 0);
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView title = text(task.optString("title"), 15, INK, true);
        if (completed) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(MUTED);
        }
        details.addView(title);

        String note = task.optString("category", "Personal") + " · " + task.optString("priority", "Medium");
        long dueAt = task.optLong("dueAt", 0);
        if (dueAt > 0) {
            note += " · " + new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(new Date(dueAt));
        }
        TextView subtitle = text(note, 12, MUTED, false);
        subtitle.setPadding(0, dp(5), 0, 0);
        details.addView(subtitle);

        checkBox.setOnCheckedChangeListener((button, checked) -> setTaskCompleted(id, checked));
        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("Delete \"" + task.optString("title") + "\"?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> deleteTask(id))
                    .show();
            return true;
        });
        return row;
    }

    private void showAddTaskDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), 0);

        EditText titleInput = new EditText(this);
        titleInput.setHint("What do you need to do?");
        titleInput.setSingleLine(true);
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        form.addView(titleInput);

        Spinner prioritySpinner = new Spinner(this);
        String[] priorities = {"Automatic priority", "High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);
        form.addView(prioritySpinner, margins(0, 8, 0, 4));

        long[] dueAt = {0};
        Button dueButton = button("Add a reminder");
        dueButton.setOnClickListener(v -> chooseDueDate(dueAt, dueButton));
        form.addView(dueButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New task")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add task", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("Enter a task");
                return;
            }
            String selected = prioritySpinner.getSelectedItem().toString();
            String priority = selected.equals("Automatic priority") ? inferPriority(title, dueAt[0]) : selected;
            addTask(title, categorize(title), priority, dueAt[0]);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void chooseDueDate(long[] dueAt, Button dueButton) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (datePicker, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(Calendar.YEAR, year);
            selected.set(Calendar.MONTH, month);
            selected.set(Calendar.DAY_OF_MONTH, day);
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                selected.set(Calendar.HOUR_OF_DAY, hour);
                selected.set(Calendar.MINUTE, minute);
                selected.set(Calendar.SECOND, 0);
                selected.set(Calendar.MILLISECOND, 0);
                if (selected.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Choose a reminder time in the future.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dueAt[0] = selected.getTimeInMillis();
                dueButton.setText("Reminder: " + new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        .format(new Date(dueAt[0])));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addTask(String title, String category, String priority, long dueAt) {
        JSONArray tasks = readTasks();
        String id = UUID.randomUUID().toString();
        JSONObject task = new JSONObject();
        try {
            task.put("id", id);
            task.put("title", title);
            task.put("category", category);
            task.put("priority", priority);
            task.put("dueAt", dueAt);
            task.put("completed", false);
            task.put("createdAt", System.currentTimeMillis());
            tasks.put(task);
        } catch (JSONException exception) {
            Toast.makeText(this, "Couldn't save this task.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveTasks(tasks);
        if (dueAt > 0) {
            ReminderReceiver.schedule(this, id, title, dueAt);
        }
        refreshTasks();
    }

    private void setTaskCompleted(String id, boolean completed) {
        JSONArray tasks = readTasks();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && task.optString("id").equals(id)) {
                try {
                    task.put("completed", completed);
                    task.put("completedAt", completed ? System.currentTimeMillis() : 0);
                } catch (JSONException exception) {
                    Toast.makeText(this, "Couldn't update this task.", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveTasks(tasks);
                if (completed) {
                    ReminderReceiver.cancel(this, id);
                } else if (task.optLong("dueAt", 0) > 0) {
                    ReminderReceiver.schedule(this, id, task.optString("title"), task.optLong("dueAt"));
                }
                refreshTasks();
                return;
            }
        }
    }

    private void deleteTask(String id) {
        JSONArray tasks = readTasks();
        JSONArray remaining = new JSONArray();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && !task.optString("id").equals(id)) {
                remaining.put(task);
            }
        }
        saveTasks(remaining);
        ReminderReceiver.cancel(this, id);
        refreshTasks();
    }

    private void chooseAvailability() {
        int startHour = preferences.getInt("available_start", 9);
        int endHour = preferences.getInt("available_end", 17);
        new TimePickerDialog(this, (picker, hour, minute) -> {
            int selectedStart = hour;
            new TimePickerDialog(this, (endPicker, end, endMinute) -> {
                if (end <= selectedStart) {
                    Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
                    return;
                }
                preferences.edit()
                        .putInt("available_start", selectedStart)
                        .putInt("available_end", end)
                        .apply();
                updateFocusSuggestion(readTasks());
            }, endHour, 0, false).show();
        }, startHour, 0, false).show();
    }

    private void updateFocusSuggestion(JSONArray tasks) {
        int start = preferences.getInt("available_start", 9);
        int end = preferences.getInt("available_end", 17);
        int routineHourTotal = 0;
        int routineCount = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && task.optBoolean("completed")) {
                long completedAt = task.optLong("completedAt", 0);
                if (completedAt > 0) {
                    Calendar completed = Calendar.getInstance();
                    completed.setTimeInMillis(completedAt);
                    int hour = completed.get(Calendar.HOUR_OF_DAY);
                    if (hour >= start && hour < end) {
                        routineHourTotal += hour;
                        routineCount++;
                    }
                }
            }
        }
        int focusHour = routineCount == 0 ? start + Math.max(0, end - start - 1) / 2
                : routineHourTotal / routineCount;
        String basis = routineCount == 0 ? "based on your available hours" : "based on your completion routine";
        focusText.setText(String.format(Locale.getDefault(), "%s–%s  ·  %s",
                hourLabel(focusHour), hourLabel(Math.min(focusHour + 1, end)), basis));
    }

    private String hourLabel(int hour) {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, 0);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.getTime());
    }

    private JSONArray readTasks() {
        String saved = preferences.getString(TASKS_KEY, "[]");
        try {
            return new JSONArray(saved);
        } catch (JSONException exception) {
            Toast.makeText(this, "Saved tasks couldn't be read.", Toast.LENGTH_LONG).show();
            return new JSONArray();
        }
    }

    private void saveTasks(JSONArray tasks) {
        preferences.edit().putString(TASKS_KEY, tasks.toString()).apply();
    }

    private String categorize(String title) {
        String value = title.toLowerCase(Locale.ROOT);
        if (containsAny(value, "study", "read", "class", "homework", "exam", "learn")) {
            return "Study";
        }
        if (containsAny(value, "work", "meeting", "email", "report", "client", "project")) {
            return "Work";
        }
        if (containsAny(value, "run", "walk", "gym", "doctor", "health", "exercise", "meditate")) {
            return "Health";
        }
        return "Personal";
    }

    private String inferPriority(String title, long dueAt) {
        String value = title.toLowerCase(Locale.ROOT);
        if (containsAny(value, "urgent", "asap", "today", "deadline")) {
            return "High";
        }
        if (dueAt > 0 && dueAt - System.currentTimeMillis() < 24L * 60L * 60L * 1000L) {
            return "High";
        }
        return "Medium";
    }

    private int priorityRank(JSONObject task) {
        if (task.optBoolean("completed")) {
            return 0;
        }
        String priority = task.optString("priority", "Medium");
        if ("High".equals(priority)) {
            return 3;
        }
        if ("Low".equals(priority)) {
            return 1;
        }
        return 2;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(17), dp(18), dp(17));
        layout.setBackgroundColor(SURFACE);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(GREEN);
        button.setAllCaps(false);
        button.setTextSize(13);
        return button;
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
