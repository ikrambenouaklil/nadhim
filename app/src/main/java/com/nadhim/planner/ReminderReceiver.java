package com.nadhim.planner;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_reminders";
    private static final String EXTRA_TASK_ID = "task_id";
    private static final String EXTRA_TASK_TITLE = "task_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra(EXTRA_TASK_ID);
        String title = intent.getStringExtra(EXTRA_TASK_TITLE);
        if (taskId == null || title == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Notification.Builder notification = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        notification.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("A little nudge")
                .setContentText(title)
                .setAutoCancel(true);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(taskId.hashCode(), notification.build());
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Task reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Gentle reminders for your planned tasks");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public static void schedule(Context context, String taskId, String title, long dueAt) {
        Intent intent = reminderIntent(context, taskId, title);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, taskId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long reminderAt = Math.max(System.currentTimeMillis() + 5000L, dueAt - 30L * 60L * 1000L);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pending);
    }

    public static void cancel(Context context, String taskId) {
        Intent intent = reminderIntent(context, taskId, "");
        PendingIntent pending = PendingIntent.getBroadcast(
                context, taskId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(pending);
        pending.cancel();
    }

    private static Intent reminderIntent(Context context, String taskId, String title) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TITLE, title);
        return intent;
    }
}
