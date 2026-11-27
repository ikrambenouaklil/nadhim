package com.example.myapplication;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Insights extends Fragment {
    private int BLUE;
    private int BLUE_MID;
    private int GREEN;
    private int DARK;
    private int MUTED;
    private int TRACK;
    private int[] priorityColors;
    private static final String[] PRIORITIES = {"HIGH", "MEDIUM", "LOW", "UNASSIGNED"};
    private static final int CHART_BAR_MAX_DP = 92;

    private LinearLayout weeklyBars;
    private LinearLayout categoriesContainer;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_insights, container, false);
        loadInsightColors();
        weeklyBars = findViewById(R.id.weeklyBars);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        refreshInsights();
        return contentView;
    }

    private void loadInsightColors() {
        BLUE = getActivity().getColor(R.color.insight_blue);
        BLUE_MID = getActivity().getColor(R.color.insight_blue_mid);
        GREEN = getActivity().getColor(R.color.insight_green);
        DARK = getActivity().getColor(R.color.insight_dark);
        MUTED = getActivity().getColor(R.color.insight_muted);
        TRACK = getActivity().getColor(R.color.insight_track);
        priorityColors = new int[]{BLUE, BLUE_MID, GREEN, MUTED};
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
        if (contentView != null) refreshInsights();
    }

    private void refreshInsights() {
        List<Task> tasks = new TaskStore(getActivity()).getAll();
        Calendar start = weekStart();
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        renderStats(tasks, start, end);
        renderWeeklyChart(tasks, start);
        renderPriorityMix(tasks, start, end);
        renderFocusCard(tasks, start, end);
    }

    private void renderStats(List<Task> tasks, Calendar start, Calendar end) {
        int completedThisWeek = 0;
        int completedMinutes = 0;
        int dueSoFar = 0;
        int completedDueSoFar = 0;
        String todayKey = dateKey(Calendar.getInstance());
        for (Task task : tasks) {
            if (!inWeek(task, start, end)) continue;
            if (task.done) {
                completedThisWeek++;
                completedMinutes += Math.max(0, task.duration);
            }
            if (task.date.compareTo(todayKey) <= 0) {
                dueSoFar++;
                if (task.done) completedDueSoFar++;
            }
        }

        int rate = dueSoFar == 0 ? 0 : completedDueSoFar * 100 / dueSoFar;
        ((TextView) findViewById(R.id.statTasksValue)).setText(String.valueOf(completedThisWeek));
        ((TextView) findViewById(R.id.statOnTimeValue)).setText(rate + "%");
        ((TextView) findViewById(R.id.statFocusValue)).setText(
                String.format(Locale.US, "%.1fh", completedMinutes / 60f));
    }

    private void renderWeeklyChart(List<Task> tasks, Calendar start) {
        weeklyBars.removeAllViews();
        int[] completedByDay = new int[7];
        int max = 0;
        for (Task task : tasks) {
            if (!task.done) continue;
            int index = dayIndex(task.date, start);
            if (index >= 0 && index < completedByDay.length) {
                completedByDay[index]++;
                max = Math.max(max, completedByDay[index]);
            }
        }

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) start.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            int count = completedByDay[i];

            View column = LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_insight_day_bar, weeklyBars, false);
            TextView value = column.findViewById(R.id.insightDayCount);
            value.setText(String.valueOf(count));
            value.setTextColor(count == 0 ? MUTED : DARK);
            int height = count == 0 ? dp(4) : Math.max(dp(12), dp(CHART_BAR_MAX_DP) * count / Math.max(1, max));
            View bar = column.findViewById(R.id.insightDayBar);
            bar.setBackgroundResource(count > 0 && count == max
                    ? R.drawable.bg_insights_bar_best : R.drawable.bg_insights_bar);
            android.view.ViewGroup.LayoutParams barParams = bar.getLayoutParams();
            barParams.height = height;
            bar.setLayoutParams(barParams);

            String name = new SimpleDateFormat("EEE", Locale.ENGLISH).format(day.getTime());
            TextView label = column.findViewById(R.id.insightDayLabel);
            label.setText(name.substring(0, 1));
            weeklyBars.addView(column);
        }
    }

    private void renderPriorityMix(List<Task> tasks, Calendar start, Calendar end) {
        categoriesContainer.removeAllViews();
        int[] counts = new int[PRIORITIES.length];
        int total = 0;
        for (Task task : tasks) {
            if (!inWeek(task, start, end)) continue;
            total++;
            String priority = task.priority == null ? "" : task.priority.trim().toUpperCase(Locale.ENGLISH);
            int index = priorityIndex(priority);
            counts[index]++;
        }

        if (total == 0) {
            categoriesContainer.addView(LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_insight_empty, categoriesContainer, false));
            return;
        }

        for (int i = 0; i < PRIORITIES.length; i++) {
            addPriorityRow(PRIORITIES[i], counts[i], counts[i] * 100 / total, priorityColors[i], i);
        }
    }

    private void addPriorityRow(String label, int count, int percent, int color, int index) {
        View row = LayoutInflater.from(getActivity())
                .inflate(R.layout.item_insight_priority, categoriesContainer, false);
        LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
        rowParams.topMargin = dp(index == 0 ? 0 : 12);
        row.setLayoutParams(rowParams);
        TextView name = row.findViewById(R.id.insightPriorityName);
        name.setText("UNASSIGNED".equals(label) ? "No priority" : titleCase(label));
        TextView value = row.findViewById(R.id.insightPriorityValue);
        value.setText(count + " tasks · " + percent + "%");
        ProgressBar progress = row.findViewById(R.id.insightPriorityProgress);
        progress.setMax(100);
        progress.setProgress(percent);
        progress.setProgressTintList(ColorStateList.valueOf(color));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(TRACK));
        categoriesContainer.addView(row);
    }

    private void renderFocusCard(List<Task> tasks, Calendar start, Calendar end) {
        int[] completedByDay = new int[7];
        for (Task task : tasks) {
            if (!task.done) continue;
            int index = dayIndex(task.date, start);
            if (index >= 0 && index < completedByDay.length) completedByDay[index]++;
        }

        int bestDay = -1;
        for (int i = 0; i < completedByDay.length; i++) {
            if (completedByDay[i] > 0 && (bestDay < 0 || completedByDay[i] > completedByDay[bestDay])) {
                bestDay = i;
            }
        }

        String summary;
        if (bestDay < 0) {
            summary = "No tasks completed this week yet";
        } else {
            Calendar day = (Calendar) start.clone();
            day.add(Calendar.DAY_OF_MONTH, bestDay);
            String dayName = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(day.getTime());
            summary = dayName + " · " + completedByDay[bestDay] + " completed";
        }
        ((TextView) findViewById(R.id.focusTimeText)).setText(summary);
    }

    private int priorityIndex(String priority) {
        if ("HIGH".equals(priority)) return 0;
        if ("MEDIUM".equals(priority)) return 1;
        if ("LOW".equals(priority)) return 2;
        return 3;
    }

    private String titleCase(String value) {
        return value.substring(0, 1) + value.substring(1).toLowerCase(Locale.ENGLISH);
    }

    private boolean inWeek(Task task, Calendar start, Calendar end) {
        if (task.date == null) return false;
        String key = task.date;
        return key.compareTo(dateKey(start)) >= 0 && key.compareTo(dateKey(end)) <= 0;
    }

    private int dayIndex(String date, Calendar start) {
        if (date == null) return -1;
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            if (parsed == null) return -1;
            Calendar day = Calendar.getInstance();
            day.setTime(parsed);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            return (int) ((day.getTimeInMillis() - start.getTimeInMillis()) / (24L * 60L * 60L * 1000L));
        } catch (java.text.ParseException ignored) {
            return -1;
        }
    }

    private Calendar weekStart() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        int daysSinceMonday = (start.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        start.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);
        return start;
    }

    private String dateKey(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private int dp(int value) {
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
