package model;

import javax.persistence.*;
import java.sql.Date; // Using java.sql.Date as per original model

@Entity
@Table(name = "members") // Assuming table name is 'members'
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming auto-incrementing ID
    @Column(name = "member_id")
    private int memberId;

    @Column(name = "full_name", nullable = false, length = 255) // Assuming max length and not nullable
    private String fullName;

    @Column(length = 10) // Assuming gender can be 'Male', 'Female', 'Other'
    private String gender;

    @Column(name = "phone", length = 20) // Assuming phone number max length
    private String phoneNumber;

    @Column(name = "birth_date")
    private Date birthdate; // java.sql.Date is suitable for DATE columns

    @Column(name = "group_id") // This suggests a relationship, will handle later if Groups entity is created
    private Integer groupId; // Using Integer to allow null if a member is not in a group

    // Constructors
    public Member() {}

    public Member(String fullName, String gender, String phoneNumber, Date birthdate, Integer groupId) {
        this.fullName = fullName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
        this.groupId = groupId;
    }

    // Getters and Setters
    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Member{" +
               "memberId=" + memberId +
               ", fullName='" + fullName + '\'' +
               ", gender='" + gender + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", birthdate=" + birthdate +
               ", groupId=" + groupId +
               '}';
    }
}
