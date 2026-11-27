package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Reads and updates the currently signed-in user's local session. */
public final class SessionManager {
    private static final String PREFERENCES = "nadhim_preferences";
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String DARK_MODE_KEY = "dark_mode";

    private SessionManager() { }

    // u write the context and it gets the preferences
    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
    // u write the context + name key and it gets the value of a
    // pereferences if the key not exists it returns null

    public static String getName(Context context) {
        return preferences(context).getString(NAME_KEY, "");
    }
    // u write the context + email key and it gets the value of a
    // pereferences if the key not exists it returns null
    public static String getEmail(Context context) {
        return preferences(context).getString(EMAIL_KEY, "");
    }

    public static boolean isDarkMode(Context context) {
        return preferences(context).getBoolean(DARK_MODE_KEY, false);
    }

    public static void setDarkMode(Context context, boolean dark) {
        preferences(context).edit().putBoolean(DARK_MODE_KEY, dark).apply();
    }
    // u write the context + name + email and it saves the user infos in the context prefer
    public static void save(Context context, String name, String email) {
        preferences(context).edit().putString(NAME_KEY, name).putString(EMAIL_KEY, email).apply();
    }

    public static void clear(Context context) {
        preferences(context).edit().remove(NAME_KEY).remove(EMAIL_KEY).apply();
    }
}
