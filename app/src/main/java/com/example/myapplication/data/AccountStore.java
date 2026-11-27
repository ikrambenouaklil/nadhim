package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores local accounts using the existing preference names and keys. */
public final class AccountStore {
    private static final String PREFERENCES = "nadhim_accounts";
    private final SharedPreferences preferences;

    public AccountStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean exists(String email) {
        return preferences.contains(key(email, "password"));
    }

    public String getPasswordHash(String email) {
        return preferences.getString(key(email, "password"), null);
    }

    public String getName(String email) {
        return preferences.getString(key(email, "name"), "");
    }

    public boolean hasAccounts() {
        return !preferences.getAll().isEmpty();
    }

    public void save(String email, String name, String passwordHash) {
        preferences.edit()
                .putString(key(email, "password"), passwordHash)
                .putString(key(email, "name"), name)
                .apply();
    }

    public boolean updateProfile(String currentEmail, String newEmail, String newName) {
        String oldEmail = currentEmail.trim();
        String updatedEmail = newEmail.trim();
        String passwordHash = getPasswordHash(oldEmail);
        if (passwordHash == null) return false;
        if (!oldEmail.equalsIgnoreCase(updatedEmail) && exists(updatedEmail)) return false;

        return preferences.edit()
                .remove(key(oldEmail, "password"))
                .remove(key(oldEmail, "name"))
                .putString(key(updatedEmail, "password"), passwordHash)
                .putString(key(updatedEmail, "name"), newName.trim())
                .commit();
    }

    public boolean updatePassword(String email, String passwordHash) {
        if (!exists(email)) return false;
        return preferences.edit().putString(key(email, "password"), passwordHash).commit();
    }

    private String key(String email, String field) {
        return email.trim() + "." + field;
    }
}
