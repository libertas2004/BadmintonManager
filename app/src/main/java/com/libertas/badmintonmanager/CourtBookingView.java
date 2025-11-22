package com.libertas.badmintonmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourtBookingView extends View {

    private static final int CELL_WIDTH = 120;  // Width của mỗi ô 30 phút
    private static final int CELL_HEIGHT = 80;  // Height của mỗi ô sân
    private static final int HEADER_HEIGHT = 60; // Height của header giờ
    private static final int COURT_NAME_WIDTH = 100; // Width của cột tên sân

    private Paint paintWhite, paintRed, paintGray, paintSelected, paintText, paintBorder;
    private List<TimeSlot> timeSlots = new ArrayList<>();
    private List<TimeSlot> selectedSlots = new ArrayList<>();
    private String selectedDate;
    private float scrollX = 0;
    private float scrollY = 0;
    private float lastTouchX, lastTouchY;

    private int numCourts = 14; // Số lượng sân (1-14)
    private String[] timeLabels; // 6:00, 6:30, 7:00, ..., 22:00

    private OnBookingChangeListener listener;

    public interface OnBookingChangeListener {
        void onSelectionChanged(List<TimeSlot> selectedSlots);
    }

    public CourtBookingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paints
        paintWhite = new Paint();
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStyle(Paint.Style.FILL);

        paintRed = new Paint();
        paintRed.setColor(Color.parseColor("#F44336"));
        paintRed.setStyle(Paint.Style.FILL);

        paintGray = new Paint();
        paintGray.setColor(Color.parseColor("#BDBDBD"));
        paintGray.setStyle(Paint.Style.FILL);

        paintSelected = new Paint();
        paintSelected.setColor(Color.parseColor("#81C784")); // Light green
        paintSelected.setStyle(Paint.Style.FILL);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(32);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBorder = new Paint();
        paintBorder.setColor(Color.parseColor("#E0E0E0"));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2);

        // Generate time labels: 6:00, 6:30, 7:00, ..., 22:00
        generateTimeLabels();

        // Generate all time slots
        generateTimeSlots();
    }

    private void generateTimeLabels() {
        List<String> labels = new ArrayList<>();
        for (int hour = 6; hour <= 22; hour++) {
            labels.add(String.format("%d:00", hour));
            if (hour < 22) {
                labels.add(String.format("%d:30", hour));
            }
        }
        timeLabels = labels.toArray(new String[0]);
    }

    private void generateTimeSlots() {
        timeSlots.clear();
        for (int court = 1; court <= numCourts; court++) {
            for (String time : timeLabels) {
                TimeSlot slot = new TimeSlot("Sân " + court, time, false, false, "");
                timeSlots.add(slot);
            }
        }
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
        updatePastSlots();
        invalidate();
    }

    public void setBookedSlots(List<Booking> bookings) {
        // Reset all slots
        for (TimeSlot slot : timeSlots) {
            slot.setBooked(false);
            slot.setBookedBy("");
        }

        // Mark booked slots
        for (Booking booking : bookings) {
            if (booking.getDate().equals(selectedDate) && !booking.getStatus().equals("cancelled")) {
                markSlotsAsBooked(booking.getCourtName(), booking.getTimeStart(),
                        booking.getTimeEnd(), booking.getCustomerName());
            }
        }

        updatePastSlots();
        invalidate();
    }

    private void markSlotsAsBooked(String courtName, String startTime, String endTime, String userName) {
        for (TimeSlot slot : timeSlots) {
            if (slot.getCourtName().equals(courtName)) {
                String slotTime = slot.getTime();
                if (isTimeBetween(slotTime, startTime, endTime)) {
                    slot.setBooked(true);
                    slot.setBookedBy(userName);
                }
            }
        }
    }

    private boolean isTimeBetween(String time, String start, String end) {
        return time.compareTo(start) >= 0 && time.compareTo(end) < 0;
    }

    private void updatePastSlots() {
        if (selectedDate == null) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date currentDate = new Date();
            Date selectedDateTime = sdf.parse(selectedDate + " 00:00");

            if (selectedDateTime == null) return;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            boolean isSameDay = dateFormat.format(currentDate).equals(selectedDate);

            if (selectedDateTime.before(currentDate) && !isSameDay) {
                // All slots in the past
                for (TimeSlot slot : timeSlots) {
                    slot.setPast(true);
                }
            } else if (isSameDay) {
                // Mark slots before current time as past
                SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
                String currentTime = timeFormat.format(currentDate);

                for (TimeSlot slot : timeSlots) {
                    slot.setPast(slot.getTime().compareTo(currentTime) < 0);
                }
            } else {
                // Future date, no past slots
                for (TimeSlot slot : timeSlots) {
                    slot.setPast(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw court names (fixed column)
        for (int i = 0; i < numCourts; i++) {
            float y = HEADER_HEIGHT + i * CELL_HEIGHT;
            canvas.drawRect(0, y, COURT_NAME_WIDTH, y + CELL_HEIGHT, paintWhite);
            canvas.drawRect(0, y, COURT_NAME_WIDTH, y + CELL_HEIGHT, paintBorder);
            canvas.drawText("Sân " + (i + 1), COURT_NAME_WIDTH / 2f,
                    y + CELL_HEIGHT / 2f + 12, paintText);
        }

        // Draw time headers (fixed row)
        for (int i = 0; i < timeLabels.length; i++) {
            float x = COURT_NAME_WIDTH + i * CELL_WIDTH - scrollX;
            if (x + CELL_WIDTH < COURT_NAME_WIDTH || x > getWidth()) continue;

            canvas.drawRect(x, 0, x + CELL_WIDTH, HEADER_HEIGHT, paintWhite);
            canvas.drawRect(x, 0, x + CELL_WIDTH, HEADER_HEIGHT, paintBorder);
            canvas.drawText(timeLabels[i], x + CELL_WIDTH / 2f, HEADER_HEIGHT / 2f + 12, paintText);
        }

        // Draw time slots grid
        for (int court = 0; court < numCourts; court++) {
            for (int time = 0; time < timeLabels.length; time++) {
                float x = COURT_NAME_WIDTH + time * CELL_WIDTH - scrollX;
                float y = HEADER_HEIGHT + court * CELL_HEIGHT - scrollY;

                // Skip if out of view
                if (x + CELL_WIDTH < COURT_NAME_WIDTH || x > getWidth() ||
                        y + CELL_HEIGHT < HEADER_HEIGHT || y > getHeight()) {
                    continue;
                }

                TimeSlot slot = getSlot(court, time);
                if (slot == null) continue;

                // Choose paint based on slot state
                Paint paint;
                if (selectedSlots.contains(slot)) {
                    paint = paintSelected;
                } else if (slot.isPast()) {
                    paint = slot.isBooked() ? paintGray : paintGray;
                    paint.setAlpha(100); // Make it dimmer
                } else if (slot.isBooked()) {
                    paint = paintRed;
                } else {
                    paint = paintWhite;
                }

                canvas.drawRect(x, y, x + CELL_WIDTH, y + CELL_HEIGHT, paint);
                canvas.drawRect(x, y, x + CELL_WIDTH, y + CELL_HEIGHT, paintBorder);

                // Reset alpha
                if (slot.isPast()) paint.setAlpha(255);
            }
        }
    }

    private TimeSlot getSlot(int courtIndex, int timeIndex) {
        int index = courtIndex * timeLabels.length + timeIndex;
        return index < timeSlots.size() ? timeSlots.get(index) : null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;

                scrollX = Math.max(0, Math.min(scrollX - dx,
                        timeLabels.length * CELL_WIDTH - getWidth() + COURT_NAME_WIDTH));
                scrollY = Math.max(0, Math.min(scrollY - dy,
                        numCourts * CELL_HEIGHT - getHeight() + HEADER_HEIGHT));

                lastTouchX = event.getX();
                lastTouchY = event.getY();
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                // Check if it's a tap (not a scroll)
                if (Math.abs(event.getX() - lastTouchX) < 10 &&
                        Math.abs(event.getY() - lastTouchY) < 10) {
                    handleTap(event.getX(), event.getY());
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleTap(float x, float y) {
        // Skip if tap is on header or court names
        if (x < COURT_NAME_WIDTH || y < HEADER_HEIGHT) return;

        int courtIndex = (int) ((y - HEADER_HEIGHT + scrollY) / CELL_HEIGHT);
        int timeIndex = (int) ((x - COURT_NAME_WIDTH + scrollX) / CELL_WIDTH);

        if (courtIndex < 0 || courtIndex >= numCourts ||
                timeIndex < 0 || timeIndex >= timeLabels.length) return;

        TimeSlot slot = getSlot(courtIndex, timeIndex);
        if (slot == null || slot.isBooked() || slot.isPast()) return;

        // Toggle selection
        if (selectedSlots.contains(slot)) {
            selectedSlots.remove(slot);
        } else {
            selectedSlots.add(slot);
        }

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedSlots));
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setOnBookingChangeListener(OnBookingChangeListener listener) {
        this.listener = listener;
    }

    public void clearSelection() {
        selectedSlots.clear();
        invalidate();
    }

    public List<TimeSlot> getSelectedSlots() {
        return new ArrayList<>(selectedSlots);
    }
}