package com.speedtyping.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cryptographic utility providing secure password hashing and verification
 * using PBKDF2 with HMAC-SHA256 and unique cryptographic salts.
 */
public class SecurityUtils {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16;  // bytes
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2$";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a password using PBKDF2 with HMAC-SHA256 and a random cryptographic salt.
     * Output format: PBKDF2$&lt;iterations&gt;$&lt;base64-salt&gt;$&lt;base64-hash&gt;
     *
     * @param password the plaintext password to hash
     * @return the formatted salt and hash string
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        return PREFIX + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a raw plaintext password against a stored password string.
     * Supports both PBKDF2 hashed passwords and legacy plaintext passwords for backward compatibility.
     *
     * @param rawPassword the user-entered plaintext password
     * @param storedPassword the password stored in the database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith(PREFIX)) {
            String[] parts = storedPassword.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

                byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
                return MessageDigest.isEqual(expectedHash, actualHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Constant-time comparison fallback for legacy plaintext passwords
        return MessageDigest.isEqual(rawPassword.getBytes(), storedPassword.getBytes());
    }

    /**
     * Checks if a stored password is in the PBKDF2 format or is legacy unhashed text.
     *
     * @param storedPassword the password string from the database
     * @return true if already hashed with PBKDF2, false if legacy
     */
    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while computing PBKDF2 hash: " + e.getMessage(), e);
        }
    }
}
