package com.example.myapplication.ui.auth;
import com.example.myapplication.R;
import com.example.myapplication.ui.main.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.data.AccountStore;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.ui.NadhimActivity;
import com.example.myapplication.ui.theme.ThemeManager;

public class WelcomeActivity extends NadhimActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Check if a user is already logged in
        String savedName = SessionManager.getName(this);

        if (!savedName.trim().isEmpty()) {
            openMainScreen();
            return;
        }

        // 2. Check if an account already exists
        if (new AccountStore(this).hasAccounts()) {
            openLoginScreen();
            return;
        }

        // 3. No account exists: show Welcome screen
        setContentView(R.layout.activity_welcome);
        ThemeManager.bindToggle(this);

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
