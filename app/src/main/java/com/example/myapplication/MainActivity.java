package com.example.myapplication;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The main screen of the app: "Today".
 */
public class MainActivity extends Activity {

    private static final int BASE_COMPLETED_TASKS = 2;
    private static final int TASKS_TOTAL = 5;

    private CheckBox task1Check;
    private CheckBox task2Check;
    private CheckBox task3Check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout XML and show it on screen.
        setContentView(R.layout.activity_main);

        task1Check = findViewById(R.id.task1_check);
        task2Check = findViewById(R.id.task2_check);
        task3Check = findViewById(R.id.task3_check);

        // Fill in each part of the screen.
        showGreeting();
        showTodayDate();
        setUpTask(task1Check, R.id.task1_title);
        setUpTask(task2Check, R.id.task2_title);
        setUpTask(task3Check, R.id.task3_title);

        // Set initial checked state for task 1 (1 checked + 2 base = 3 of 5 completed initially)
        if (task1Check != null && !task1Check.isChecked()) {
            task1Check.setChecked(true);
        } else {
            updateProgress();
        }

        setUpBottomNavigation();
    }

    /** Shows "Good morning / afternoon / evening, Sara 👋" depending on the current hour. */
    private void showGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // 0 to 23

        int greetingText;
        if (hour < 12) {
            greetingText = R.string.greeting_morning;
        } else if (hour < 18) {
            greetingText = R.string.greeting_afternoon;
        } else {
            greetingText = R.string.greeting_evening;
        }

        String name = WelcomeActivity.getSavedName(this);
        if (name == null) {
            name = "";
        } else {
            name = name.trim();
        }

        TextView greeting = findViewById(R.id.greeting);
        if (greeting != null) {
            greeting.setText(getString(greetingText, name));
        }

        // The profile circle shows the first letter of the name, e.g. "S" for "Sara".
        TextView avatar = findViewById(R.id.avatar);
        if (avatar != null) {
            avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ENGLISH));
        }
    }

    /** Shows today's date, for example "Monday, November 16 · Your day at a glance". */
    private void showTodayDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH);
        String today = format.format(new Date());

        TextView date = findViewById(R.id.date);
        if (date != null) {
            date.setText(getString(R.string.date_subtitle, today));
        }
    }

    /** Dynamically calculates completed tasks and updates progress card. */
    private void updateProgress() {
        int checkedCount = 0;
        if (task1Check != null && task1Check.isChecked()) checkedCount++;
        if (task2Check != null && task2Check.isChecked()) checkedCount++;
        if (task3Check != null && task3Check.isChecked()) checkedCount++;

        int done = BASE_COMPLETED_TASKS + checkedCount;
        showProgress(done, TASKS_TOTAL);
    }

    /** Updates the blue card: the "x of y tasks completed" text, the percentage and the bar. */
    private void showProgress(int done, int total) {
        int percent = total == 0 ? 0 : (done * 100) / total;

        TextView subtitle = findViewById(R.id.progress_subtitle);
        TextView percentText = findViewById(R.id.progress_percent);
        ProgressBar bar = findViewById(R.id.progress_bar);

        if (subtitle != null) {
            subtitle.setText(getString(R.string.tasks_completed, done, total));
        }
        if (percentText != null) {
            percentText.setText(getString(R.string.percent, percent));
        }
        if (bar != null) {
            bar.setProgress(percent);
        }
    }

    /**
     * Makes a task's round tick box work: when it's ticked, the title gets crossed out
     * and turns grey; when it's unticked, the title goes back to normal.
     * Also updates progress in real-time.
     */
    private void setUpTask(CheckBox checkBox, int titleId) {
        if (checkBox == null) return;
        TextView title = findViewById(titleId);

        checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            if (title != null) {
                if (isChecked) {
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setTextColor(getColor(R.color.text_secondary));
                } else {
                    title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setTextColor(getColor(R.color.text_primary));
                }
            }
            updateProgress();
        });
    }

    /**
     * The bottom bar.
     */
    private void setUpBottomNavigation() {
        View navToday = findViewById(R.id.nav_today);
        if (navToday != null) {
            navToday.setOnClickListener(view ->
                    Toast.makeText(this, R.string.already_on_today, Toast.LENGTH_SHORT).show());
        }
        showComingSoonOnTap(R.id.nav_calendar, R.string.nav_calendar);
        showComingSoonOnTap(R.id.nav_add, R.string.nav_add);
        showComingSoonOnTap(R.id.nav_insights, R.string.nav_insights);
    }

    private void showComingSoonOnTap(int tabId, int tabName) {
        View tab = findViewById(tabId);
        if (tab != null) {
            tab.setOnClickListener(view ->
                    Toast.makeText(this,
                            getString(R.string.coming_soon, getString(tabName)),
                            Toast.LENGTH_SHORT).show());
        }
    }
}
