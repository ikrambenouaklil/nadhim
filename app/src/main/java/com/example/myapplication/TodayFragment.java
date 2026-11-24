package com.example.myapplication;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** The Today tab. */
public class
TodayFragment extends Fragment {

    private static final int BASE_COMPLETED_TASKS = 2;
    private static final int TASKS_TOTAL = 5;

    private View contentView;
    private CheckBox task1Check;
    private CheckBox task2Check;
    private CheckBox task3Check;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_main, container, false);
       

        task1Check = findViewById(R.id.task1_check);
        task2Check = findViewById(R.id.task2_check);
        task3Check = findViewById(R.id.task3_check);

        showGreeting();
        showTodayDate();
        setUpTask(task1Check, R.id.task1_title);
        setUpTask(task2Check, R.id.task2_title);
        setUpTask(task3Check, R.id.task3_title);

        if (task1Check != null && !task1Check.isChecked()) {
            task1Check.setChecked(true);
        } else {
            updateProgress();
        }
        return contentView;
    }

    private void showGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingText;
        if (hour < 12) {
            greetingText = R.string.greeting_morning;
        } else if (hour < 18) {
            greetingText = R.string.greeting_afternoon;
        } else {
            greetingText = R.string.greeting_evening;
        }

        String name = WelcomeActivity.getSavedName(getActivity());
        name = name == null ? "" : name.trim();

        TextView greeting = findViewById(R.id.greeting);
        if (greeting != null) {
            greeting.setText(getString(greetingText, name));
        }

        TextView avatar = findViewById(R.id.avatar);
        if (avatar != null) {
            avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ENGLISH));
        }
    }

    private void showTodayDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH);
        String today = format.format(new Date());
        TextView date = findViewById(R.id.date);
        if (date != null) {
            date.setText(getString(R.string.date_subtitle, today));
        }
    }

    private void updateProgress() {
        int checkedCount = 0;
        if (task1Check != null && task1Check.isChecked()) checkedCount++;
        if (task2Check != null && task2Check.isChecked()) checkedCount++;
        if (task3Check != null && task3Check.isChecked()) checkedCount++;
        showProgress(BASE_COMPLETED_TASKS + checkedCount, TASKS_TOTAL);
    }

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

    private void setUpTask(CheckBox checkBox, int titleId) {
        if (checkBox == null) return;
        TextView title = findViewById(titleId);
        checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            if (title != null) {
                if (isChecked) {
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setTextColor(getActivity().getColor(R.color.text_secondary));
                } else {
                    title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setTextColor(getActivity().getColor(R.color.text_primary));
                }
            }
            updateProgress();
        });
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
