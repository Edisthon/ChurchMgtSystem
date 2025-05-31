package model;

import javax.persistence.*;

@Entity
@Table(name = "accounts") // Assuming table name
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int accountId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255) // Storing hashed password
    private String passwordHash;

    @Column(name = "role", length = 50) // e.g., 'ADMIN', 'USER'
    private String role;

    // Assuming a link to a Member, if applicable
    // @ManyToOne
    // @JoinColumn(name = "member_id")
    // private Member member;

    // OTP related fields
    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_expiry_time")
    private java.sql.Timestamp otpExpiryTime;


    // Constructors
    public Accounts() {}

    public Accounts(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public java.sql.Timestamp getOtpExpiryTime() {
        return otpExpiryTime;
    }

    public void setOtpExpiryTime(java.sql.Timestamp otpExpiryTime) {
        this.otpExpiryTime = otpExpiryTime;
    }

    // public Member getMember() {
    //     return member;
    // }

    // public void setMember(Member member) {
    //     this.member = member;
    // }
}
