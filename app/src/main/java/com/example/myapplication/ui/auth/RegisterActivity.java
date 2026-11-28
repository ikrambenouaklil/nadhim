package com.example.myapplication.ui.auth;
import com.example.myapplication.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.data.AccountStore;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.util.PasswordHasher;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.NadhimActivity;
import com.example.myapplication.ui.theme.ThemeManager;

public class RegisterActivity extends NadhimActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we will work only on this interface
        setContentView(R.layout.activity_register);
        ThemeManager.bindToggle(this);

        EditText name = findViewById(R.id.registerName);
        EditText email = findViewById(R.id.registerEmail);
        EditText password = findViewById(R.id.registerPassword);
        ImageButton passwordVisibility = findViewById(R.id.toggleRegisterPassword);
        passwordVisibility.setOnClickListener(view -> {
            int cursorPosition = password.getSelectionStart();
            boolean hidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (hidden) {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibility.setImageResource(R.drawable.ic_visibility);
                passwordVisibility.setContentDescription(getString(R.string.hide_password));
            } else {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordVisibility.setImageResource(R.drawable.ic_visibility_off);
                passwordVisibility.setContentDescription(getString(R.string.show_password));
            }
            password.setSelection(Math.max(cursorPosition, 0));
        });

        // check if the user entred the infos ( not empty )
        findViewById(R.id.registerSubmit).setOnClickListener(view -> {
            if (name.getText().toString().trim().isEmpty()
                    || email.getText().toString().trim().isEmpty()
                    || password.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String e = AccountStore.normalize(email.getText().toString());
            AccountStore accounts = new AccountStore(this);
            if (accounts.exists(e)) {
                Toast.makeText(this, "user already exists ", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordHash;
            try {
                passwordHash = PasswordHasher.hash(password.getText().toString());
            } catch (Exception ex) {
                Toast.makeText(this, "Could not secure the password", Toast.LENGTH_SHORT).show();
                return;
            }
            String userName = name.getText().toString().trim();
            accounts.save(e, userName, passwordHash);
            SessionManager.save(this, userName, e);

               // once i completed the next interface swipe to main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        // if the user clicked on login
        findViewById(R.id.registerLogin).setOnClickListener(view ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
