package com.libertas.badmintonmanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class UserDashboardActivity extends AppCompatActivity {

    private Spinner spCourt;
    private TextView tvDate, tvTimeStart, tvTimeEnd, tvPrice;
    private Button btnSelectDate, btnSelectTimeStart, btnSelectTimeEnd, btnBooking;
    private String username;
    private String selectedDate = "";
    private String selectedTimeStart = "";
    private String selectedTimeEnd = "";
    private int price = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        username = getIntent().getStringExtra("username");
        TextView tvWelcome = findViewById(R.id.tvWelcomeUser);
        tvWelcome.setText("Chào mừng: " + username);

        spCourt = findViewById(R.id.spCourt);
        tvDate = findViewById(R.id.tvDate);
        tvTimeStart = findViewById(R.id.tvTimeStart);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);
        tvPrice = findViewById(R.id.tvPrice);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTimeStart = findViewById(R.id.btnSelectTimeStart);
        btnSelectTimeEnd = findViewById(R.id.btnSelectTimeEnd);
        btnBooking = findViewById(R.id.btnBooking);

        setupCourtSpinner();
        setupListeners();
    }

    private void setupCourtSpinner() {
        String[] courts = {"Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, courts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourt.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTimeStart.setOnClickListener(v -> showTimePicker(true));
        btnSelectTimeEnd.setOnClickListener(v -> showTimePicker(false));
        btnBooking.setOnClickListener(v -> processBooking());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
            tvDate.setText("Ngày: " + selectedDate);
            calculatePrice();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format("%02d:%02d", hour, minute);
            if (isStartTime) {
                selectedTimeStart = time;
                tvTimeStart.setText("Giờ bắt đầu: " + time);
            } else {
                selectedTimeEnd = time;
                tvTimeEnd.setText("Giờ kết thúc: " + time);
            }
            calculatePrice();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void calculatePrice() {
        if (!selectedTimeStart.isEmpty() && !selectedTimeEnd.isEmpty()) {
            String[] start = selectedTimeStart.split(":");
            String[] end = selectedTimeEnd.split(":");
            int startHour = Integer.parseInt(start[0]);
            int endHour = Integer.parseInt(end[0]);
            int hours = endHour - startHour;

            if (hours > 0) {
                price = hours * 100000; // 100,000 VNĐ/giờ
                tvPrice.setText("Giá: " + String.format("%,d VNĐ", price));
            }
        }
    }

    private void processBooking() {
        if (selectedDate.isEmpty() || selectedTimeStart.isEmpty() || selectedTimeEnd.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String court = spCourt.getSelectedItem().toString();
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("court", court);
        intent.putExtra("customer", username);
        intent.putExtra("date", selectedDate);
        intent.putExtra("time", selectedTimeStart + " - " + selectedTimeEnd);
        intent.putExtra("price", price);
        startActivity(intent);
    }
}
