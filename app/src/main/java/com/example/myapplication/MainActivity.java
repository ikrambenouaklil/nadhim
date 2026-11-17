package com.example.myapplication;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The first screen of the app: "Today".
 *
 * An Activity is one screen. Android creates it and calls onCreate() when the app opens.
 * The look of the screen is in res/layout/activity_main.xml. This class fills in the
 * parts that change (greeting, date, progress) and reacts when the user taps things.
 */
public class MainActivity extends Activity {

    // Sample numbers from the Figma design ("3 of 5 tasks completed").
    // Later these will come from the user's real saved tasks.
    private static final int TASKS_DONE = 3;
    private static final int TASKS_TOTAL = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout XML and show it on screen.
        setContentView(R.layout.activity_main);

        // Fill in each part of the screen. Each step has its own small method
        // below, so onCreate() reads like a table of contents.
        showGreeting();
        showTodayDate();
        showProgress(TASKS_DONE, TASKS_TOTAL);
        setUpTask(R.id.task1_check, R.id.task1_title);
        setUpTask(R.id.task2_check, R.id.task2_title);
        setUpTask(R.id.task3_check, R.id.task3_title);
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

        // findViewById() finds a view from the layout by the android:id we gave it.
        TextView greeting = findViewById(R.id.greeting);
        // getString(id, value) puts the name in place of "%1$s" in strings.xml.
        greeting.setText(getString(greetingText, getString(R.string.user_name)));
    }

    /** Shows today's date, for example "Monday, November 16 · Your day at a glance". */
    private void showTodayDate() {
        // "EEEE" = full day name, "MMMM" = full month name, "d" = day number.
        // Locale.ENGLISH keeps the date in English like the rest of the screen,
        // even if the phone is set to French.
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH);
        String today = format.format(new Date());

        TextView date = findViewById(R.id.date);
        date.setText(getString(R.string.date_subtitle, today));
    }

    /** Updates the blue card: the "x of y tasks completed" text, the percentage and the bar. */
    private void showProgress(int done, int total) {
        // Avoid dividing by zero if there are no tasks.
        int percent = total == 0 ? 0 : (done * 100) / total; // 3 * 100 / 5 = 60

        TextView subtitle = findViewById(R.id.progress_subtitle);
        TextView percentText = findViewById(R.id.progress_percent);
        ProgressBar bar = findViewById(R.id.progress_bar);

        subtitle.setText(getString(R.string.tasks_completed, done, total));
        percentText.setText(getString(R.string.percent, percent));
        bar.setProgress(percent); // the bar's max is 100, so this fills 60%
    }

    /**
     * Makes a task's round tick box work: when it's ticked, the title gets crossed out
     * and turns grey; when it's unticked, the title goes back to normal.
     */
    private void setUpTask(int checkBoxId, int titleId) {
        CheckBox checkBox = findViewById(checkBoxId);
        TextView title = findViewById(titleId);

        // This code runs every time the user taps the tick box.
        checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                // Add the "strike-through" flag to the text's paint flags.
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(getColor(R.color.text_secondary));
            } else {
                // Remove the strike-through flag.
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(getColor(R.color.text_primary));
            }
        });
    }

    /**
     * The bottom bar. We are already on "Today". The other screens don't exist yet,
     * so for now tapping them only shows a short message (a "Toast").
     */
    private void setUpBottomNavigation() {
        showComingSoonOnTap(R.id.nav_calendar, R.string.nav_calendar);
        showComingSoonOnTap(R.id.nav_add, R.string.nav_add);
        showComingSoonOnTap(R.id.nav_insights, R.string.nav_insights);
    }

    private void showComingSoonOnTap(int tabId, int tabName) {
        findViewById(tabId).setOnClickListener(view ->
                Toast.makeText(this,
                        getString(R.string.coming_soon, getString(tabName)),
                        Toast.LENGTH_SHORT).show());
    }
}
