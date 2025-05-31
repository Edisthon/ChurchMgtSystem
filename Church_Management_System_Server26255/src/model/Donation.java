package model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id")
    private int donationId;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY to avoid loading member unless needed
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "donation_date", nullable = false)
    private Timestamp donationDate;

    @Column(name = "notes", length = 255)
    private String notes; // e.g., Tithe, Offering, Building Fund, specific campaign

    @Column(name = "event_id", nullable = true)
    private Integer eventId; // Optional: if donation is for a specific event

    // Constructors
    public Donation() {
    }

    public Donation(Member member, BigDecimal amount, Timestamp donationDate, String notes, Integer eventId) {
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
        return "Donation{" +
                "donationId=" + donationId +
                ", memberId=" + (member != null ? member.getMemberId() : "null") + // Avoid NPE if member is null
                ", amount=" + amount +
                ", donationDate=" + donationDate +
                ", notes='" + notes + '\'' +
                ", eventId=" + eventId +
                '}';
    }
}
