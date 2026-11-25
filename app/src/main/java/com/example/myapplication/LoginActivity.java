package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public class LoginActivity extends Activity {
    SharedPreferences sp ;

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
            sp = getSharedPreferences("nadhim_accounts",MODE_PRIVATE);

            String e = email.getText().toString().trim();   // email

            String enteredPassword = password.getText().toString();// the password that the user entred

            String storedValue  = sp.getString(e + ".password" , null )  ; // the stored Value of password

           if ( storedValue == null ){ // check if the email exists
               Toast.makeText(this , " user not exists ",Toast.LENGTH_SHORT).show();
               return;
           }

        //  2. check if the crediantials are correct

            boolean correct ;
            try {
                correct = verifyPassword(enteredPassword, storedValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if(!correct){
                Toast.makeText(this , "credentils are incorrect ",Toast.LENGTH_SHORT).show();
                return;
            }

            // save user name to pass it to the main activity
            String userName = sp.getString(e + ".name", "");

            getSharedPreferences("nadhim_preferences", MODE_PRIVATE)
                    .edit()
                    .putString("name", userName)
                    .putString("email", e)
                    .apply();
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


     // check if the password is correct
    private boolean verifyPassword(String password, String storedValue) throws Exception {

        // Separate the salt and the hash
        String[] parts = storedValue.split(":");

        if (parts.length != 2) {
            return false;
        }

        // Decode the stored salt and hash
        byte[] salt = android.util.Base64.decode(
                parts[0], android.util.Base64.NO_WRAP);

        byte[] savedHash = android.util.Base64.decode(
                parts[1], android.util.Base64.NO_WRAP);

        // Recreate the hash using the entered password and saved salt
        javax.crypto.SecretKeyFactory factory =
                javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        javax.crypto.spec.PBEKeySpec spec =
                new javax.crypto.spec.PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        600000,
                        256
                );

        byte[] newHash = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        // Compare the two hashes
        return java.security.MessageDigest.isEqual(savedHash, newHash);
    }
}
