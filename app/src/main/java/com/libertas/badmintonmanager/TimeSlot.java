package com.libertas.badmintonmanager;

public class TimeSlot {
    private String courtName;
    private String time;
    private boolean isBooked;
    private boolean isPast;
    private boolean isSelected;
    private String bookedBy;

    public TimeSlot(String courtName, String time, boolean isBooked, boolean isPast, String bookedBy) {
        this.courtName = courtName;
        this.time = time;
        this.isBooked = isBooked;
        this.isPast = isPast;
        this.bookedBy = bookedBy;
        this.isSelected = false;
    }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    public boolean isPast() { return isPast; }
    public void setPast(boolean past) { isPast = past; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) obj;
        return courtName.equals(timeSlot.courtName) && time.equals(timeSlot.time);
    }

    @Override
    public int hashCode() {
        return (courtName + time).hashCode();
    }
}