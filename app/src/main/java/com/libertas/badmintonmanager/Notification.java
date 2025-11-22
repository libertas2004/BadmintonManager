package com.libertas.badmintonmanager;

import java.util.UUID;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String timestamp;
    private boolean isRead;
    private String bookingId;

    public Notification(String title, String message, String timestamp, String bookingId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
        this.bookingId = bookingId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}