package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Reads and updates the currently signed-in user's local session. */
public final class SessionManager {
    private static final String PREFERENCES = "nadhim_preferences";
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";

    private SessionManager() { }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static String getName(Context context) {
        return preferences(context).getString(NAME_KEY, "");
    }

    public static String getEmail(Context context) {
        return preferences(context).getString(EMAIL_KEY, "");
    }

    public static void save(Context context, String name, String email) {
        preferences(context).edit().putString(NAME_KEY, name).putString(EMAIL_KEY, email).apply();
    }

    public static void clear(Context context) {
        preferences(context).edit().clear().apply();
    }
}
