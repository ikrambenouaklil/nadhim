package com.example.myapplication.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import com.example.myapplication.data.Task;
import com.example.myapplication.data.TaskStore;

/** Shared task confirmation dialogs. */
public final class TaskDialogs {
    private TaskDialogs() { }

    public static void confirmDelete(Activity activity, Task task, Runnable onDeleted) {
        new AlertDialog.Builder(activity)
                .setTitle("Delete task?")
                .setMessage(task.title)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (!new TaskStore(activity).delete(task.id)) {
                        Toast.makeText(activity, "Could not delete task", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(activity, "Task deleted", Toast.LENGTH_SHORT).show();
                    onDeleted.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
