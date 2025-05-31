package model;

import java.sql.Timestamp; // Or java.time.LocalDateTime

public class Accounts {
    private int accountId;
    private String username;
    // Password hash should generally not be sent to client, but role might be useful
    private String role;
    // OTP fields might not be needed directly in client model unless displaying status
    private String otpCode;
    private Timestamp otpExpiryTime;


    public Accounts() {}

    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public Timestamp getOtpExpiryTime() { return otpExpiryTime; }
    public void setOtpExpiryTime(Timestamp otpExpiryTime) { this.otpExpiryTime = otpExpiryTime; }
}
