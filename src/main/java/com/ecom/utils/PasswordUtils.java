package com.ecom.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    
    /**
     * Hash a password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    /**
     * Verify a password against a hash
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

