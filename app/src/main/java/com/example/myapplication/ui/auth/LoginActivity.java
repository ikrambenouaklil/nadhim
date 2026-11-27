package com.example.myapplication.ui.auth;
import com.example.myapplication.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.data.AccountStore;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.util.PasswordHasher;
import com.example.myapplication.ui.main.MainActivity;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // use the login interface
        setContentView(R.layout.activity_login);

        EditText email = findViewById(R.id.loginEmail);
        EditText password = findViewById(R.id.loginPassword);
         // eye button
        ImageButton passwordVisibility = findViewById(R.id.togglePasswordVisibility);
        passwordVisibility.setOnClickListener(view -> {

            // the cursor | in the text
            int cursorPosition = password.getSelectionStart();
            boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (isHidden) {
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

        findViewById(R.id.forgotPassword).setOnClickListener(view ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.forgot_password_title)
                        .setMessage(R.string.forgot_password_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show());

       // once the use clicks on submit
        findViewById(R.id.loginSubmit).setOnClickListener(view -> {
            // check if the user didn't fill the form
            if (email.getText().toString().trim().isEmpty()
                    || password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter your email and password", Toast.LENGTH_SHORT).show();
                return;
            }
         // 1. check is the email exists
            String e = email.getText().toString().trim();   // email
            String enteredPassword = password.getText().toString();// the password that the user entred
            AccountStore accounts = new AccountStore(this);
            String storedValue = accounts.getPasswordHash(e);

           if ( storedValue == null ){ // check if the email exists
               Toast.makeText(this , " user not exists ",Toast.LENGTH_SHORT).show();
               return;
           }

        //  2. check if the crediantials are correct

            boolean correct ;
            try {
                correct = PasswordHasher.verify(enteredPassword, storedValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if(!correct){
                Toast.makeText(this , "credentils are incorrect ",Toast.LENGTH_SHORT).show();
                return;
            }

            // save user name to pass it to the main activity
            String userName = accounts.getName(e);
            SessionManager.save(this, userName, e);
            //  3. forward user to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();




        });

        findViewById(R.id.loginGoogle).setOnClickListener(view ->
                Toast.makeText(this,
                        "Google Sign-In requires OAuth configuration",
                        Toast.LENGTH_LONG).show());
        findViewById(R.id.loginSignUp).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
