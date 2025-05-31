package model;

import java.sql.Timestamp;

public class EventAttendance {
    private int attendanceId;
    private Member member; // Or just memberId if full object isn't always needed
    private Event event;   // Or just eventId
    private Timestamp checkInTime;

    public EventAttendance() {}

    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }
}
