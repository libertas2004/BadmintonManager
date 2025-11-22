package com.libertas.badmintonmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private ImageView ivTogglePassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DataManager dataManager;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dataManager = new DataManager(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> showRegisterDialog());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dataManager.getUser(username, password);

        if (user != null) {
            Intent intent;
            if (user.isAdmin()) {
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
            }
            intent.putExtra("username", username);
            intent.putExtra("fullName", user.getFullName());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRegisterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);

        EditText etRegUsername = dialogView.findViewById(R.id.etRegUsername);
        EditText etRegPassword = dialogView.findViewById(R.id.etRegPassword);
        EditText etRegFullName = dialogView.findViewById(R.id.etRegFullName);
        EditText etRegPhone = dialogView.findViewById(R.id.etRegPhone);

        new AlertDialog.Builder(this)
                .setTitle("Đăng ký tài khoản")
                .setView(dialogView)
                .setPositiveButton("Đăng ký", (dialog, which) -> {
                    String username = etRegUsername.getText().toString().trim();
                    String password = etRegPassword.getText().toString().trim();
                    String fullName = etRegFullName.getText().toString().trim();
                    String phone = etRegPhone.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(username, password, "user", fullName, phone);
                    if (dataManager.addUser(newUser)) {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}