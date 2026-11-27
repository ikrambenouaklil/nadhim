package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps all task saving and loading rules in one place.
 * Screens ask this class for tasks instead of accessing storage directly.
 */
public class TaskStore {
    private static final String PREFERENCES_NAME = "nadhim_tasks";
    private static final String TASKS_KEY_PREFIX = "tasks_";

    private final SharedPreferences preferences;
    private final String userTasksKey;

    public TaskStore(Context context) {
        // Context gives Android the app information needed to open local storage.
        preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );

        // Each email gets its own key, keeping user task lists separate.
        String email = WelcomeActivity.getSavedEmail(context);
        userTasksKey = email == null || email.trim().isEmpty()
                ? null
                : TASKS_KEY_PREFIX + email.trim();
    }

    /** Returns the tasks saved for the currently signed-in user. */
    public List<Task> getAll() {
        List<Task> tasks = new ArrayList<>();
        // Without a saved email, do not read or write tasks shared between users.
        if (userTasksKey == null) return tasks;

        String savedJson = preferences.getString(userTasksKey, "[]");

        try {
            JSONArray savedTasks = new JSONArray(savedJson);
            for (int i = 0; i < savedTasks.length(); i++) {
                JSONObject item = savedTasks.getJSONObject(i);
                tasks.add(new Task(
                        item.getLong("id"),
                        item.getString("title"),
                        item.getString("date"),
                        item.getInt("minutes"),
                        item.getInt("duration"),
                        item.optString("notes", ""),
                        item.optString("category", ""),
                        item.optString("priority", ""),
                        item.optBoolean("done", false)
                ));
            }
        } catch (JSONException ignored) {
            // If saved data is damaged, return an empty list instead of crashing the screen.
        }

        return tasks;
    }

    /** Adds one task to the current user list. */
    public boolean add(Task task) {
        List<Task> tasks = getAll();
        tasks.add(task);
        return saveAll(tasks);
    }

    /** Replaces the saved task with the same id. */
    public boolean update(Task updatedTask) {
        List<Task> tasks = getAll();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id == updatedTask.id) {
                tasks.set(i, updatedTask);
                return saveAll(tasks);
            }
        }
        return false;
    }

    /** Removes the task with this id from the user list. */
    public boolean delete(long taskId) {
        List<Task> tasks = getAll();
        boolean removed = false;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (tasks.get(i).id == taskId) {
                tasks.remove(i);
                removed = true;
            }
        }
        return removed && saveAll(tasks);
    }

    /** Converts tasks to JSON and saves the list under the current user key. */
    private boolean saveAll(List<Task> tasks) {
        if (userTasksKey == null) return false;

        JSONArray array = new JSONArray();
        try {
            for (Task task : tasks) {
                JSONObject item = new JSONObject();
                item.put("id", task.id);
                item.put("title", task.title);
                item.put("date", task.date);
                item.put("minutes", task.minutes);
                item.put("duration", task.duration);
                item.put("notes", task.notes);
                item.put("category", task.category);
                item.put("priority", task.priority);
                item.put("done", task.done);
                array.put(item);
            }
        } catch (JSONException ignored) {
            return false;
        }

        preferences.edit().putString(userTasksKey, array.toString()).apply();
        return true;
    }
}
