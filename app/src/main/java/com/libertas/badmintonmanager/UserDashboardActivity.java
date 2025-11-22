package com.libertas.badmintonmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvDate, tvTotalPrice, tvTotalHours;
    private Button btnPayment;
    private ImageView ivLogout;
    private TabLayout tabLayout;
    private CourtBookingView bookingView;
    private ListView lvNotifications;
    private View layoutBooking, layoutNotifications;

    private String username;
    private String selectedDate;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        username = getIntent().getStringExtra("username");
        dataManager = new DataManager(this);

        initViews();
        setupTabLayout();
        setupDateSelector();
        setupBookingView();
        setupLogout();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        btnPayment = findViewById(R.id.btnPayment);
        ivLogout = findViewById(R.id.ivLogout);
        tabLayout = findViewById(R.id.tabLayout);
        bookingView = findViewById(R.id.bookingView);
        lvNotifications = findViewById(R.id.lvNotifications);
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
            bookingView.setSelectedDate(selectedDate);
            loadBookedSlots();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupBookingView() {
        bookingView.setSelectedDate(selectedDate);
        loadBookedSlots();

        bookingView.setOnBookingChangeListener(selectedSlots -> {
            updateTotalPrice(selectedSlots);
        });

        btnPayment.setOnClickListener(v -> processPayment());
    }

    private void loadBookedSlots() {
        List<Booking> bookings = dataManager.getBookings();
        bookingView.setBookedSlots(bookings);
    }

    private void updateTotalPrice(List<TimeSlot> selectedSlots) {
        if (selectedSlots.isEmpty()) {
            tvTotalHours.setText("Tổng giờ: 0h00");
            tvTotalPrice.setText("Tổng tiền: 0 đ");
            return;
        }

        // Each slot is 30 minutes = 30,000 VNĐ
        int totalSlots = selectedSlots.size();
        int hours = totalSlots / 2;
        int minutes = (totalSlots % 2) * 30;
        int totalPrice = totalSlots * 30000;

        tvTotalHours.setText(String.format("Tổng giờ: %dh%02d", hours, minutes));
        tvTotalPrice.setText(String.format("Tổng tiền: %,d đ", totalPrice));
    }

    private void processPayment() {
        List<TimeSlot> selectedSlots = bookingView.getSelectedSlots();

        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group slots by court and find continuous time ranges
        Map<String, List<TimeSlot>> slotsByCourt = new HashMap<>();
        for (TimeSlot slot : selectedSlots) {
            if (!slotsByCourt.containsKey(slot.getCourtName())) {
                slotsByCourt.put(slot.getCourtName(), new java.util.ArrayList<>());
            }
            slotsByCourt.get(slot.getCourtName()).add(slot);
        }

        // For simplicity, use the first court's time range
        String courtName = selectedSlots.get(0).getCourtName();
        String startTime = selectedSlots.get(0).getTime();
        String endTime = getEndTime(selectedSlots.get(selectedSlots.size() - 1).getTime());
        int totalPrice = selectedSlots.size() * 30000;

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("court", courtName);
        intent.putExtra("customer", username);
        intent.putExtra("date", selectedDate);
        intent.putExtra("timeStart", startTime);
        intent.putExtra("timeEnd", endTime);
        intent.putExtra("price", totalPrice);
        startActivity(intent);
    }

    private String getEndTime(String lastSlotTime) {
        // Add 30 minutes to last slot time
        String[] parts = lastSlotTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        minute += 30;
        if (minute >= 60) {
            hour += 1;
            minute -= 60;
        }

        return String.format("%d:%02d", hour, minute);
    }

    private void loadNotifications() {
        List<Notification> notifications = dataManager.getNotifications(username);

        if (notifications.isEmpty()) {
            Toast.makeText(this, "Chưa có thông báo nào", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationAdapter adapter = new NotificationAdapter(this, notifications, notification -> {
            dataManager.markNotificationAsRead(username, notification.getId());
            loadNotifications();
        });

        lvNotifications.setAdapter(adapter);
    }

    private void setupLogout() {
        ivLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookedSlots();
    }
}