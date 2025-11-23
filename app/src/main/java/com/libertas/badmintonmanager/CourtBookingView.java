package com.libertas.badmintonmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourtBookingView extends View {

    private static final int CELL_WIDTH = 120;
    private static final int CELL_HEIGHT = 80;
    private static final int HEADER_HEIGHT = 70;
    private static final int COURT_NAME_WIDTH = 100;

    private Paint paintWhite, paintRed, paintGray, paintSelected, paintText, paintBorder, paintHeader;
    private List<TimeSlot> timeSlots = new ArrayList<>();
    private List<TimeSlot> selectedSlots = new ArrayList<>();
    private String selectedDate;
    private float scrollX = 0;
    private float scrollY = 0;
    private float lastTouchX, lastTouchY;
    private boolean isSelectable = true;
    private boolean isDragging = false;

    private int numCourts = 14;
    private String[] timeLabels;

    private OnBookingChangeListener listener;

    public interface OnBookingChangeListener {
        void onSelectionChanged(List<TimeSlot> selectedSlots);
    }

    public CourtBookingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
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
        paintSelected.setColor(Color.parseColor("#81C784"));
        paintSelected.setStyle(Paint.Style.FILL);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(28);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintHeader = new Paint();
        paintHeader.setColor(Color.parseColor("#E8F5E9"));
        paintHeader.setStyle(Paint.Style.FILL);

        paintBorder = new Paint();
        paintBorder.setColor(Color.parseColor("#E0E0E0"));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2);

        generateTimeLabels();
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

    public void setSelectable(boolean selectable) {
        this.isSelectable = selectable;
    }

    public void setBookedSlots(List<Booking> bookings) {
        for (TimeSlot slot : timeSlots) {
            slot.setBooked(false);
            slot.setBookedBy("");
        }

        for (Booking booking : bookings) {
            if (booking.getDate().equals(selectedDate) && !booking.getStatus().equals("cancelled")) {
                String userName = booking.getStatus().equals("confirmed") ? booking.getCustomerName() : "";
                markSlotsAsBooked(booking.getCourtName(), booking.getTimeStart(),
                        booking.getTimeEnd(), userName);
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date selectedDateTime = sdf.parse(selectedDate);
            Date currentDate = new Date();

            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String selectedDateStr = dateOnlyFormat.format(selectedDateTime);
            String currentDateStr = dateOnlyFormat.format(currentDate);

            if (selectedDateStr.equals(currentDateStr)) {
                // Same day - mark past time slots
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = timeFormat.format(currentDate);

                for (TimeSlot slot : timeSlots) {
                    String slotTime = formatTimeToHHmm(slot.getTime());
                    slot.setPast(slotTime.compareTo(currentTime) <= 0);
                }
            } else if (selectedDateTime.before(currentDate)) {
                // Past date - all slots are past
                for (TimeSlot slot : timeSlots) {
                    slot.setPast(true);
                }
            } else {
                // Future date - no past slots
                for (TimeSlot slot : timeSlots) {
                    slot.setPast(false);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String formatTimeToHHmm(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return String.format("%02d:%02d", hour, minute);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // PANEL 1: Fixed top-left corner
        canvas.drawRect(0, 0, COURT_NAME_WIDTH, HEADER_HEIGHT, paintHeader);
        canvas.drawRect(0, 0, COURT_NAME_WIDTH, HEADER_HEIGHT, paintBorder);

        // PANEL 2: Time headers (fixed vertically, scrollable horizontally)
        canvas.save();
        canvas.clipRect(COURT_NAME_WIDTH, 0, getWidth(), HEADER_HEIGHT);

        for (int i = 0; i < timeLabels.length; i++) {
            float x = COURT_NAME_WIDTH + i * CELL_WIDTH - scrollX;

            canvas.drawRect(x, 0, x + CELL_WIDTH, HEADER_HEIGHT, paintHeader);
            canvas.drawRect(x, 0, x + CELL_WIDTH, HEADER_HEIGHT, paintBorder);
            canvas.drawText(timeLabels[i], x + CELL_WIDTH / 2f, HEADER_HEIGHT / 2f + 10, paintText);
        }

        canvas.restore();

        // PANEL 3: Court names (fixed horizontally, scrollable vertically)
        canvas.save();
        canvas.clipRect(0, HEADER_HEIGHT, COURT_NAME_WIDTH, getHeight());

        for (int i = 0; i < numCourts; i++) {
            float y = HEADER_HEIGHT + i * CELL_HEIGHT - scrollY;

            canvas.drawRect(0, y, COURT_NAME_WIDTH, y + CELL_HEIGHT, paintHeader);
            canvas.drawRect(0, y, COURT_NAME_WIDTH, y + CELL_HEIGHT, paintBorder);
            canvas.drawText("Sân " + (i + 1), COURT_NAME_WIDTH / 2f,
                    y + CELL_HEIGHT / 2f + 10, paintText);
        }

        canvas.restore();

        // PANEL 4: Time slots grid (scrollable both directions)
        canvas.save();
        canvas.clipRect(COURT_NAME_WIDTH, HEADER_HEIGHT, getWidth(), getHeight());

        for (int court = 0; court < numCourts; court++) {
            for (int time = 0; time < timeLabels.length; time++) {
                float x = COURT_NAME_WIDTH + time * CELL_WIDTH - scrollX;
                float y = HEADER_HEIGHT + court * CELL_HEIGHT - scrollY;

                TimeSlot slot = getSlot(court, time);
                if (slot == null) continue;

                Paint paint;
                if (selectedSlots.contains(slot)) {
                    paint = paintSelected;
                } else if (slot.isPast()) {
                    paint = paintGray;
                    paint.setAlpha(100);
                } else if (slot.isBooked()) {
                    paint = paintRed;
                } else {
                    paint = paintWhite;
                }

                canvas.drawRect(x, y, x + CELL_WIDTH, y + CELL_HEIGHT, paint);
                canvas.drawRect(x, y, x + CELL_WIDTH, y + CELL_HEIGHT, paintBorder);

                if (slot.isPast()) paint.setAlpha(255);
            }
        }

        canvas.restore();
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
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;

                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    isDragging = true;
                }

                // Only scroll if in the scrollable area (not in fixed panels)
                if (lastTouchX > COURT_NAME_WIDTH && lastTouchY > HEADER_HEIGHT) {
                    // In grid area - can scroll both directions
                    float maxScrollX = Math.max(0, timeLabels.length * CELL_WIDTH - (getWidth() - COURT_NAME_WIDTH));
                    float maxScrollY = Math.max(0, numCourts * CELL_HEIGHT - (getHeight() - HEADER_HEIGHT));

                    scrollX = Math.max(0, Math.min(scrollX - dx, maxScrollX));
                    scrollY = Math.max(0, Math.min(scrollY - dy, maxScrollY));
                } else if (lastTouchX > COURT_NAME_WIDTH) {
                    // In time header area - only scroll horizontally
                    float maxScrollX = Math.max(0, timeLabels.length * CELL_WIDTH - (getWidth() - COURT_NAME_WIDTH));
                    scrollX = Math.max(0, Math.min(scrollX - dx, maxScrollX));
                } else if (lastTouchY > HEADER_HEIGHT) {
                    // In court name area - only scroll vertically
                    float maxScrollY = Math.max(0, numCourts * CELL_HEIGHT - (getHeight() - HEADER_HEIGHT));
                    scrollY = Math.max(0, Math.min(scrollY - dy, maxScrollY));
                }

                lastTouchX = event.getX();
                lastTouchY = event.getY();
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (isSelectable && !isDragging) {
                    handleTap(event.getX(), event.getY());
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleTap(float x, float y) {
        if (x < COURT_NAME_WIDTH || y < HEADER_HEIGHT) return;

        int courtIndex = (int) ((y - HEADER_HEIGHT + scrollY) / CELL_HEIGHT);
        int timeIndex = (int) ((x - COURT_NAME_WIDTH + scrollX) / CELL_WIDTH);

        if (courtIndex < 0 || courtIndex >= numCourts ||
                timeIndex < 0 || timeIndex >= timeLabels.length) return;

        TimeSlot slot = getSlot(courtIndex, timeIndex);
        if (slot == null || slot.isBooked() || slot.isPast()) return;

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
        List<TimeSlot> sorted = new ArrayList<>(selectedSlots);
        Collections.sort(sorted, new Comparator<TimeSlot>() {
            @Override
            public int compare(TimeSlot s1, TimeSlot s2) {
                int courtCompare = s1.getCourtName().compareTo(s2.getCourtName());
                if (courtCompare != 0) return courtCompare;
                return s1.getTime().compareTo(s2.getTime());
            }
        });
        return sorted;
    }
}