package com.example.myapplication;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Insights extends Fragment {
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_DARK = Color.rgb(29, 78, 216);
    private static final int BLUE_LIGHT = Color.rgb(96, 165, 250);
    private static final int LIME = Color.rgb(163, 230, 0);
    private static final int PURPLE = Color.rgb(147, 51, 234);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int[] CATEGORY_COLORS = {BLUE, LIME, PURPLE};
    private static final int CHART_BAR_MAX_DP = 130;

    private LinearLayout weeklyBars;
    private LinearLayout categoriesContainer;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_insights, container, false);
        weeklyBars = findViewById(R.id.weeklyBars);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        refreshInsights();
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
        if (contentView != null) refreshInsights();
    }

    private void refreshInsights() {
        // Read the current user's saved tasks, then calculate every insight from that list.
        List<Task> tasks = new TaskStore(getActivity()).getAll();
        renderStats(tasks);
        renderWeeklyChart(tasks);
        renderCategories(tasks);
        renderFocusCard(tasks);
    }

    private void renderStats(List<Task> tasks) {
        int completed = 0;
        int plannedMinutes = 0;
        for (Task task : tasks) {
            if (task.done) completed++;
            plannedMinutes += Math.max(0, task.duration);
        }

        int total = tasks.size();
        int completionRate = total == 0 ? 0 : (completed * 100) / total;
        float plannedHours = plannedMinutes / 60f;

        ((TextView) findViewById(R.id.statTasksValue)).setText(String.valueOf(completed));
        ((TextView) findViewById(R.id.statOnTimeValue)).setText(completionRate + "%");
        ((TextView) findViewById(R.id.statFocusValue)).setText(
                String.format(Locale.US, "%.1fh", plannedHours));
    }

    private void renderWeeklyChart(List<Task> tasks) {
        weeklyBars.removeAllViews();

        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        int daysSinceMonday = (weekStart.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        weekStart.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);

        List<Calendar> days = new ArrayList<>();
        int[] counts = new int[7];
        int max = 0;
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            days.add(day);

            String key = dateKey(day);
            for (Task task : tasks) {
                if (key.equals(task.date)) counts[i]++;
            }
            max = Math.max(max, counts[i]);
        }

        for (int i = 0; i < days.size(); i++) {
            Calendar day = days.get(i);
            int count = counts[i];
            LinearLayout column = new LinearLayout(getActivity());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

            int barHeight = count == 0
                    ? dp(4)
                    : Math.max(dp(14), dp(CHART_BAR_MAX_DP) * count / Math.max(1, max));
            View bar = new View(getActivity());
            bar.setBackground(i == maxIndex(counts) && count > 0
                    ? barDrawable(LIME, LIME)
                    : barDrawable(BLUE_LIGHT, BLUE));
            bar.setLayoutParams(new LinearLayout.LayoutParams(dp(18), barHeight));

            String fullDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(day.getTime());
            TextView label = new TextView(getActivity());
            label.setText(fullDay.substring(0, 1));
            label.setTextColor(MUTED);
            label.setTextSize(11);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(10);
            label.setLayoutParams(labelParams);

            String dayName = fullDay;
            column.setOnClickListener(v -> Toast.makeText(getActivity(),
                    count + " tasks scheduled for " + dayName, Toast.LENGTH_SHORT).show());

            column.addView(bar);
            column.addView(label);
            weeklyBars.addView(column);
        }
    }

    private int maxIndex(int[] values) {
        int index = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[index]) index = i;
        }
        return index;
    }

    private void renderCategories(List<Task> tasks) {
        categoriesContainer.removeAllViews();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Task task : tasks) {
            String category = task.category == null ? "" : task.category.trim();
            if (category.isEmpty()) category = "Uncategorized";
            counts.put(category, counts.getOrDefault(category, 0) + 1);
        }

        if (counts.isEmpty()) {
            addCategoryRow("No tasks yet", 0, 0);
            return;
        }

        int index = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int percent = tasks.isEmpty() ? 0 : entry.getValue() * 100 / tasks.size();
            addCategoryRow(entry.getKey(), percent, index++);
        }
    }

    private void addCategoryRow(String category, int percentValue, int index) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(index == 0 ? 0 : 14);
        row.setLayoutParams(rowParams);

        View dot = new View(getActivity());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(CATEGORY_COLORS[index % CATEGORY_COLORS.length]);
        dot.setBackground(shape);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(10), dp(10));
        dotParams.setMarginEnd(dp(12));
        dot.setLayoutParams(dotParams);

        TextView name = new TextView(getActivity());
        name.setText(category);
        name.setTextColor(DARK);
        name.setTextSize(13);
        name.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView percent = new TextView(getActivity());
        percent.setText(percentValue + "%");
        percent.setTextColor(MUTED);
        percent.setTextSize(12);

        row.addView(dot);
        row.addView(name);
        row.addView(percent);
        categoriesContainer.addView(row);
    }

    private void renderFocusCard(List<Task> tasks) {
        View card = findViewById(R.id.focusCard);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE_DARK, BLUE});
        gradient.setCornerRadius(dp(16));
        card.setBackground(gradient);

        int[] tasksPerHour = new int[24];
        for (Task task : tasks) {
            int hour = task.minutes / 60;
            if (hour >= 0 && hour < tasksPerHour.length) tasksPerHour[hour]++;
        }

        int busiestHour = -1;
        for (int hour = 0; hour < tasksPerHour.length; hour++) {
            if (tasksPerHour[hour] > 0
                    && (busiestHour == -1 || tasksPerHour[hour] > tasksPerHour[busiestHour])) {
                busiestHour = hour;
            }
        }

        String timeText = busiestHour == -1
                ? "No tasks yet"
                : String.format(Locale.US, "%02d:00–%02d:00",
                        busiestHour, (busiestHour + 1) % 24);
        ((TextView) findViewById(R.id.focusTimeText)).setText(timeText);
    }

    private String dateKey(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    /** Creates a rounded vertical gradient for one chart bar. */
    private GradientDrawable barDrawable(int topColor, int bottomColor) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{topColor, bottomColor});
        drawable.setCornerRadius(dp(9));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
