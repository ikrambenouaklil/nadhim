package com.example.myapplication.ui.today;
import com.example.myapplication.R;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.data.TaskStore;
import com.example.myapplication.data.Task;
import com.example.myapplication.ui.dialog.TaskEditorDialog;
import com.example.myapplication.ui.dialog.TaskDialogs;
import com.example.myapplication.data.Priority;
import com.example.myapplication.util.DateUtils;
import com.example.myapplication.data.SessionManager;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
        contentView = inflater.inflate(R.layout.fragment_today, container, false);
        tasksContainer = contentView.findViewById(R.id.todayTasksContainer);
        emptyTasksMessage = contentView.findViewById(R.id.todayTasksEmpty);

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

        String name = SessionManager.getName(getActivity());
        name = name == null ? "" : name.trim();

        TextView greeting = contentView.findViewById(R.id.greeting);
        greeting.setText(getString(greetingText, name));

        TextView avatar = contentView.findViewById(R.id.avatar);
        if (avatar != null) {
            avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ENGLISH));
        }
    }

    private void showTodayDate() {
        String today = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(new Date());
        TextView date = contentView.findViewById(R.id.date);
        date.setText(getString(R.string.date_subtitle, today));
    }

    private void loadTodaysTasks() {
        String todayKey = DateUtils.todayKey();
        todaysTasks.clear();

        // Keep today's tasks visible and carry unfinished overdue tasks forward.
        for (Task task : new TaskStore(getActivity()).getAll()) {
            if (task.date == null) continue;
            boolean dueToday = todayKey.equals(task.date);
            boolean overdue = task.date.compareTo(todayKey) < 0 && !task.done;
            if (dueToday || overdue) todaysTasks.add(task);
        }
        Collections.sort(todaysTasks, (first, second) -> {
            int dateOrder = first.date.compareTo(second.date);
            return dateOrder != 0 ? dateOrder : Integer.compare(first.minutes, second.minutes);
        });

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
        ImageButton editButton = row.findViewById(R.id.todayTaskEdit);
        ImageButton deleteButton = row.findViewById(R.id.todayTaskDelete);

        title.setText(task.title);
        boolean overdue = task.date.compareTo(DateUtils.todayKey()) < 0 && !task.done;
        String schedule = DateUtils.formatTime(task.minutes) + " – "
                + DateUtils.formatTime(task.minutes + task.duration) + "  ·  " + DateUtils.formatDuration(task.duration);
        time.setText(overdue ? "Overdue · " + DateUtils.formatShort(task.date) + " · " + schedule : schedule);
        showPriority(priority, task.priority);

        checkBox.setContentDescription(task.title);
        editButton.setContentDescription("Edit task " + task.title);
        deleteButton.setContentDescription("Delete task " + task.title);
        checkBox.setChecked(task.done);
        updateTitleStyle(title, task.done);
        checkBox.setOnCheckedChangeListener((button, isChecked) -> saveTaskStatus(task, checkBox, title, isChecked));
        editButton.setOnClickListener(v -> TaskEditorDialog.show(getActivity(), task, this::loadTodaysTasks));
        deleteButton.setOnClickListener(v -> TaskDialogs.confirmDelete(getActivity(), task, this::loadTodaysTasks));
        tasksContainer.addView(row);
    }

    private void saveTaskStatus(Task task, CheckBox checkBox, TextView title, boolean checked) {
        boolean previous = task.done;
        task.done = checked;
        if (!new TaskStore(getActivity()).update(task)) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(previous);
            checkBox.setOnCheckedChangeListener((button, value) -> saveTaskStatus(task, checkBox, title, value));
            task.done = previous;
            Toast.makeText(getActivity(), "Could not save task status", Toast.LENGTH_SHORT).show();
            return;
        }
        updateTitleStyle(title, checked);
        updateProgress();
    }

    private void showPriority(TextView badge, String value) {
        Priority priority = Priority.fromString(value);
        if (priority == Priority.NONE) {
            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);
        badge.setText(priority.name());
        if (priority == Priority.HIGH) {
            badge.setBackgroundResource(R.drawable.bg_badge_high);
        } else if (priority == Priority.LOW) {
            badge.setBackgroundResource(R.drawable.bg_badge_low);
            badge.setTextColor(getActivity().getColor(R.color.card_text_primary));
        } else {
            badge.setBackgroundResource(R.drawable.bg_badge_med);
            badge.setTextColor(getActivity().getColor(R.color.white));
        }
        if (priority == Priority.HIGH) {
            badge.setTextColor(getActivity().getColor(R.color.white));
        }
    }

    private void updateTitleStyle(TextView title, boolean done) {
        if (done) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(getActivity().getColor(R.color.card_text_secondary));
        } else {
            title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(getActivity().getColor(R.color.card_text_primary));
        }
    }

    private void updateProgress() {
        String todayKey = DateUtils.todayKey();
        int completed = 0;
        int total = 0;
        for (Task task : todaysTasks) {
            if (todayKey.equals(task.date)) {
                total++;
                if (task.done) completed++;
            }
        }

        int percent = total == 0 ? 0 : (completed * 100) / total;
        TextView subtitle = contentView.findViewById(R.id.progress_subtitle);
        TextView percentText = contentView.findViewById(R.id.progress_percent);
        ProgressBar bar = contentView.findViewById(R.id.progress_bar);
        subtitle.setText(getString(R.string.tasks_completed, completed, total));
        percentText.setText(getString(R.string.percent, percent));
        bar.setProgress(percent);
    }

}
