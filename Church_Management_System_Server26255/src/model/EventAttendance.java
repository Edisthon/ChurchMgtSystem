package model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "event_attendance") // Assuming table name
public class EventAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private int attendanceId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "check_in_time")
    private Timestamp checkInTime;

    // Constructors
    public EventAttendance() {}

    public EventAttendance(Member member, Event event, Timestamp checkInTime) {
        this.member = member;
        this.event = event;
        this.checkInTime = checkInTime;
    }

    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }
}
