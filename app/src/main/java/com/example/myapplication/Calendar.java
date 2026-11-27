package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Calendar extends Fragment {

    // ---- Nadhim theme (same colors as before) ----
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_BORDER = Color.rgb(219, 228, 255);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int FADED = Color.rgb(203, 213, 225);

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

    // =====================================================================
    // Lifecycle
    // =====================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_calendar, container, false);
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
    }

    private void attachListeners() {
        dayTab.setOnClickListener(v -> setMode(MODE_DAY));
        weekTab.setOnClickListener(v -> setMode(MODE_WEEK));
        monthTab.setOnClickListener(v -> setMode(MODE_MONTH));

        findViewById(R.id.prevButton).setOnClickListener(v -> shift(-1));
        findViewById(R.id.nextButton).setOnClickListener(v -> shift(1));

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
            tabs[i].setBackground(selected ? rounded(BLUE, 18, BLUE, 0) : null);
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

        // Header: M T W T F S S
        LinearLayout header = new LinearLayout(getActivity());
        header.setOrientation(LinearLayout.HORIZONTAL);
        String[] letters = {"M", "T", "W", "T", "F", "S", "S"};
        for (String letter : letters) {
            TextView t = new TextView(getActivity());
            t.setText(letter);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(MUTED);
            t.setTextSize(11);
            t.setLayoutParams(new LinearLayout.LayoutParams(0, dp(24), 1));
            header.addView(t);
        }
        monthGrid.addView(header);

        java.util.Calendar first = (java.util.Calendar) selectedDate.clone();
        first.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Calendar day = mondayOf(first);
        int month = selectedDate.get(java.util.Calendar.MONTH);

        for (int row = 0; row < 6; row++) {
            LinearLayout line = new LinearLayout(getActivity());
            line.setOrientation(LinearLayout.HORIZONTAL);
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

        int textColor = selected ? Color.WHITE : (inMonth ? DARK : FADED);

        LinearLayout cell = new LinearLayout(getActivity());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        if (selected) {
            cell.setBackground(rounded(BLUE, 12, BLUE, 0));
        } else if (today) {
            cell.setBackground(rounded(Color.WHITE, 12, BLUE, 1));
        }

        if (showWeekday) {
            TextView weekday = new TextView(getActivity());
            weekday.setGravity(Gravity.CENTER);
            weekday.setText(fmt("EEE", date));
            weekday.setTextColor(selected ? Color.WHITE : MUTED);
            weekday.setTextSize(10);
            cell.addView(weekday);
        }

        TextView number = new TextView(getActivity());
        number.setGravity(Gravity.CENTER);
        number.setText(String.valueOf(date.get(java.util.Calendar.DAY_OF_MONTH)));
        number.setTextColor(textColor);
        number.setTextSize(14);
        number.setTypeface(null, Typeface.BOLD);
        cell.addView(number);

        View dot = new View(getActivity());
        GradientDrawable dotShape = new GradientDrawable();
        dotShape.setShape(GradientDrawable.OVAL);
        dotShape.setColor(selected ? Color.WHITE : BLUE);
        dot.setBackground(dotShape);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(5), dp(5));
        dotParams.topMargin = dp(2);
        dot.setLayoutParams(dotParams);
        dot.setVisibility(hasEvents ? View.VISIBLE : View.INVISIBLE);
        cell.addView(dot);

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
            TextView header = new TextView(getActivity());
            header.setText(fmt("EEEE, MMM d", date));
            header.setTextColor(sameDate(date, java.util.Calendar.getInstance()) ? BLUE : DARK);
            header.setTextSize(12);
            header.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hp.topMargin = dp(10);
            hp.bottomMargin = dp(4);
            header.setLayoutParams(hp);
            header.setOnClickListener(v -> {
                selectedDate.setTimeInMillis(date.getTimeInMillis());
                mode = MODE_DAY;
                renderAll();
            });
            agendaContainer.addView(header);
        }

        if (list.isEmpty()) {
            TextView empty = new TextView(getActivity());
            empty.setText(withHeader ? "Free" : "Nothing planned. Tap below to add an event.");
            empty.setTextColor(MUTED);
            empty.setTextSize(12);
            empty.setPadding(0, dp(4), 0, dp(8));
            agendaContainer.addView(empty);
            return;
        }
        for (Event event : list) {
            agendaContainer.addView(createEventRow(event));
        }
    }

    private View createEventRow(final Event event) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(8);
        row.setLayoutParams(rowParams);

        TextView time = new TextView(getActivity());
        time.setText(time(event.task.minutes));
        time.setTextColor(MUTED);
        time.setTextSize(12);
        time.setPadding(0, dp(10), 0, 0);
        time.setLayoutParams(new LinearLayout.LayoutParams(dp(48),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(8), dp(10), dp(8));
        card.setBackground(rounded(Color.WHITE, 10, BLUE_BORDER, 1));
        card.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView title = new TextView(getActivity());
        title.setText(event.task.title);
        title.setTextColor(DARK);
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);

        TextView details = new TextView(getActivity());
        details.setText(time(event.task.minutes) + " – " + time(event.task.minutes + event.task.duration)
                + "  ·  " + event.task.duration + " min"
                + (event.task.priority == null || event.task.priority.isEmpty() ? "" : "  ·  " + event.task.priority));
        details.setTextColor(MUTED);
        details.setTextSize(11);

        card.addView(title);
        card.addView(details);
        row.addView(time);
        row.addView(card);

        card.setOnClickListener(v -> showTaskActions(event));
        return row;
    }

    // =====================================================================
    private void showTaskActions(final Event event) {
        new AlertDialog.Builder(getActivity())
                .setTitle(event.task.title)
                .setItems(new String[]{"Edit task", "Delete task"}, (dialog, choice) -> {
                    if (choice == 0) editTask(event.task);
                    else confirmDelete(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editTask(final Task task) {
        LinearLayout form = new LinearLayout(getActivity());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);
        form.addView(formLabel("Task name"));
        android.widget.EditText titleInput = new android.widget.EditText(getActivity());
        titleInput.setSingleLine(true);
        titleInput.setText(task.title);
        form.addView(titleInput);
        form.addView(formLabel("Notes"));
        android.widget.EditText notesInput = new android.widget.EditText(getActivity());
        notesInput.setMinLines(2);
        notesInput.setGravity(Gravity.TOP | Gravity.START);
        notesInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesInput.setText(task.notes == null ? "" : task.notes);
        form.addView(notesInput);

        form.addView(formLabel("Priority"));
        android.widget.Spinner priorityPicker = new android.widget.Spinner(getActivity());
        String[] priorities = {"None", "Low", "Medium", "High"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priorityPicker.setAdapter(adapter);
        String currentPriority = task.priority == null ? "" : task.priority.toUpperCase(Locale.ENGLISH);
        priorityPicker.setSelection("LOW".equals(currentPriority) ? 1
                : "MEDIUM".equals(currentPriority) ? 2 : "HIGH".equals(currentPriority) ? 3 : 0);
        form.addView(priorityPicker);

        java.util.Calendar editedDate = java.util.Calendar.getInstance();
        try {
            java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(task.date);
            if (parsed != null) editedDate.setTime(parsed);
        } catch (java.text.ParseException ignored) { }
        final int[] editedMinutes = {task.minutes};
        form.addView(formLabel("Deadline date"));
        TextView dateButton = pickerButton("Date: " + fmt("EEE, MMM d, yyyy", editedDate));
        dateButton.setOnClickListener(v -> new android.app.DatePickerDialog(getActivity(), (picker, year, month, day) -> {
            editedDate.set(year, month, day);
            dateButton.setText("Date: " + fmt("EEE, MMM d, yyyy", editedDate));
        }, editedDate.get(java.util.Calendar.YEAR), editedDate.get(java.util.Calendar.MONTH),
                editedDate.get(java.util.Calendar.DAY_OF_MONTH)).show());
        form.addView(dateButton);

        form.addView(formLabel("Deadline time"));
        TextView timeButton = pickerButton("Time: " + time(editedMinutes[0]));
        timeButton.setOnClickListener(v -> new android.app.TimePickerDialog(getActivity(), (picker, hour, minute) -> {
            editedMinutes[0] = hour * 60 + minute;
            timeButton.setText("Time: " + time(editedMinutes[0]));
        }, editedMinutes[0] / 60, editedMinutes[0] % 60, true).show());
        form.addView(timeButton);

        form.addView(formLabel("Estimated duration"));
        LinearLayout durationRow = new LinearLayout(getActivity());
        durationRow.setGravity(Gravity.CENTER);
        android.widget.NumberPicker hours = new android.widget.NumberPicker(getActivity());
        hours.setMinValue(0);
        hours.setMaxValue(72);
        hours.setValue(task.duration / 60);
        android.widget.NumberPicker minutes = new android.widget.NumberPicker(getActivity());
        String[] minuteOptions = {"00", "15", "30", "45"};
        minutes.setMinValue(0);
        minutes.setMaxValue(3);
        minutes.setDisplayedValues(minuteOptions);
        minutes.setValue((task.duration % 60) / 15);
        durationRow.addView(hours);
        durationRow.addView(minutes);
        form.addView(durationRow);

        android.widget.ScrollView editScroll = new android.widget.ScrollView(getActivity());
        editScroll.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Edit task")
                .setView(editScroll)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTitle = titleInput.getText().toString().trim();
            int newDuration = hours.getValue() * 60 + minutes.getValue() * 15;
            if (newTitle.isEmpty()) {
                titleInput.setError("Enter a task name");
                return;
            }
            if (newDuration == 0) {
                Toast.makeText(getActivity(), "Duration must be at least 15 minutes", Toast.LENGTH_SHORT).show();
                return;
            }
            task.title = newTitle;
            task.notes = notesInput.getText().toString().trim();
            task.date = dateKey(editedDate);
            task.minutes = editedMinutes[0];
            task.duration = newDuration;
            int selectedPriority = priorityPicker.getSelectedItemPosition();
            task.priority = selectedPriority == 0 ? "" : priorities[selectedPriority].toUpperCase(Locale.ENGLISH);
            if (!new TaskStore(getActivity()).update(task)) {
                Toast.makeText(getActivity(), "Could not save task changes", Toast.LENGTH_SHORT).show();
                return;
            }
            loadEvents();
            renderAll();
            Toast.makeText(getActivity(), "Task updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private TextView formLabel(String text) {
        TextView label = new TextView(getActivity());
        label.setText(text);
        label.setTextColor(DARK);
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, dp(12), 0, dp(2));
        return label;
    }

    private TextView pickerButton(String text) {
        TextView button = new TextView(getActivity());
        button.setText(text);
        button.setTextColor(DARK);
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setBackground(rounded(Color.WHITE, 8, BLUE_BORDER, 1));
        button.setMinHeight(dp(44));
        return button;
    }
    // Deletion
    // =====================================================================

    private void confirmDelete(final Event event) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete event?")
                .setMessage(event.task.title)
                .setPositiveButton("Delete", (d, w) -> {
                    events.remove(event);
                    new TaskStore(getActivity()).delete(event.task.id);
                    renderAll();
                    Toast.makeText(getActivity(), "Event deleted", Toast.LENGTH_SHORT).show();
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
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}
