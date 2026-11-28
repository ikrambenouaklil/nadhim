package com.example.myapplication.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.data.Task;
import com.example.myapplication.data.TaskStore;
import com.example.myapplication.ui.NadhimActivity;
import com.example.myapplication.util.DateUtils;
import com.example.myapplication.util.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Calendar;

/** Shows task deadline reminders and a wellbeing reminder. */
public class NotificationActivity extends NadhimActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.notificationsBackButton).setOnClickListener(view -> finish());
        populateNotifications();
        bindRestReminder();
    }

    private void populateNotifications() {
        LinearLayout list = findViewById(R.id.notificationDeadlineList);
        String today = DateUtils.todayKey();
        Calendar lastReminderDate = Calendar.getInstance();
        lastReminderDate.add(Calendar.DAY_OF_YEAR, 7);
        String lastReminderDateKey = DateUtils.dateKey(lastReminderDate);

        List<Task> reminders = new ArrayList<>();
        for (Task task : new TaskStore(this).getAll()) {
            if (task.done || task.date == null || task.date.trim().isEmpty()) continue;
            if (task.date.compareTo(today) < 0 || task.date.compareTo(lastReminderDateKey) <= 0) {
                reminders.add(task);
            }
        }
        Collections.sort(reminders, Comparator
                .comparing((Task task) -> task.date)
                .thenComparingInt(task -> task.minutes));

        if (reminders.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.notifications_no_deadlines);
            empty.setTextColor(getColor(R.color.text_secondary));
            empty.setTextSize(13);
            empty.setPadding(4, 8, 4, 8);
            list.addView(empty);
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        for (Task task : reminders) {
            View card = inflater.inflate(R.layout.item_notification, list, false);
            TextView category = card.findViewById(R.id.notificationCategory);
            TextView title = card.findViewById(R.id.notificationTitle);
            TextView detail = card.findViewById(R.id.notificationDetail);

            category.setText(R.string.notification_deadline_label);
            title.setText(task.title == null || task.title.trim().isEmpty()
                    ? getString(R.string.notification_untitled_task) : task.title);
            detail.setText(formatDeadline(task, today));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = UiUtils.dp(this, 10);
            list.addView(card, params);
        }
    }

    private String formatDeadline(Task task, String today) {
        String time = DateUtils.formatTime(task.minutes);
        if (task.date.compareTo(today) < 0) {
            return getString(R.string.notification_overdue, DateUtils.formatLong(task.date), time);
        }
        if (task.date.equals(today)) {
            return getString(R.string.notification_due_today, time);
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (task.date.equals(DateUtils.dateKey(tomorrow))) {
            return getString(R.string.notification_due_tomorrow, time);
        }
        return getString(R.string.notification_due_date, DateUtils.formatLong(task.date), time);
    }

    private void bindRestReminder() {
        View reminder = findViewById(R.id.restReminder);
        ((TextView) reminder.findViewById(R.id.notificationCategory))
                .setText(R.string.notification_rest_label);
        ((TextView) reminder.findViewById(R.id.notificationTitle))
                .setText(R.string.notification_rest_title);
        ((TextView) reminder.findViewById(R.id.notificationDetail))
                .setText(R.string.notification_rest_body);
    }
}
