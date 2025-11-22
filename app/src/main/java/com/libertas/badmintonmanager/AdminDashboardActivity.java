package com.libertas.badmintonmanager;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView lvBookings;
    private TextView tvTotalRevenue;
    private List<Booking> bookingList;
    private BookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        String username = getIntent().getStringExtra("username");
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Chào mừng Admin: " + username);

        lvBookings = findViewById(R.id.lvBookings);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);

        loadBookings();

        lvBookings.setOnItemClickListener((parent, view, position, id) -> {
            Booking booking = bookingList.get(position);
            showBookingDetails(booking);
        });
    }

    private void loadBookings() {
        bookingList = new ArrayList<>();
        // Dữ liệu mẫu
        bookingList.add(new Booking("Sân 1", "Nguyễn Văn A", "08:00 - 10:00", "01/12/2024", 200000, "Đã thanh toán"));
        bookingList.add(new Booking("Sân 2", "Trần Thị B", "10:00 - 12:00", "01/12/2024", 200000, "Chờ thanh toán"));
        bookingList.add(new Booking("Sân 3", "Lê Văn C", "14:00 - 16:00", "01/12/2024", 200000, "Đã thanh toán"));

        adapter = new BookingAdapter(this, bookingList);
        lvBookings.setAdapter(adapter);

        calculateRevenue();
    }

    private void calculateRevenue() {
        int total = 0;
        for (Booking b : bookingList) {
            if (b.getStatus().equals("Đã thanh toán")) {
                total += b.getPrice();
            }
        }
        tvTotalRevenue.setText("Tổng doanh thu: " + formatPrice(total));
    }

    private void showBookingDetails(Booking booking) {
        String details = "Sân: " + booking.getCourtName() + "\n" +
                "Khách hàng: " + booking.getCustomerName() + "\n" +
                "Thời gian: " + booking.getTime() + "\n" +
                "Ngày: " + booking.getDate() + "\n" +
                "Giá: " + formatPrice(booking.getPrice()) + "\n" +
                "Trạng thái: " + booking.getStatus();

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết đặt sân")
                .setMessage(details)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private String formatPrice(int price) {
        return String.format("%,d VNĐ", price);
    }
}
