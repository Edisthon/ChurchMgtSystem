package util;

public class UserSession {

    private static UserSession instance;
    private String username;
    private String role;
    private int accountId; // Added accountId field

    private UserSession() {
        // Private constructor to prevent instantiation
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getAccountId() { // Added getter for accountId
        return accountId;
    }

    public void setUser(String username, String role, int accountId) { // Modified setUser
        this.username = username;
        this.role = role;
        this.accountId = accountId;
    }

    public void clearSession() { // Modified clearSession
        this.username = null;
        this.role = null;
        this.accountId = 0; // Reset accountId, assuming 0 is not a valid ID
    }
}
