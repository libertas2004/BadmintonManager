package com.libertas.badmintonmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dataManager = new DataManager(this);

        String court = getIntent().getStringExtra("court");
        String customer = getIntent().getStringExtra("customer");
        String fullName = getIntent().getStringExtra("fullName");
        String date = getIntent().getStringExtra("date");
        String timeStart = getIntent().getStringExtra("timeStart");
        String timeEnd = getIntent().getStringExtra("timeEnd");
        String totalHours = getIntent().getStringExtra("totalHours");
        int price = getIntent().getIntExtra("price", 0);

        if (fullName == null) fullName = customer;

        TextView tvPaymentInfo = findViewById(R.id.tvPaymentInfo);
        ImageView ivQRCode = findViewById(R.id.ivQRCode);
        Button btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        String info = "═══════════════════════════\n" +
                "      THÔNG TIN THANH TOÁN\n" +
                "═══════════════════════════\n\n" +
                "Chủ sân: Trần Văn Linh\n" +
                "Email: libertas.infor@gmail.com\n" +
                "SĐT: 0904.620.940\n\n" +
                "───────────────────────────\n" +
                "     THÔNG TIN ĐẶT SÂN\n" +
                "───────────────────────────\n\n" +
                "Sân: " + court + "\n" +
                "Khách hàng: " + fullName + "\n" +
                "Username: " + customer + "\n" +
                "Ngày: " + date + "\n" +
                "Khung giờ: " + timeStart + " - " + timeEnd + "\n" +
                "Tổng thời gian: " + totalHours + "\n\n" +
                "═══════════════════════════\n" +
                "Số tiền: " + String.format("%,d VNĐ", price) + "\n" +
                "═══════════════════════════\n\n" +
                "Vui lòng quét mã QR để thanh toán";

        tvPaymentInfo.setText(info);
        ivQRCode.setImageResource(R.drawable.qr_payment);

        final String finalFullName = fullName;
        btnConfirmPayment.setOnClickListener(v -> {
            Booking booking = new Booking(court, customer, timeStart, timeEnd,
                    date, price, "pending");
            dataManager.addBooking(booking);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Send notification to admin
            Notification adminNotification = new Notification(
                    "Đặt sân mới",
                    finalFullName + " (" + customer + ") đã đặt " + court +
                            " vào " + date + " lúc " + timeStart + " - " + timeEnd +
                            ". Tổng tiền: " + String.format("%,d VNĐ", price),
                    timestamp,
                    booking.getId()
            );
            dataManager.addNotification("admin", adminNotification);

            // Send confirmation to user
            Notification userNotification = new Notification(
                    "Đặt sân thành công",
                    "Bạn đã đặt " + court + " vào " + date + " lúc " + timeStart + " - " + timeEnd +
                            ". Tổng tiền: " + String.format("%,d VNĐ", price) +
                            ". Vui lòng thanh toán qua QR code và chờ admin xác nhận.",
                    timestamp,
                    booking.getId()
            );
            dataManager.addNotification(customer, userNotification);

            Toast.makeText(this, "Đặt sân thành công!\n" +
                            "Sân: " + court + "\n" +
                            "Giờ: " + timeStart + " - " + timeEnd + "\n" +
                            "Vui lòng thanh toán qua QR code",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
}