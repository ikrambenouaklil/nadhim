package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
    private static final String PREFERENCES = "nadhim_preferences";
    private static final String NAME_KEY = "name";

    public static String getSavedName(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getString(NAME_KEY, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getSavedName(this).trim().isEmpty()) {
            openMainScreen();
            return;
        }

        setContentView(R.layout.activity_welcome);

        TextView welcomeButton = findViewById(R.id.welcomeBtn);
        welcomeButton.setOnClickListener(view ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void continueToApp(String name) {
        getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit()
                .putString(NAME_KEY, name)
                .apply();
        openMainScreen();
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}