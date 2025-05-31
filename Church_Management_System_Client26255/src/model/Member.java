package model;

import java.sql.Date; // Or java.time.LocalDate if preferred and handled by Jackson

public class Member {
    private int memberId;
    private String fullName;
    private String gender;
    private String phoneNumber;
    private Date birthdate; // Ensure Jackson can handle this (with jsr310 module)
    private Integer groupId; // Use Integer for nullable groupId

    // Default constructor (needed by Jackson)
    public Member() {}

    // Optional: Constructor with fields
    public Member(int memberId, String fullName, String gender, String phoneNumber, Date birthdate, Integer groupId) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
        this.groupId = groupId;
    }

    // Getters and Setters for all fields (needed by Jackson and for UI binding)
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Date getBirthdate() { return birthdate; }
    public void setBirthdate(Date birthdate) { this.birthdate = birthdate; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    @Override
    public String toString() { // For debugging or simple display
        return fullName + " (ID: " + memberId + ")";
    }
}
