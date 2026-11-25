package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText name = findViewById(R.id.registerName);
        EditText email = findViewById(R.id.registerEmail);
        EditText password = findViewById(R.id.registerPassword);

        findViewById(R.id.registerSubmit).setOnClickListener(view -> {
            if (name.getText().toString().trim().isEmpty()
                    || email.getText().toString().trim().isEmpty()
                    || password.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            getSharedPreferences("nadhim_preferences", MODE_PRIVATE).edit()
                    .putString("name", name.getText().toString().trim())
                    .apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.registerLogin).setOnClickListener(view ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
