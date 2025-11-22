package com.libertas.badmintonmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;



public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvDate, tvTotalPrice;
    private Button btnPayment;
    private ImageView ivLogout;
    private TabLayout tabLayout;
    private RecyclerView rvTimeSlots;
    private View layoutBooking, layoutNotifications;

    private String username;
    private String selectedDate;
    private DataManager dataManager;
    private List<TimeSlot> selectedSlots = new ArrayList<>();
    private TimeSlotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        username = getIntent().getStringExtra("username");
        dataManager = new DataManager(this);

        initViews();
        setupTabLayout();
        setupDateSelector();
        setupTimeSlots();
        setupLogout();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPayment = findViewById(R.id.btnPayment);
        ivLogout = findViewById(R.id.ivLogout);
        tabLayout = findViewById(R.id.tabLayout);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        layoutBooking = findViewById(R.id.layoutBooking);
        layoutNotifications = findViewById(R.id.layoutNotifications);

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        tvDate.setText(selectedDate);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Đặt sân"));
        tabLayout.addTab(tabLayout.newTab().setText("Thông báo"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutBooking.setVisibility(View.VISIBLE);
                    layoutNotifications.setVisibility(View.GONE);
                } else {
                    layoutBooking.setVisibility(View.GONE);
                    layoutNotifications.setVisibility(View.VISIBLE);
                    loadNotifications();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDateSelector() {
        tvDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
            tvDate.setText(selectedDate);
            loadBookedSlots();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTimeSlots() {
        List<TimeSlot> allSlots = generateTimeSlots();
        adapter = new TimeSlotAdapter(allSlots, this::onSlotSelected);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 15); // 15 courts
        rvTimeSlots.setLayoutManager(layoutManager);
        rvTimeSlots.setAdapter(adapter);

        loadBookedSlots();

        btnPayment.setOnClickListener(v -> processPayment());
    }

    private List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        String[] times = {"14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00",
                "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00"};

        for (int court = 1; court <= 14; court++) {
            for (String time : times) {
                slots.add(new TimeSlot("Sân " + court, time, false, false, ""));
            }
        }
        return slots;
    }

    private void loadBookedSlots() {
        List<Booking> bookings = dataManager.getBookings();
        List<TimeSlot> allSlots = adapter.getSlots();

        // Reset all slots
        for (TimeSlot slot : allSlots) {
            slot.setBooked(false);
            slot.setPast(false);
        }

        // Mark booked slots
        for (Booking booking : bookings) {
            if (booking.getDate().equals(selectedDate) && !booking.getStatus().equals("cancelled")) {
                String startTime = booking.getTimeStart();
                String endTime = booking.getTimeEnd();
                markSlotsAsBooked(allSlots, booking.getCourtName(), startTime, endTime,
                        booking.getStatus().equals("confirmed") ? booking.getCustomerName() : "");
            }
        }

        // Mark past time slots
        markPastSlots(allSlots);

        adapter.notifyDataSetChanged();
    }

    private void markSlotsAsBooked(List<TimeSlot> slots, String court, String start, String end, String userName) {
        for (TimeSlot slot : slots) {
            if (slot.getCourtName().equals(court)) {
                String slotTime = slot.getTime();
                if (isTimeBetween(slotTime, start, end)) {
                    slot.setBooked(true);
                    slot.setBookedBy(userName);
                }
            }
        }
    }

    private void markPastSlots(List<TimeSlot> slots) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date currentDate = new Date();
            Date selectedDateTime = sdf.parse(selectedDate + " 00:00");

            if (selectedDateTime != null && selectedDateTime.before(currentDate)) {
                for (TimeSlot slot : slots) {
                    slot.setPast(true);
                }
            } else if (selectedDateTime != null && isSameDay(selectedDateTime, currentDate)) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = timeFormat.format(currentDate);

                for (TimeSlot slot : slots) {
                    if (slot.getTime().compareTo(currentTime) < 0) {
                        slot.setPast(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private boolean isTimeBetween(String time, String start, String end) {
        return time.compareTo(start) >= 0 && time.compareTo(end) < 0;
    }

    private void onSlotSelected(TimeSlot slot, boolean isSelected) {
        if (isSelected) {
            selectedSlots.add(slot);
        } else {
            selectedSlots.remove(slot);
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        int totalHours = calculateTotalHours();
        int totalPrice = totalHours * 60000; // 60,000 VNĐ per hour
        tvTotalPrice.setText("Tổng tiền: " + String.format("%,d đ", totalPrice));
    }

    private int calculateTotalHours() {
        // Calculate based on 30-minute slots
        return selectedSlots.size() / 2;
    }

    private void processPayment() {
        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group slots by court
        // ... (simplified for brevity)

        int totalPrice = calculateTotalHours() * 60000;
        String timeRange = getTimeRange();

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("court", selectedSlots.get(0).getCourtName());
        intent.putExtra("customer", username);
        intent.putExtra("date", selectedDate);
        intent.putExtra("time", timeRange);
        intent.putExtra("price", totalPrice);
        startActivity(intent);
    }

    private String getTimeRange() {
        if (selectedSlots.isEmpty()) return "";
        String start = selectedSlots.get(0).getTime();
        String end = selectedSlots.get(selectedSlots.size() - 1).getTime();
        return start + " - " + end;
    }

    private void loadNotifications() {
        // Load notifications from DataManager
        // Display in RecyclerView
    }

    private void setupLogout() {
        ivLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}