package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {

    // Basic SHA-256 hashing (BCrypt or SCrypt are recommended for production)
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            // Consider throwing IllegalArgumentException for better error handling upstream
            System.err.println("PasswordUtil: Plain password cannot be null or empty for hashing.");
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // This should ideally not happen with SHA-256, which is a standard algorithm
            e.printStackTrace();
            // In a real application, this might be a fatal error or require specific handling
            throw new RuntimeException("Error hashing password due to missing SHA-256 algorithm.", e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            System.err.println("PasswordUtil: Plain password or hashed password cannot be null or empty for checking.");
            return false;
        }
        String hashedAttempt = hashPassword(plainPassword);
        if (hashedAttempt == null) { // hashPassword might return null if plainPassword was invalid (though checked above)
             return false;
        }
        return hashedPassword.equals(hashedAttempt);
    }
}
