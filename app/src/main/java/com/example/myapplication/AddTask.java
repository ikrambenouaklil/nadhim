package com.example.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddTask extends Activity {

    // ---- Thème Nadhim ----
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_DARK = Color.rgb(29, 78, 216);
    private static final int BLUE_CHIP = Color.rgb(219, 234, 254);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int BORDER = Color.rgb(226, 232, 240);
    private static final String PREFS = "nadhim_calendar";
    private static final String KEY_EVENTS = "events";

    private EditText descriptionInput;
    private EditText detailsInput;
    private TextView deadlineText;
    private TextView chip30;
    private TextView chip60;
    private TextView chip120;

    private final java.util.Calendar deadline = java.util.Calendar.getInstance();
    private boolean deadlineSet = false;
    private int duration = 30; // minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        bindViews();
        setupDeadline();
        setupDurationChips();
        setupAnalyzeButton();
        setupNavigation();
    }

    private void bindViews() {
        descriptionInput = findViewById(R.id.descriptionInput);
        detailsInput = findViewById(R.id.detailsInput);
        deadlineText = findViewById(R.id.deadlineText);
        chip30 = findViewById(R.id.chip30);
        chip60 = findViewById(R.id.chip60);
        chip120 = findViewById(R.id.chip120);
    }

    // =====================================================================
    // Deadline : date puis heure
    // =====================================================================

    private void setupDeadline() {
        findViewById(R.id.deadlineField).setOnClickListener(v -> pickDate());
    }

    private void pickDate() {
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            deadline.set(year, month, day);
            pickTime();
        }, deadline.get(java.util.Calendar.YEAR),
                deadline.get(java.util.Calendar.MONTH),
                deadline.get(java.util.Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void pickTime() {
        new TimePickerDialog(this, (view, hour, minute) -> {
            deadline.set(java.util.Calendar.HOUR_OF_DAY, hour);
            deadline.set(java.util.Calendar.MINUTE, minute);
            deadlineSet = true;
            deadlineText.setText("▦   " + new SimpleDateFormat("EEE, MMM d · HH:mm", Locale.ENGLISH)
                    .format(deadline.getTime()));
            deadlineText.setTextColor(DARK);
        }, deadline.get(java.util.Calendar.HOUR_OF_DAY),
                deadline.get(java.util.Calendar.MINUTE), true).show();
    }

    // =====================================================================
    // Durée estimée : 30 min / 1 hour / 2 hours
    // =====================================================================

    private void setupDurationChips() {
        chip30.setOnClickListener(v -> selectDuration(30));
        chip60.setOnClickListener(v -> selectDuration(60));
        chip120.setOnClickListener(v -> selectDuration(120));
        selectDuration(30);
    }

    private void selectDuration(int minutes) {
        duration = minutes;
        styleChip(chip30, minutes == 30);
        styleChip(chip60, minutes == 60);
        styleChip(chip120, minutes == 120);
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

    // =====================================================================
    // Bouton « Analyze with Nadhim »
    // =====================================================================

    private void setupAnalyzeButton() {
        TextView button = findViewById(R.id.analyzeButton);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE_DARK, BLUE});
        gradient.setCornerRadius(dp(16));
        button.setBackground(gradient);

        button.setOnClickListener(v -> {
            String title = descriptionInput.getText().toString().trim();
            if (title.isEmpty()) {
                descriptionInput.setError("Describe your task");
                descriptionInput.requestFocus();
                return;
            }
            if (!deadlineSet) {
                Toast.makeText(this, "Select a deadline first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveTask(title, detailsInput.getText().toString().trim())) {
                Toast.makeText(this, "Task added to your calendar", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Calendar.class));
                finish();
            }
        });
    }

    private boolean saveTask(String title, String notes) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        try {
            JSONArray events = new JSONArray(prefs.getString(KEY_EVENTS, "[]"));
            JSONObject task = new JSONObject();
            task.put("id", System.currentTimeMillis());
            task.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(deadline.getTime()));
            task.put("minutes", deadline.get(java.util.Calendar.HOUR_OF_DAY) * 60
                    + deadline.get(java.util.Calendar.MINUTE));
            task.put("duration", duration);
            task.put("title", title);
            task.put("notes", notes);
            events.put(task);
            prefs.edit().putString(KEY_EVENTS, events.toString()).apply();
            return true;
        } catch (JSONException e) {
            Toast.makeText(this, "Could not save the task", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // =====================================================================
    // Navigation du bas (onglet actif : Add)
    // =====================================================================

    private void setupNavigation() {
        TextView addNav = findViewById(R.id.addNavigation);
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(BLUE_CHIP);
        pill.setCornerRadius(dp(20));
        addNav.setBackground(pill);
        addNav.setTextColor(BLUE);
        addNav.setTypeface(null, Typeface.BOLD);

        findViewById(R.id.todayNavigation).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.calendarNavigation).setOnClickListener(v ->
                startActivity(new Intent(this, Calendar.class)));
        findViewById(R.id.insightsNavigation).setOnClickListener(v ->
                startActivity(new Intent(this, Insights.class)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}