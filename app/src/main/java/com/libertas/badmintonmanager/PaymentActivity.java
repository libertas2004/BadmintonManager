package com.libertas.badmintonmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        String court = getIntent().getStringExtra("court");
        String customer = getIntent().getStringExtra("customer");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        int price = getIntent().getIntExtra("price", 0);

        TextView tvPaymentInfo = findViewById(R.id.tvPaymentInfo);
        ImageView ivQRCode = findViewById(R.id.ivQRCode);
        Button btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        String info = "THÔNG TIN THANH TOÁN\n\n" +
                "Chủ sân: Trần Văn Linh\n" +
                "Email: libertas.infor@gmail.com\n\n" +
                "Thông tin đặt sân:\n" +
                "Sân: " + court + "\n" +
                "Khách hàng: " + customer + "\n" +
                "Ngày: " + date + "\n" +
                "Thời gian: " + time + "\n\n" +
                "Số tiền: " + String.format("%,d VNĐ", price);

        tvPaymentInfo.setText(info);

        // QR code sẽ được thay thế sau
        ivQRCode.setImageResource(R.drawable.qr_placeholder);

        btnConfirmPayment.setOnClickListener(v -> {
            Toast.makeText(this, "Đặt sân thành công! Vui lòng thanh toán qua QR code",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
}