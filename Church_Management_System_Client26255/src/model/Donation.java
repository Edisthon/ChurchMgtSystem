package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

// This is a client-side representation. It doesn't need JPA annotations.
// It should mirror the fields of the server-side model.Donation for data transfer.
public class Donation {

    private int donationId;
    private Member member; // Client-side Member model
    private BigDecimal amount;
    private Timestamp donationDate;
    private String notes;
    private Integer eventId;

    // Default constructor
    public Donation() {
    }

    // Optional: Parameterized constructor (useful for testing or manual creation)
    public Donation(int donationId, Member member, BigDecimal amount, Timestamp donationDate, String notes, Integer eventId) {
        this.donationId = donationId;
        this.member = member;
        this.amount = amount;
        this.donationDate = donationDate;
        this.notes = notes;
        this.eventId = eventId;
    }

    // Getters and Setters
    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Timestamp donationDate) {
        this.donationDate = donationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "Donation Client {" +
                "donationId=" + donationId +
                ", member=" + (member != null ? member.getFullName() : "null") +
                ", amount=" + amount +
                ", donationDate=" + donationDate +
                ", notes='" + notes + '\'' +
                ", eventId=" + eventId +
                '}';
    }
}
