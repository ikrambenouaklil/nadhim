package com.example.myapplication.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Creates and verifies the salted PBKDF2 password format used by saved accounts. */
public final class PasswordHasher {
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordHasher() { }

    public static String hash(String password) throws Exception {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password, salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP) + ":"
                + Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verify(String password, String stored) throws Exception {
        if (stored == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;
        byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] savedHash = Base64.decode(parts[1], Base64.NO_WRAP);
        return MessageDigest.isEqual(savedHash, derive(password, salt));
    }

    private static byte[] derive(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
