package com.poly.ban_giay_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.poly.ban_giay_app.models.Product;
import com.poly.ban_giay_app.network.ApiClient;
import com.poly.ban_giay_app.network.ApiService;
import com.poly.ban_giay_app.network.NetworkUtils;
import com.poly.ban_giay_app.network.model.BaseResponse;
import com.poly.ban_giay_app.network.model.PaymentResponse;
import com.poly.ban_giay_app.network.request.PaymentRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDetailActivity extends AppCompatActivity {
    private LinearLayout btnBack;
    private ImageView imgPaymentMethod;
    private TextView txtPaymentMethod;
    private EditText edtCardholderName, edtCardNumber, edtExpiryDate, edtCVV;
    private Button btnConfirmPayment;
    private String paymentMethod;
    private Product product;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_detail);

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get payment method and product from intent
        paymentMethod = getIntent().getStringExtra("paymentMethod");
        product = (Product) getIntent().getSerializableExtra("product");

        if (paymentMethod == null) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        initViews();
        bindActions();
        setupPaymentMethod();
        setupInputFormatters();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgPaymentMethod = findViewById(R.id.imgPaymentMethod);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        edtCardholderName = findViewById(R.id.edtCardholderName);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        edtCVV = findViewById(R.id.edtCVV);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    private void bindActions() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Confirm payment button
        btnConfirmPayment.setOnClickListener(v -> {
            if (validateInput()) {
                processPayment();
            }
        });
    }

    private void setupPaymentMethod() {
        if (paymentMethod == null) {
            return;
        }

        switch (paymentMethod) {
            case "credit_card":
                imgPaymentMethod.setImageResource(R.drawable.ic_credit_card);
                txtPaymentMethod.setText("Thẻ tín dụng");
                break;
            case "atm_card":
                imgPaymentMethod.setImageResource(R.drawable.ic_atm_card);
                txtPaymentMethod.setText("Thẻ ATM");
                break;
            default:
                imgPaymentMethod.setImageResource(R.drawable.ic_credit_card);
                txtPaymentMethod.setText("Thẻ tín dụng");
                break;
        }
    }

    private void setupInputFormatters() {
        // Format card number with spaces (e.g., 1234 5678 9012 3456)
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll(" ", "");
                if (input.length() > 0 && input.length() % 4 == 0 && s.length() < input.length() + (input.length() / 4 - 1)) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ");
                        }
                        formatted.append(input.charAt(i));
                    }
                    edtCardNumber.removeTextChangedListener(this);
                    edtCardNumber.setText(formatted.toString());
                    edtCardNumber.setSelection(formatted.length());
                    edtCardNumber.addTextChangedListener(this);
                }
            }
        });

        // Format expiry date (MM/YY)
        edtExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("/", "");
                if (input.length() >= 2 && !s.toString().contains("/")) {
                    String formatted = input.substring(0, 2) + "/" + (input.length() > 2 ? input.substring(2) : "");
                    edtExpiryDate.removeTextChangedListener(this);
                    edtExpiryDate.setText(formatted);
                    edtExpiryDate.setSelection(formatted.length());
                    edtExpiryDate.addTextChangedListener(this);
                }
            }
        });
    }

    private boolean validateInput() {
        String cardholderName = edtCardholderName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().replaceAll(" ", "").trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String cvv = edtCVV.getText().toString().trim();

        if (cardholderName.isEmpty()) {
            edtCardholderName.setError("Vui lòng nhập tên chủ thẻ");
            edtCardholderName.requestFocus();
            return false;
        }

        if (cardNumber.isEmpty() || cardNumber.length() < 13) {
            edtCardNumber.setError("Số thẻ không hợp lệ");
            edtCardNumber.requestFocus();
            return false;
        }

        if (expiryDate.isEmpty() || !expiryDate.matches("\\d{2}/\\d{2}")) {
            edtExpiryDate.setError("Ngày hết hạn không hợp lệ (MM/YY)");
            edtExpiryDate.requestFocus();
            return false;
        }

        if (cvv.isEmpty() || cvv.length() < 3) {
            edtCVV.setError("CVV không hợp lệ");
            edtCVV.requestFocus();
            return false;
        }

        return true;
    }

    private void processPayment() {
        // Disable button to prevent multiple clicks
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Đang xử lý...");

        // Get input values
        String cardholderName = edtCardholderName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().replaceAll(" ", "").trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String email = sessionManager.getEmail();

        // If user is not logged in, use a default email or show error
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            btnConfirmPayment.setEnabled(true);
            btnConfirmPayment.setText("Xác nhận thanh toán");
            return;
        }

        // Get product info
        String productName = product != null ? product.name : "Sản phẩm";
        String productPrice = product != null ? product.priceNew : "0₫";

        // Create payment request
        PaymentRequest request = new PaymentRequest(
            paymentMethod,
            cardholderName,
            cardNumber,
            expiryDate,
            email,
            productName,
            productPrice
        );

        // Call API
        Log.d("PaymentDetailActivity", "Calling payment API with: " + 
            "method=" + paymentMethod + 
            ", name=" + cardholderName + 
            ", email=" + email + 
            ", product=" + productName);
        
        apiService.createPayment(request).enqueue(new Callback<BaseResponse<PaymentResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<PaymentResponse>> call, Response<BaseResponse<PaymentResponse>> response) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Xác nhận thanh toán");

                Log.d("PaymentDetailActivity", "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PaymentResponse> baseResponse = response.body();
                    Log.d("PaymentDetailActivity", "Success: " + baseResponse.getSuccess() + ", Message: " + baseResponse.getMessage());
                    
                    if (baseResponse.getSuccess()) {
                        PaymentResponse paymentResponse = baseResponse.getData();
                        if (paymentResponse != null) {
                            String message = String.format(
                                "Thanh toán thành công!\nTên chủ thẻ: %s\n4 số cuối: %s\nEmail: %s",
                                paymentResponse.getTenChuThe(),
                                paymentResponse.getSoTheCuoi(),
                                paymentResponse.getEmail()
                            );
                            Toast.makeText(PaymentDetailActivity.this, message, Toast.LENGTH_LONG).show();
                            
                            // Navigate back
                            finish();
                        } else {
                            Toast.makeText(PaymentDetailActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        String errorMsg = baseResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Có lỗi xảy ra khi thanh toán";
                        }
                        Toast.makeText(PaymentDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("PaymentDetailActivity", "Response not successful. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("PaymentDetailActivity", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("PaymentDetailActivity", "Error reading error body", e);
                        }
                    }
                    String errorMsg = NetworkUtils.extractErrorMessage(response);
                    Toast.makeText(PaymentDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PaymentResponse>> call, Throwable t) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Xác nhận thanh toán");
                
                Log.e("PaymentDetailActivity", "Payment failed", t);
                String errorMsg = t.getMessage();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng thử lại.";
                }
                Toast.makeText(PaymentDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}

