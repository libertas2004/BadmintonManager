package com.libertas.badmintonmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalRevenue, tvDate;
    private ImageView ivLogout;
    private TabLayout tabLayout;
    private RecyclerView rvBookings, rvTimeSlots;
    private View layoutBookingList, layoutCourtManagement;

    private String username;
    private String selectedDate;
    private DataManager dataManager;
    private BookingListAdapter bookingAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        username = getIntent().getStringExtra("username");
        String fullName = getIntent().getStringExtra("fullName");
        dataManager = new DataManager(this);

        initViews();
        setupTabLayout();
        setupLogout();

        tvWelcome.setText("Chào Admin: " + fullName);

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        tvDate.setText(selectedDate);

        loadBookings();
        updateRevenue();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvDate = findViewById(R.id.tvDate);
        ivLogout = findViewById(R.id.ivLogout);
        tabLayout = findViewById(R.id.tabLayout);
        rvBookings = findViewById(R.id.rvBookings);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        layoutBookingList = findViewById(R.id.layoutBookingList);
        layoutCourtManagement = findViewById(R.id.layoutCourtManagement);
    }

    private void setupTabLayout() {
        TabLayout.Tab tab1 = tabLayout.newTab().setText("Danh sách đặt sân");
        TabLayout.Tab tab2 = tabLayout.newTab().setText("Quản lý sân");

        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);

        // Update badge for unread notifications
        updateNotificationBadge(tab1);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutBookingList.setVisibility(View.VISIBLE);
                    layoutCourtManagement.setVisibility(View.GONE);
                    loadBookings();
                } else {
                    layoutBookingList.setVisibility(View.GONE);
                    layoutCourtManagement.setVisibility(View.VISIBLE);
                    loadCourtManagement();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateNotificationBadge(TabLayout.Tab tab) {
        int unreadCount = getUnreadBookingCount();
        if (unreadCount > 0) {
            BadgeDrawable badge = tab.getOrCreateBadge();
            badge.setNumber(unreadCount);
            badge.setVisible(true);
        }
    }

    private int getUnreadBookingCount() {
        List<Booking> bookings = dataManager.getBookings();
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus().equals("pending")) {
                count++;
            }
        }
        return count;
    }

    private void loadBookings() {
        List<Booking> bookings = dataManager.getBookings();

        bookingAdapter = new BookingListAdapter(bookings, new BookingListAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking) {
                showBookingDialog(booking);
            }
        });

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(bookingAdapter);
    }

    private void showBookingDialog(Booking booking) {
        String details = "Sân: " + booking.getCourtName() + "\n" +
                "Khách hàng: " + booking.getCustomerName() + "\n" +
                "Thời gian: " + booking.getTime() + "\n" +
                "Ngày: " + booking.getDate() + "\n" +
                "Giá: " + String.format("%,d VNĐ", booking.getPrice()) + "\n" +
                "Trạng thái: " + booking.getStatusText();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đặt sân")
                .setMessage(details);

        // Add payment button if not paid yet
        if (!booking.getStatus().equals("paid") && !booking.getStatus().equals("confirmed")) {
            builder.setNeutralButton("Đã thanh toán", (dialog, which) -> {
                booking.setStatus("paid");
                dataManager.updateBooking(booking);

                // Send notification to user
                Notification notification = new Notification(
                        "Xác nhận thanh toán",
                        "Đơn đặt sân " + booking.getCourtName() + " ngày " + booking.getDate() + " đã được xác nhận thanh toán.",
                        new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()),
                        booking.getId()
                );
                dataManager.addNotification(booking.getCustomerName(), notification);

                loadBookings();
                updateRevenue();
                Toast.makeText(this, "Đã xác nhận thanh toán", Toast.LENGTH_SHORT).show();
            });
        }

        // Add reject button
        builder.setNegativeButton("Không nhận", (dialog, which) -> {
            booking.setStatus("cancelled");
            dataManager.updateBooking(booking);

            // Send notification to user
            Notification notification = new Notification(
                    "Đặt sân bị hủy",
                    "Đơn đặt sân " + booking.getCourtName() + " ngày " + booking.getDate() + " đã bị hủy.",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()),
                    booking.getId()
            );
            dataManager.addNotification(booking.getCustomerName(), notification);

            loadBookings();
            Toast.makeText(this, "Đã hủy đơn đặt sân", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    private void updateRevenue() {
        List<Booking> bookings = dataManager.getBookings();
        int total = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus().equals("paid") || booking.getStatus().equals("confirmed")) {
                total += booking.getPrice();
            }
        }
        tvTotalRevenue.setText("Tổng doanh thu: " + String.format("%,d VNĐ", total));
    }

    private void loadCourtManagement() {
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
                tvDate.setText(selectedDate);
                loadCourtTimeSlots();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        loadCourtTimeSlots();
    }

    private void loadCourtTimeSlots() {
        List<TimeSlot> allSlots = generateTimeSlots();

        // Load bookings for selected date
        List<Booking> bookings = dataManager.getBookings();
        for (Booking booking : bookings) {
            if (booking.getDate().equals(selectedDate) && !booking.getStatus().equals("cancelled")) {
                String userName = booking.getStatus().equals("confirmed") ? booking.getCustomerName() : "";
                markSlotsAsBooked(allSlots, booking.getCourtName(),
                        booking.getTimeStart(), booking.getTimeEnd(), userName);
            }
        }

        // Mark past slots
        markPastSlots(allSlots);

        timeSlotAdapter = new TimeSlotAdapter(allSlots, null);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 15);
        rvTimeSlots.setLayoutManager(layoutManager);
        rvTimeSlots.setAdapter(timeSlotAdapter);
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
        loadBookings();
        updateRevenue();
    }
}