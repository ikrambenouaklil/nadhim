package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class TaskStore {

    private static final String PREFS_NAME     = "nadhim_tasks";
    private final SharedPreferences preferences;
    private final String userTasksKey;

    public   TaskStore (Context context ){
   preferences = context.getSharedPreferences(
           PREFS_NAME , Context.MODE_PRIVATE);
        String email = WelcomeActivity.getSavedEmail(context);
        userTasksKey = "tasks_" + email;
    }


}
