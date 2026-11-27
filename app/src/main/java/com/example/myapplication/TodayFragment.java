package com.example.myapplication;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** The Today tab. */
public class TodayFragment extends Fragment {
    private View contentView;
    private LinearLayout tasksContainer;
    private TextView emptyTasksMessage;
    private List<Task> todaysTasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_main, container, false);
        tasksContainer = findViewById(R.id.todayTasksContainer);
        emptyTasksMessage = findViewById(R.id.todayTasksEmpty);

        showGreeting();
        showTodayDate();
        loadTodaysTasks();
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshIfReady();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) refreshIfReady();
    }

    private void refreshIfReady() {
        if (contentView != null) {
            showGreeting();
            showTodayDate();
            loadTodaysTasks();
        }
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
        greeting.setText(getString(greetingText, name));

        TextView avatar = findViewById(R.id.avatar);
        if (avatar != null) {
            avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ENGLISH));
        }
    }

    private void showTodayDate() {
        String today = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(new Date());
        TextView date = findViewById(R.id.date);
        date.setText(getString(R.string.date_subtitle, today));
    }

    private void loadTodaysTasks() {
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        todaysTasks.clear();

        // Keep only this user's tasks whose date is today.
        for (Task task : new TaskStore(getActivity()).getAll()) {
            if (todayKey.equals(task.date)) todaysTasks.add(task);
        }
        Collections.sort(todaysTasks, (first, second) ->
                Integer.compare(first.minutes, second.minutes));

        tasksContainer.removeAllViews();
        for (Task task : todaysTasks) addTaskRow(task);
        emptyTasksMessage.setVisibility(todaysTasks.isEmpty() ? View.VISIBLE : View.GONE);
        updateProgress();
    }

    private void addTaskRow(Task task) {
        View row = LayoutInflater.from(getActivity())
                .inflate(R.layout.item_task_today, tasksContainer, false);
        CheckBox checkBox = row.findViewById(R.id.todayTaskCheck);
        TextView title = row.findViewById(R.id.todayTaskTitle);
        TextView time = row.findViewById(R.id.todayTaskTime);
        TextView priority = row.findViewById(R.id.todayTaskPriority);

        title.setText(task.title);
        time.setText(formatTime(task.minutes) + " – "
                + formatTime(task.minutes + task.duration) + "  ·  "
                + task.duration + " min");
        showPriority(priority, task.priority);

        checkBox.setContentDescription(task.title);
        checkBox.setChecked(task.done);
        updateTitleStyle(title, task.done);
        checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            task.done = isChecked;
            updateTitleStyle(title, isChecked);
            if (!new TaskStore(getActivity()).update(task)) {
                Toast.makeText(getActivity(), "Could not save task status", Toast.LENGTH_SHORT).show();
            }
            updateProgress();
        });
    }

    private void showPriority(TextView badge, String value) {
        String priority = value == null ? "" : value.trim();
        if (priority.isEmpty()) {
            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);
        badge.setText(priority.toUpperCase(Locale.ENGLISH));
        if ("HIGH".equalsIgnoreCase(priority)) {
            badge.setBackgroundResource(R.drawable.bg_badge_high);
        } else if ("LOW".equalsIgnoreCase(priority)) {
            badge.setBackgroundResource(R.drawable.bg_badge_low);
            badge.setTextColor(getActivity().getColor(R.color.text_primary));
        } else {
            badge.setBackgroundResource(R.drawable.bg_badge_med);
        }
    }

    private void updateTitleStyle(TextView title, boolean done) {
        if (done) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(getActivity().getColor(R.color.text_secondary));
        } else {
            title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(getActivity().getColor(R.color.text_primary));
        }
    }

    private void updateProgress() {
        int completed = 0;
        for (Task task : todaysTasks) {
            if (task.done) completed++;
        }

        int total = todaysTasks.size();
        int percent = total == 0 ? 0 : (completed * 100) / total;
        TextView subtitle = findViewById(R.id.progress_subtitle);
        TextView percentText = findViewById(R.id.progress_percent);
        ProgressBar bar = findViewById(R.id.progress_bar);
        subtitle.setText(getString(R.string.tasks_completed, completed, total));
        percentText.setText(getString(R.string.percent, percent));
        bar.setProgress(percent);
    }

    private String formatTime(int totalMinutes) {
        int minutesInDay = 24 * 60;
        int safeMinutes = ((totalMinutes % minutesInDay) + minutesInDay) % minutesInDay;
        return String.format(Locale.US, "%02d:%02d", safeMinutes / 60, safeMinutes % 60);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
