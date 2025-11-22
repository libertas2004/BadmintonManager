package com.libertas.badmintonmanager;

import java.util.UUID;

public class Booking {
    private String id;
    private String courtName;
    private String customerName;
    private String timeStart;
    private String timeEnd;
    private String date;
    private int price;
    private String status; // "pending", "paid", "confirmed", "cancelled"
    private long timestamp;

    public Booking(String courtName, String customerName, String timeStart, String timeEnd,
                   String date, int price, String status) {
        this.id = UUID.randomUUID().toString();
        this.courtName = courtName;
        this.customerName = customerName;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.date = date;
        this.price = price;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTimeStart() { return timeStart; }
    public void setTimeStart(String timeStart) { this.timeStart = timeStart; }

    public String getTimeEnd() { return timeEnd; }
    public void setTimeEnd(String timeEnd) { this.timeEnd = timeEnd; }

    public String getTime() { return timeStart + " - " + timeEnd; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatusText() {
        switch (status) {
            case "pending": return "Chưa thanh toán";
            case "paid": return "Đã thanh toán";
            case "confirmed": return "Đã xác nhận";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }
}