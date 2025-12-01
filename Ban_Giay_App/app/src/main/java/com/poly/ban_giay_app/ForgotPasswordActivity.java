package com.poly.ban_giay_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.poly.ban_giay_app.network.ApiClient;
import com.poly.ban_giay_app.network.ApiService;
import com.poly.ban_giay_app.network.NetworkUtils;
import com.poly.ban_giay_app.network.model.BaseResponse;
import com.poly.ban_giay_app.network.request.ForgotPasswordRequest;
import com.poly.ban_giay_app.network.request.VerifyOtpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private View btnBack;
    private EditText edtPhoneEmail;
    private EditText edtVerificationCode;
    private EditText edtNewPassword;
    private EditText edtConfirmNewPassword;
    private Button btnSendCode;
    private Button btnResetPassword;
    private ProgressDialog progressDialog;
    private ApiService apiService;
    private String userEmail; // Lưu email để dùng cho verify OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getApiService();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.forgot_password_processing));
        progressDialog.setCancelable(false);

        initViews();
        bindActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtPhoneEmail = findViewById(R.id.edtPhoneEmail);
        edtVerificationCode = findViewById(R.id.edtVerificationCode);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> requestForgotPassword());
        btnResetPassword.setOnClickListener(v -> verifyOtpAndResetPassword());
    }

    private void requestForgotPassword() {
        String email = edtPhoneEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtPhoneEmail.setError(getString(R.string.error_email_required));
            return;
        }

        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang gửi mã xác nhận...");
        progressDialog.show();
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        apiService.forgotPassword(request).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    userEmail = email; // Lưu email để dùng cho verify OTP
                    Toast.makeText(ForgotPasswordActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Hiển thị form nhập OTP
                    showOtpForm();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            NetworkUtils.extractErrorMessage(response),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this,
                        "Lỗi: " + (t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Không thể kết nối đến server"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpForm() {
        // Ẩn form nhập email
        edtPhoneEmail.setVisibility(View.GONE);
        btnSendCode.setVisibility(View.GONE);
        
        // Hiển thị form nhập OTP và mật khẩu mới
        edtVerificationCode.setVisibility(View.VISIBLE);
        edtNewPassword.setVisibility(View.VISIBLE);
        edtConfirmNewPassword.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.VISIBLE);
        
        // Disable email field
        edtPhoneEmail.setEnabled(false);
    }

    private void verifyOtpAndResetPassword() {
        String otp = edtVerificationCode.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            edtVerificationCode.setError("Vui lòng nhập mã xác nhận");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Lỗi: Không tìm thấy email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang đặt lại mật khẩu...");
        progressDialog.show();
        VerifyOtpRequest request = new VerifyOtpRequest(userEmail, otp, newPassword, confirmPassword);
        apiService.verifyOtpAndResetPassword(request).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish(); // Quay về màn hình đăng nhập
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            NetworkUtils.extractErrorMessage(response),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this,
                        "Lỗi: " + (t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Không thể kết nối đến server"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}