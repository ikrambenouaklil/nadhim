package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

    private String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        java.security.SecureRandom random = new java.security.SecureRandom();
        random.nextBytes(salt);

        javax.crypto.SecretKeyFactory factory =
                javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        javax.crypto.spec.PBEKeySpec spec =
                new javax.crypto.spec.PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        600000,
                        256
                );

        byte[] hash = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        return android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
                + ":"
                + android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we will work only on this interface
        setContentView(R.layout.activity_register);

        EditText name = findViewById(R.id.registerName);
        EditText email = findViewById(R.id.registerEmail);
        EditText password = findViewById(R.id.registerPassword);

        // check if the user entred the infos ( not empty )
        findViewById(R.id.registerSubmit).setOnClickListener(view -> {
            if (name.getText().toString().trim().isEmpty()
                    || email.getText().toString().trim().isEmpty()
                    || password.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // i created a shared P named nadhim accounts and i'll add the user infos to it
            SharedPreferences sp = getSharedPreferences("nadhim_accounts", MODE_PRIVATE);

            String e = email.getText().toString().trim();
            String p ;
            // hashing password
            try {
                p = hashPassword(password.getText().toString());
            } catch (Exception ex) {
                ex.printStackTrace(); // print Exception details  in logcat

                return;
            }
            String key = e + ".password";

             // check if the user already exists
            if(sp.contains(key)){
                Toast.makeText(this , "user already exists ", Toast.LENGTH_SHORT).show();
                return ;}

             // "ikram@gmail.com.password" : real password
            //  "ikram@gmail.com.name" : user  name
            sp.edit().putString(e+".password",p)
                    .putString(e+".name",name.getText().toString().trim()).apply();

            String userName = name.getText().toString().trim();

            getSharedPreferences("nadhim_preferences", MODE_PRIVATE)
                    .edit()
                    .putString("name", userName)
                    .putString("email",e )
                    .apply();

               // once i completed the next interface is main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        // if the user clicked on login
        findViewById(R.id.registerLogin).setOnClickListener(view ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
