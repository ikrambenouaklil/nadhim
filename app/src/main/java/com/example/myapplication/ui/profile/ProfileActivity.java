package com.example.myapplication.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.data.AccountStore;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.TaskStore;
import com.example.myapplication.ui.NadhimActivity;
import com.example.myapplication.ui.auth.WelcomeActivity;
import com.example.myapplication.util.PasswordHasher;

import java.util.Locale;

/** Profile, account details, password, theme, and logout controls in one screen. */
public class ProfileActivity extends NadhimActivity {
    private EditText nameInput;
    private EditText emailInput;
    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private TextView avatar;
    private TextView currentName;
    private TextView currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameInput = findViewById(R.id.profileNameInput);
        emailInput = findViewById(R.id.profileEmailInput);
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        avatar = findViewById(R.id.profileAvatar);
        currentName = findViewById(R.id.profileCurrentName);
        currentEmail = findViewById(R.id.profileCurrentEmail);

        showCurrentProfile();
        bindPasswordVisibility(currentPasswordInput, findViewById(R.id.toggleCurrentPassword));
        bindPasswordVisibility(newPasswordInput, findViewById(R.id.toggleNewPassword));
        bindPasswordVisibility(confirmPasswordInput, findViewById(R.id.toggleConfirmPassword));
        findViewById(R.id.profileBackButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveProfileButton).setOnClickListener(v -> saveProfile());
        findViewById(R.id.updatePasswordButton).setOnClickListener(v -> updatePassword());
        findViewById(R.id.logoutButton).setOnClickListener(v -> confirmLogout());
    }

    private void showCurrentProfile() {
        String name = SessionManager.getName(this).trim();
        String email = SessionManager.getEmail(this).trim();
        nameInput.setText(name);
        emailInput.setText(email);
        currentName.setText(name);
        currentEmail.setText(email);
        avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ENGLISH));
    }

    private void bindPasswordVisibility(EditText password, ImageButton toggle) {
        toggle.setOnClickListener(view -> {
            int cursor = password.getSelectionStart();
            boolean hidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (hidden) {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggle.setImageResource(R.drawable.ic_visibility);
                toggle.setContentDescription(getString(R.string.hide_password));
            } else {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggle.setImageResource(R.drawable.ic_visibility_off);
                toggle.setContentDescription(getString(R.string.show_password));
            }
            password.setSelection(Math.max(cursor, 0));
        });
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String oldEmail = SessionManager.getEmail(this).trim();
        String newEmail = emailInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("Enter your name");
            nameInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        AccountStore accounts = new AccountStore(this);
        if (!oldEmail.equalsIgnoreCase(newEmail) && accounts.exists(newEmail)) {
            emailInput.setError("An account already uses this email");
            emailInput.requestFocus();
            return;
        }

        boolean emailChanged = !oldEmail.equals(newEmail);
        if (emailChanged && !TaskStore.moveTasksToEmail(this, oldEmail, newEmail)) {
            emailInput.setError("This email already has task data on this device");
            emailInput.requestFocus();
            return;
        }
        if (!accounts.updateProfile(oldEmail, newEmail, name)) {
            if (emailChanged) TaskStore.moveTasksToEmail(this, newEmail, oldEmail);
            Toast.makeText(this, "Could not save your profile", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager.save(this, name, newEmail);
        showCurrentProfile();
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
    }

    private void updatePassword() {
        String oldPassword = currentPasswordInput.getText().toString();
        String newPassword = newPasswordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();
        if (oldPassword.isEmpty()) {
            currentPasswordInput.setError("Enter your current password");
            currentPasswordInput.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            newPasswordInput.setError("Use at least 6 characters");
            newPasswordInput.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmation)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        String email = SessionManager.getEmail(this);
        AccountStore accounts = new AccountStore(this);
        try {
            String storedHash = accounts.getPasswordHash(email);
            if (storedHash == null || !PasswordHasher.verify(oldPassword, storedHash)) {
                currentPasswordInput.setError("Current password is incorrect");
                currentPasswordInput.requestFocus();
                return;
            }
            if (!accounts.updatePassword(email, PasswordHasher.hash(newPassword))) {
                Toast.makeText(this, "Could not update your password", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Could not secure the new password", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPasswordInput.setText("");
        newPasswordInput.setText("");
        confirmPasswordInput.setText("");
        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    SessionManager.clear(this);
                    Intent intent = new Intent(this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
