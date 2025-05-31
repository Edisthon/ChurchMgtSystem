package com.churchsystem.client.util; // Placing in util package

public class SessionManager {
    private static String currentToken = null;

    public static void setCurrentToken(String token) {
        currentToken = token;
    }

    public static String getCurrentToken() {
        return currentToken;
    }

    public static boolean hasToken() {
        return currentToken != null && !currentToken.isEmpty();
    }

    public static void clearCurrentToken() {
        currentToken = null;
    }
}
