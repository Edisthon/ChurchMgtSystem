package model;

import java.sql.Timestamp;

public class Event {
    private int eventId;
    private String eventName;
    private Timestamp eventDateTime;
    private String location;
    private String description;

    public Event() {}

    // Getters and Setters
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public Timestamp getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(Timestamp eventDateTime) { this.eventDateTime = eventDateTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
