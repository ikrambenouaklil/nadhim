package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    private static final String PREFERENCES = "nadhim_preferences";
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";

    public static String getSavedName(Context context) {
        return context.getSharedPreferences(PREFERENCES, context.MODE_PRIVATE)
                .getString(NAME_KEY, "");
    }
    public  static String getSavedEmail(Context context){

        return context.getSharedPreferences(PREFERENCES, context.MODE_PRIVATE)
                .getString( EMAIL_KEY,"");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Check if a user is already logged in
        String savedName = getSavedName(this);

        if (!savedName.trim().isEmpty()) {
            openMainScreen();
            return;
        }

        // 2. Check if an account already exists
        SharedPreferences accounts =
                getSharedPreferences("nadhim_accounts", MODE_PRIVATE);

        if (!accounts.getAll().isEmpty()) {
            openLoginScreen();
            return;
        }

        // 3. No account exists: show Welcome screen
        setContentView(R.layout.activity_welcome);

        TextView welcomeButton = findViewById(R.id.welcomeBtn);

        welcomeButton.setOnClickListener(view -> {
            openLoginScreen();
        });
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}