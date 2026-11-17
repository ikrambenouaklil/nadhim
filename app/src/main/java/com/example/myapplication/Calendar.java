package com.example.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Calendar extends Activity {
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final String[][] EVENTS = {
            {"09:00", "Morning planning", "30 min"},
            {"10:00", "Project presentation", "45 min"},
            {"12:30", "Review lecture notes", "60 min"},
            {"15:30", "Team check-in", "30 min"}
    };

    private final java.util.Calendar selectedDate = java.util.Calendar.getInstance();
    private LinearLayout weekDaysContainer;
    private LinearLayout agendaContainer;
    private TextView calendarMonthText;
    private TextView agendaDateText;
    private TextView dayTab;
    private TextView weekTab;
    private TextView monthTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        bindViews();
        renderCalendar();
        attachListeners();
    }

    private void bindViews() {
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        agendaContainer = findViewById(R.id.agendaContainer);
        calendarMonthText = findViewById(R.id.calendarMonthText);
        agendaDateText = findViewById(R.id.agendaDateText);
        dayTab = findViewById(R.id.dayTab);
        weekTab = findViewById(R.id.weekTab);
        monthTab = findViewById(R.id.monthTab);
    }

    private void attachListeners() {
        dayTab.setOnClickListener(view -> selectTab(dayTab));
        weekTab.setOnClickListener(view -> selectTab(weekTab));
        monthTab.setOnClickListener(view -> selectTab(monthTab));
        findViewById(R.id.addEventButton).setOnClickListener(view ->
                Toast.makeText(this, "Add a planning block", Toast.LENGTH_SHORT).show());
        findViewById(R.id.todayNavigation).setOnClickListener(view -> {
            selectedDate.setTimeInMillis(System.currentTimeMillis());
            renderCalendar();
        });
        findViewById(R.id.addNavigation).setOnClickListener(view ->
                Toast.makeText(this, "Add an event", Toast.LENGTH_SHORT).show());
        findViewById(R.id.insightsNavigation).setOnClickListener(view ->
                Toast.makeText(this, "Insights", Toast.LENGTH_SHORT).show());
    }

    private void selectTab(TextView selectedTab) {
        TextView[] tabs = {dayTab, weekTab, monthTab};
        for (TextView tab : tabs) {
            boolean selected = tab == selectedTab;
            tab.setBackground(selected ? rounded(BLUE, 18, BLUE, 0) : null);
            tab.setTextColor(selected ? Color.WHITE : MUTED);
            tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void renderCalendar() {
        calendarMonthText.setText(new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
                .format(selectedDate.getTime()));
        agendaDateText.setText(new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH)
                .format(selectedDate.getTime()));
        renderWeekDays();
        renderEvents();
    }

    private void renderWeekDays() {
        weekDaysContainer.removeAllViews();
        java.util.Calendar firstDay = (java.util.Calendar) selectedDate.clone();
        int offset = (firstDay.get(java.util.Calendar.DAY_OF_WEEK)
                - java.util.Calendar.MONDAY + 7) % 7;
        firstDay.add(java.util.Calendar.DAY_OF_MONTH, -offset);

        for (int index = 0; index < 7; index++) {
            java.util.Calendar date = (java.util.Calendar) firstDay.clone();
            date.add(java.util.Calendar.DAY_OF_MONTH, index);
            weekDaysContainer.addView(createDayCell(date));
        }
    }

    private View createDayCell(java.util.Calendar date) {
        boolean selected = sameDate(date, selectedDate);
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(2, 5, 2, 5);
        cell.setBackground(selected ? rounded(BLUE, 10, BLUE, 0) : null);

        TextView weekday = new TextView(this);
        weekday.setGravity(Gravity.CENTER);
        weekday.setText(new SimpleDateFormat("EEEEE", Locale.ENGLISH).format(date.getTime()));
        weekday.setTextColor(selected ? Color.WHITE : MUTED);
        weekday.setTextSize(9);

        TextView dayNumber = new TextView(this);
        dayNumber.setGravity(Gravity.CENTER);
        dayNumber.setText(String.valueOf(date.get(java.util.Calendar.DAY_OF_MONTH)));
        dayNumber.setTextColor(selected ? Color.WHITE : DARK);
        dayNumber.setTextSize(11);
        dayNumber.setTypeface(null, Typeface.BOLD);

        cell.addView(weekday);
        cell.addView(dayNumber);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        weekDaysContainer.setWeightSum(7);
        cell.setLayoutParams(params);
        cell.setOnClickListener(view -> {
            selectedDate.setTimeInMillis(date.getTimeInMillis());
            renderCalendar();
        });
        return cell;
    }

    private void renderEvents() {
        agendaContainer.removeAllViews();
        for (String[] event : EVENTS) {
            agendaContainer.addView(createEventRow(event));
        }
    }

    private View createEventRow(String[] event) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView time = new TextView(this);
        time.setText(event[0]);
        time.setTextColor(MUTED);
        time.setTextSize(9);
        time.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(dp(42), dp(48));
        time.setLayoutParams(timeParams);

        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(5), dp(8), dp(5));
        card.setBackground(rounded(Color.WHITE, 9, Color.rgb(219, 228, 255), 1));

        TextView title = new TextView(this);
        title.setText(event[1]);
        title.setTextColor(DARK);
        title.setTextSize(10);
        title.setTypeface(null, Typeface.BOLD);

        TextView duration = new TextView(this);
        duration.setText(event[2]);
        duration.setTextColor(MUTED);
        duration.setTextSize(8);

        card.addView(title);
        card.addView(duration);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                0, dp(48), 1);
        card.setLayoutParams(cardParams);
        row.addView(time);
        row.addView(card);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        rowParams.bottomMargin = dp(7);
        row.setLayoutParams(rowParams);
        return row;
    }

    private boolean sameDate(java.util.Calendar first, java.util.Calendar second) {
        return first.get(java.util.Calendar.YEAR) == second.get(java.util.Calendar.YEAR)
                && first.get(java.util.Calendar.DAY_OF_YEAR)
                == second.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private GradientDrawable rounded(int fillColor, int radiusDp, int strokeColor,
                                     int strokeWidthDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeWidthDp > 0) {
            drawable.setStroke(dp(strokeWidthDp), strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
