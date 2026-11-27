package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Calendar extends Fragment {

    // ---- Nadhim theme (same colors as before) ----
    private int BLUE;
    private int BLUE_BORDER;
    private int DARK;
    private int MUTED;
    private int FADED;

    private static final int MODE_DAY = 0;
    private static final int MODE_WEEK = 1;
    private static final int MODE_MONTH = 2;

    private View contentView;

    public void showDate(String dateKey) {
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey);
            if (date != null) {
                selectedDate.setTime(date);
                if (contentView != null) renderAll();
            }
        } catch (java.text.ParseException ignored) { }
    }

    // ---- Event model ----
    private static class Event {
        final Task task;

        Event(Task task) {
            this.task = task;
        }
    }

    private final List<Event> events = new ArrayList<>();
    private final java.util.Calendar selectedDate = java.util.Calendar.getInstance();
    private int mode = MODE_DAY;

    private LinearLayout weekDaysContainer;
    private LinearLayout monthGrid;
    private LinearLayout agendaContainer;
    private TextView calendarMonthText;
    private TextView agendaDateText;
    private TextView dayTab;
    private TextView weekTab;
    private TextView monthTab;
    private TextView addTaskToDateButton;

    // =====================================================================
    // Lifecycle
    // =====================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_calendar, container, false);
        BLUE = getActivity().getColor(R.color.brand_blue);
        BLUE_BORDER = getActivity().getColor(R.color.brand_blue_border);
        DARK = getActivity().getColor(R.color.insight_dark);
        MUTED = getActivity().getColor(R.color.insight_muted);
        FADED = getActivity().getColor(R.color.text_hint);
        bindViews();
        loadEvents();
        attachListeners();
        renderAll();
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();   // Reload in case the Add screen added events
        renderAll();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && contentView != null) {
            loadEvents();
            renderAll();
        }
    }

    private void bindViews() {
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        monthGrid = findViewById(R.id.monthGrid);
        agendaContainer = findViewById(R.id.agendaContainer);
        calendarMonthText = findViewById(R.id.calendarMonthText);
        agendaDateText = findViewById(R.id.agendaDateText);
        dayTab = findViewById(R.id.dayTab);
        weekTab = findViewById(R.id.weekTab);
        monthTab = findViewById(R.id.monthTab);
        addTaskToDateButton = findViewById(R.id.addTaskToDateButton);
    }

    private void attachListeners() {
        dayTab.setOnClickListener(v -> setMode(MODE_DAY));
        weekTab.setOnClickListener(v -> setMode(MODE_WEEK));
        monthTab.setOnClickListener(v -> setMode(MODE_MONTH));

        findViewById(R.id.prevButton).setOnClickListener(v -> shift(-1));
        findViewById(R.id.nextButton).setOnClickListener(v -> shift(1));
        addTaskToDateButton.setOnClickListener(v ->
                ((MainActivity) getActivity()).showAddTaskForDate(dateKey(selectedDate)));

    }

    // =====================================================================
    // Navigation
    // =====================================================================

    private void setMode(int newMode) {
        mode = newMode;
        renderAll();
    }

    private void shift(int direction) {
        if (mode == MODE_DAY) {
            selectedDate.add(java.util.Calendar.DAY_OF_MONTH, direction);
        } else if (mode == MODE_WEEK) {
            selectedDate.add(java.util.Calendar.DAY_OF_MONTH, 7 * direction);
        } else {
            selectedDate.add(java.util.Calendar.MONTH, direction);
        }
        renderAll();
    }

    // =====================================================================
    // Display
    // =====================================================================

    private void renderAll() {
        calendarMonthText.setText(fmt("MMMM yyyy", selectedDate));
        addTaskToDateButton.setText("＋ Add task for " + fmt("EEE, MMM d", selectedDate));
        styleTabs();

        boolean month = mode == MODE_MONTH;
        weekDaysContainer.setVisibility(month ? View.GONE : View.VISIBLE);
        monthGrid.setVisibility(month ? View.VISIBLE : View.GONE);
        if (month) {
            renderMonthGrid();
        } else {
            renderWeekStrip();
        }

        renderAgenda();

    }

    private void styleTabs() {
        TextView[] tabs = {dayTab, weekTab, monthTab};
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = i == mode;
            tabs[i].setBackgroundResource(selected ? R.drawable.bg_calendar_tab_selected : 0);
            tabs[i].setTextColor(selected ? Color.WHITE : MUTED);
            tabs[i].setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void renderWeekStrip() {
        weekDaysContainer.removeAllViews();
        java.util.Calendar day = mondayOf(selectedDate);
        for (int i = 0; i < 7; i++) {
            weekDaysContainer.addView(createDayCell(day, true, true, 56));
            day.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void renderMonthGrid() {
        monthGrid.removeAllViews();
        monthGrid.addView(LayoutInflater.from(getActivity())
                .inflate(R.layout.item_calendar_month_header, monthGrid, false));

        java.util.Calendar first = (java.util.Calendar) selectedDate.clone();
        first.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Calendar day = mondayOf(first);
        int month = selectedDate.get(java.util.Calendar.MONTH);

        for (int row = 0; row < 6; row++) {
            LinearLayout line = (LinearLayout) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_calendar_week_row, monthGrid, false);
            for (int col = 0; col < 7; col++) {
                boolean inMonth = day.get(java.util.Calendar.MONTH) == month;
                line.addView(createDayCell(day, false, inMonth, 44));
                day.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
            monthGrid.addView(line);
        }
    }

    private View createDayCell(java.util.Calendar source, boolean showWeekday,
                               boolean inMonth, int heightDp) {
        final java.util.Calendar date = (java.util.Calendar) source.clone();
        boolean selected = sameDate(date, selectedDate);
        boolean today = sameDate(date, java.util.Calendar.getInstance());
        boolean hasEvents = !eventsFor(dateKey(date)).isEmpty();
        LinearLayout cell = (LinearLayout) LayoutInflater.from(getActivity())
                .inflate(R.layout.item_calendar_day, weekDaysContainer, false);
        TextView weekday = cell.findViewById(R.id.calendarDayWeekday);
        TextView number = cell.findViewById(R.id.calendarDayNumber);
        View dot = cell.findViewById(R.id.calendarDayDot);
        weekday.setVisibility(showWeekday ? View.VISIBLE : View.GONE);
        weekday.setText(fmt("EEE", date));
        weekday.setTextColor(selected ? Color.WHITE : MUTED);
        number.setText(String.valueOf(date.get(java.util.Calendar.DAY_OF_MONTH)));
        number.setTextColor(selected ? Color.WHITE : (inMonth ? DARK : FADED));
        cell.setBackgroundResource(selected ? R.drawable.bg_calendar_day_selected
                : today ? R.drawable.bg_calendar_day_today : 0);
        dot.setBackgroundResource(selected ? R.drawable.bg_calendar_dot_selected : R.drawable.bg_calendar_dot);
        dot.setVisibility(hasEvents ? View.VISIBLE : View.INVISIBLE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(heightDp), 1);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        cell.setLayoutParams(params);
        cell.setOnClickListener(v -> {
            selectedDate.setTimeInMillis(date.getTimeInMillis());
            renderAll();
        });
        return cell;
    }

    private void renderAgenda() {
        agendaContainer.removeAllViews();

        if (mode == MODE_WEEK) {
            java.util.Calendar monday = mondayOf(selectedDate);
            java.util.Calendar sunday = (java.util.Calendar) monday.clone();
            sunday.add(java.util.Calendar.DAY_OF_MONTH, 6);
            agendaDateText.setText("Week of " + fmt("MMM d", monday) + " – " + fmt("MMM d", sunday));

            java.util.Calendar day = (java.util.Calendar) monday.clone();
            for (int i = 0; i < 7; i++) {
                addDaySection(day, true);
                day.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
        } else {
            agendaDateText.setText(fmt("EEEE, MMMM d", selectedDate));
            addDaySection(selectedDate, false);
        }
    }

    private void addDaySection(java.util.Calendar source, boolean withHeader) {
        final java.util.Calendar date = (java.util.Calendar) source.clone();
        List<Event> list = eventsFor(dateKey(date));

        if (withHeader) {
            TextView header = (TextView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_calendar_agenda_header, agendaContainer, false);
            header.setText(fmt("EEEE, MMM d", date));
            header.setTextColor(sameDate(date, java.util.Calendar.getInstance()) ? BLUE : DARK);
            header.setOnClickListener(v -> {
                selectedDate.setTimeInMillis(date.getTimeInMillis());
                mode = MODE_DAY;
                renderAll();
            });
            agendaContainer.addView(header);
        }

        if (list.isEmpty()) {
            TextView empty = (TextView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_calendar_agenda_empty, agendaContainer, false);
            empty.setText(withHeader ? "Free" : "Nothing planned. Tap below to add an event.");
            agendaContainer.addView(empty);
            return;
        }
        for (Event event : list) {
            agendaContainer.addView(createEventRow(event));
        }
    }

    private View createEventRow(final Event event) {
        View row = LayoutInflater.from(getActivity())
                .inflate(R.layout.item_calendar_event, agendaContainer, false);
        CheckBox checkBox = row.findViewById(R.id.calendarEventCheck);
        TextView time = row.findViewById(R.id.calendarEventTime);
        View priorityBar = row.findViewById(R.id.calendarEventPriorityBar);
        TextView title = row.findViewById(R.id.calendarEventTitle);
        TextView details = row.findViewById(R.id.calendarEventDetails);
        ImageButton editButton = row.findViewById(R.id.calendarEventEdit);
        ImageButton deleteButton = row.findViewById(R.id.calendarEventDelete);
        checkBox.setContentDescription("Mark " + event.task.title + " complete");
        checkBox.setChecked(event.task.done);
        time.setText(time(event.task.minutes));
        title.setText(event.task.title);
        title.setTextColor(event.task.done ? MUTED : DARK);
        if (event.task.done) title.setPaintFlags(title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        details.setText(time(event.task.minutes) + " – " + time(event.task.minutes + event.task.duration)
                + "  ·  " + event.task.duration + " min");
        priorityBar.setBackgroundColor(priorityColor(event.task.priority));
        editButton.setContentDescription("Edit task " + event.task.title);
        editButton.setOnClickListener(v -> editTask(event.task));
        deleteButton.setContentDescription("Delete task " + event.task.title);
        deleteButton.setOnClickListener(v -> confirmDelete(event));

        CompoundButton.OnCheckedChangeListener[] listenerHolder = new CompoundButton.OnCheckedChangeListener[1];
        listenerHolder[0] = (button, checked) -> {
            boolean previous = event.task.done;
            event.task.done = checked;
            if (!new TaskStore(getActivity()).update(event.task)) {
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(previous);
                checkBox.setOnCheckedChangeListener(listenerHolder[0]);
                Toast.makeText(getActivity(), "Could not save task status", Toast.LENGTH_SHORT).show();
                return;
            }
            title.setTextColor(checked ? MUTED : DARK);
            title.setPaintFlags(checked
                    ? title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    : title.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        };
        checkBox.setOnCheckedChangeListener(listenerHolder[0]);
        return row;
    }

    private int priorityColor(String priority) {
        if (priority == null) return MUTED;
        if ("HIGH".equalsIgnoreCase(priority)) return getActivity().getColor(R.color.brand_blue);
        if ("LOW".equalsIgnoreCase(priority)) return getActivity().getColor(R.color.accent_green);
        if ("MEDIUM".equalsIgnoreCase(priority)) return getActivity().getColor(R.color.brand_blue_mid);
        return MUTED;
    }

    private void editTask(final Task task) {
        TaskEditorDialog.show(getActivity(), task, () -> {
            loadEvents();
            renderAll();
        });
    }

    // Deletion
    // =====================================================================

    private void confirmDelete(final Event event) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete task?")
                .setMessage(event.task.title)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (!new TaskStore(getActivity()).delete(event.task.id)) {
                        Toast.makeText(getActivity(), "Could not delete task", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    events.remove(event);
                    renderAll();
                    Toast.makeText(getActivity(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =====================================================================
    // TaskStore handles saving and loading tasks from local storage.
    // =====================================================================

    private List<Event> eventsFor(String dateKey) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.task.date.equals(dateKey)) result.add(e);
        }
        Collections.sort(result, (a, b) -> Integer.compare(a.task.minutes, b.task.minutes));
        return result;
    }

    private void loadEvents() {
        events.clear();
        // Load real tasks from TaskStore and adapt them for the calendar display.
        for (Task task : new TaskStore(getActivity()).getAll()) {
            events.add(new Event(task));
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private java.util.Calendar mondayOf(java.util.Calendar source) {
        java.util.Calendar c = (java.util.Calendar) source.clone();
        int offset = (c.get(java.util.Calendar.DAY_OF_WEEK) - java.util.Calendar.MONDAY + 7) % 7;
        c.add(java.util.Calendar.DAY_OF_MONTH, -offset);
        return c;
    }

    private String fmt(String pattern, java.util.Calendar c) {
        return new SimpleDateFormat(pattern, Locale.ENGLISH).format(c.getTime());
    }

    private String dateKey(java.util.Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private static String time(int totalMinutes) {
        int m = totalMinutes % (24 * 60);
        return String.format(Locale.US, "%02d:%02d", m / 60, m % 60);
    }

    private boolean sameDate(java.util.Calendar a, java.util.Calendar b) {
        return a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR)
                && a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private int dp(int value) {
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
