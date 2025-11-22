package com.libertas.badmintonmanager;

public class Booking {
    private String courtName;
    private String customerName;
    private String time;
    private String date;
    private int price;
    private String status;

    public Booking(String courtName, String customerName, String time,
                   String date, int price, String status) {
        this.courtName = courtName;
        this.customerName = customerName;
        this.time = time;
        this.date = date;
        this.price = price;
        this.status = status;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}