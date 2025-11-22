package com.libertas.badmintonmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalRevenue, tvDate;
    private ImageView ivLogout;
    private TabLayout tabLayout;
    private RecyclerView rvBookings;
    private CourtBookingView bookingView;
    private View layoutBookingList, layoutCourtManagement;

    private String username;
    private String selectedDate;
    private DataManager dataManager;
    private BookingListAdapter bookingAdapter;

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
        bookingView = findViewById(R.id.bookingView);
        layoutBookingList = findViewById(R.id.layoutBookingList);
        layoutCourtManagement = findViewById(R.id.layoutCourtManagement);
    }

    private void setupTabLayout() {
        TabLayout.Tab tab1 = tabLayout.newTab().setText("Danh sách đặt sân");
        TabLayout.Tab tab2 = tabLayout.newTab().setText("Quản lý sân");

        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);

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
        String details = "════════════════════════\n" +
                "      CHI TIẾT ĐẶT SÂN\n" +
                "════════════════════════\n\n" +
                "Sân: " + booking.getCourtName() + "\n" +
                "Khách hàng: " + booking.getCustomerName() + "\n" +
                "Ngày: " + booking.getDate() + "\n" +
                "Giờ: " + booking.getTime() + "\n" +
                "Giá: " + String.format("%,d VNĐ", booking.getPrice()) + "\n" +
                "Trạng thái: " + booking.getStatusText();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đặt sân")
                .setMessage(details);

        // Add payment button if not paid yet
        if (!booking.getStatus().equals("paid") &&
                !booking.getStatus().equals("confirmed") &&
                !booking.getStatus().equals("cancelled")) {
            builder.setNeutralButton("Đã thanh toán", (dialog, which) -> {
                booking.setStatus("confirmed");
                dataManager.updateBooking(booking);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String timestamp = sdf.format(new Date());

                Notification notification = new Notification(
                        "Xác nhận thanh toán",
                        "Đơn đặt " + booking.getCourtName() + " ngày " + booking.getDate() +
                                " lúc " + booking.getTime() + " đã được xác nhận thanh toán.",
                        timestamp,
                        booking.getId()
                );
                dataManager.addNotification(booking.getCustomerName(), notification);

                loadBookings();
                updateRevenue();

                TabLayout.Tab tab = tabLayout.getTabAt(0);
                if (tab != null) updateNotificationBadge(tab);

                Toast.makeText(this, "Đã xác nhận thanh toán", Toast.LENGTH_SHORT).show();
            });
        }

        // Add reject button
        if (!booking.getStatus().equals("cancelled")) {
            builder.setNegativeButton("Không nhận", (dialog, which) -> {
                booking.setStatus("cancelled");
                dataManager.updateBooking(booking);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String timestamp = sdf.format(new Date());

                Notification notification = new Notification(
                        "Đặt sân bị hủy",
                        "Đơn đặt " + booking.getCourtName() + " ngày " + booking.getDate() +
                                " lúc " + booking.getTime() + " đã bị hủy bởi admin.",
                        timestamp,
                        booking.getId()
                );
                dataManager.addNotification(booking.getCustomerName(), notification);

                loadBookings();

                TabLayout.Tab tab = tabLayout.getTabAt(0);
                if (tab != null) updateNotificationBadge(tab);

                Toast.makeText(this, "Đã hủy đơn đặt sân", Toast.LENGTH_SHORT).show();
            });
        }

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
                updateCourtView();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        bookingView.setSelectable(false); // Admin can only view, not select
        updateCourtView();
    }

    private void updateCourtView() {
        bookingView.setSelectedDate(selectedDate);
        List<Booking> bookings = dataManager.getBookings();
        bookingView.setBookedSlots(bookings);
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

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) updateNotificationBadge(tab);
    }
}