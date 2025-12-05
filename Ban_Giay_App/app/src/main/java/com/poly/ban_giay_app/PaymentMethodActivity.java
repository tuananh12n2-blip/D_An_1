package com.poly.ban_giay_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class PaymentMethodActivity extends AppCompatActivity {
    private LinearLayout btnBack;
    private TextView txtProductInfo;
    private LinearLayout layoutCreditCard, layoutAtmCard, layoutBankPayment;
    private Product product;
    private int quantity = 1;
    private String selectedSize = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_method);

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get product data from intent
        product = (Product) getIntent().getSerializableExtra("product");
        quantity = getIntent().getIntExtra("quantity", 1);
        selectedSize = getIntent().getStringExtra("selectedSize");

        if (product == null) {
            finish();
            return;
        }

        initViews();
        bindActions();
        displayProductInfo();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtProductInfo = findViewById(R.id.txtProductInfo);
        layoutCreditCard = findViewById(R.id.layoutCreditCard);
        layoutAtmCard = findViewById(R.id.layoutAtmCard);
        layoutBankPayment = findViewById(R.id.layoutBankPayment);
    }

    private void bindActions() {
        // Back button - quay lại màn chi tiết sản phẩm
        btnBack.setOnClickListener(v -> finish());

        // Payment method selection
        layoutCreditCard.setOnClickListener(v -> {
            selectPaymentMethod("credit_card", layoutCreditCard);
        });

        layoutAtmCard.setOnClickListener(v -> {
            selectPaymentMethod("atm_card", layoutAtmCard);
        });

        layoutBankPayment.setOnClickListener(v -> {
            selectPaymentMethod("bank_payment", layoutBankPayment);
        });
    }

    private void selectPaymentMethod(String method, LinearLayout layout) {
        // Reset all payment methods
        resetPaymentMethod(layoutCreditCard);
        resetPaymentMethod(layoutAtmCard);
        resetPaymentMethod(layoutBankPayment);

        // Highlight selected payment method - use lighter background
        layout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        layout.setElevation(4f);
        
        // Navigate to payment detail screen for credit card and ATM card
        if ("credit_card".equals(method) || "atm_card".equals(method)) {
            Intent intent = new Intent(PaymentMethodActivity.this, PaymentDetailActivity.class);
            intent.putExtra("paymentMethod", method);
            intent.putExtra("product", product);
            startActivity(intent);
        } else if ("bank_payment".equals(method)) {
            // Navigate to bank payment screen
            Intent intent = new Intent(PaymentMethodActivity.this, BankPaymentActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        }
    }

    private void resetPaymentMethod(LinearLayout layout) {
        layout.setBackgroundColor(0xFFF5F5F5); // Light gray background
        layout.setElevation(2f);
    }

    private void displayProductInfo() {
        if (product != null) {
            String productInfo = product.name + " • " + product.priceNew;
            txtProductInfo.setText(productInfo);
        }
    }

    private void setupBottomNavigation() {
        SessionManager sessionManager = new SessionManager(this);

        // Trang chủ
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(PaymentMethodActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }

        // Danh mục
        View navCategories = findViewById(R.id.navCategories);
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                Intent intent = new Intent(PaymentMethodActivity.this, CategoriesActivity.class);
                startActivity(intent);
            });
        }

        // Giỏ hàng
        View navCart = findViewById(R.id.navCart);
        if (navCart != null) {
            navCart.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng giỏ hàng đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        // Trợ giúp
        View navHelp = findViewById(R.id.navHelp);
        if (navHelp != null) {
            navHelp.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng trợ giúp đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        // Tài khoản
        View navAccount = findViewById(R.id.navAccount);
        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                if (sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(PaymentMethodActivity.this, AccountActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(PaymentMethodActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}

