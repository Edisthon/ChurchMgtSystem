package util;

public class UserSession {

    private static UserSession instance;
    private String username;
    private String role;

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

    public void setUser(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void clearSession() {
        this.username = null;
        this.role = null;
    }
}
