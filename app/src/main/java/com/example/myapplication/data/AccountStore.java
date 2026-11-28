package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/** Stores local accounts using the existing preference names and keys. */
public final class AccountStore {
    private static final String PREFERENCES = "nadhim_accounts";
    private final SharedPreferences preferences;

    public AccountStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean exists(String email) {
        migrateLegacyEmail(email);
        return preferences.contains(key(email, "password"));
    }

    public String getPasswordHash(String email) {
        migrateLegacyEmail(email);
        return preferences.getString(key(email, "password"), null);
    }

    public String getName(String email) {
        migrateLegacyEmail(email);
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
        String oldEmail = normalize(currentEmail);
        String updatedEmail = normalize(newEmail);
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
        return normalize(email) + "." + field;
    }

    /** Migrates an account saved before email keys were normalized. */
    private void migrateLegacyEmail(String email) {
        String normalizedEmail = normalize(email);
        String normalizedPasswordKey = key(normalizedEmail, "password");
        if (normalizedEmail.isEmpty() || preferences.contains(normalizedPasswordKey)) return;

        for (String storedKey : preferences.getAll().keySet()) {
            if (!storedKey.endsWith(".password")) continue;
            String storedEmail = storedKey.substring(0, storedKey.length() - ".password".length());
            if (!normalize(storedEmail).equals(normalizedEmail)) continue;

            String passwordHash = preferences.getString(storedKey, null);
            String storedName = preferences.getString(storedEmail + ".name", "");
            preferences.edit()
                    .putString(normalizedPasswordKey, passwordHash)
                    .putString(key(normalizedEmail, "name"), storedName)
                    .remove(storedKey)
                    .remove(storedEmail + ".name")
                    .commit();
            return;
        }
    }

    /** Returns one stable representation for email keys and comparisons. */
    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
