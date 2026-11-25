package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText email = findViewById(R.id.loginEmail);
        EditText password = findViewById(R.id.loginPassword);

        findViewById(R.id.loginSubmit).setOnClickListener(view -> {
            if (email.getText().toString().trim().isEmpty()
                    || password.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter your email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            finishWithName(email.getText().toString().trim());
        });

        findViewById(R.id.loginGoogle).setOnClickListener(view ->
                Toast.makeText(this,
                        "Google Sign-In requires OAuth configuration",
                        Toast.LENGTH_LONG).show());
        findViewById(R.id.loginSignUp).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void finishWithName(String email) {
        getSharedPreferences("nadhim_preferences", MODE_PRIVATE).edit()
                .putString("name", email)
                .apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
