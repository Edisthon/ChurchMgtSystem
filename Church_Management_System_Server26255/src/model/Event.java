package model;


import java.sql.Timestamp; // For date and time
import javax.persistence.*;

@Entity
@Table(name = "events") // Assuming table name
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int eventId;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "event_date_time")
    private Timestamp eventDateTime;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public Event() {}

    public Event(String eventName, Timestamp eventDateTime, String location, String description) {
        this.eventName = eventName;
        this.eventDateTime = eventDateTime;
        this.location = location;
        this.description = description;
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Timestamp getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Timestamp eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
