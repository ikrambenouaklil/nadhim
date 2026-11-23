package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
    private static final String PREFERENCES = "nadhim_preferences";
    private static final String NAME_KEY = "name";

    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_DARK = Color.rgb(29, 78, 216);

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

        EditText nameInput = findViewById(R.id.nameInput);
        TextView continueButton = findViewById(R.id.continueButton);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE_DARK, BLUE});
        gradient.setCornerRadius(dp(16));
        continueButton.setBackground(gradient);

        continueButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please tell me your name first", Toast.LENGTH_SHORT).show();
                return;
            }
            getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit()
                    .putString(NAME_KEY, name)
                    .apply();
            openMainScreen();
        });
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}