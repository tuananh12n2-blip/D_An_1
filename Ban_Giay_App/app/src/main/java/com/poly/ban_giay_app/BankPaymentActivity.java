package com.poly.ban_giay_app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.poly.ban_giay_app.models.Product;

public class BankPaymentActivity extends AppCompatActivity {
    private LinearLayout btnBack;
    private ImageView imgQRCode;
    private TextView txtBankName, txtAccountName, txtAccountNumber, txtAmount;
    private Button btnCopyAccountNumber;
    private Product product;
    private String accountNumber = "1234567890";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bank_payment);

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get product from intent
        product = (Product) getIntent().getSerializableExtra("product");

        initViews();
        bindActions();
        setupBankInfo();
        generateQRCode();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgQRCode = findViewById(R.id.imgQRCode);
        txtBankName = findViewById(R.id.txtBankName);
        txtAccountName = findViewById(R.id.txtAccountName);
        txtAccountNumber = findViewById(R.id.txtAccountNumber);
        txtAmount = findViewById(R.id.txtAmount);
        btnCopyAccountNumber = findViewById(R.id.btnCopyAccountNumber);
    }

    private void bindActions() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Copy account number button
        btnCopyAccountNumber.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Account Number", accountNumber);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép số tài khoản", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBankInfo() {
        // Set bank information
        txtBankName.setText("Vietcombank");
        txtAccountName.setText("CONG TY TNHH SNEAKER UNIVERSE");
        txtAccountNumber.setText(accountNumber);

        // Set amount from product
        if (product != null && product.priceNew != null) {
            txtAmount.setText(product.priceNew);
        } else {
            txtAmount.setText("1.200.000₫");
        }
    }

    private void generateQRCode() {
        try {
            // Create QR code content with bank transfer info
            String qrContent = String.format(
                "Vietcombank|%s|%s|%s",
                accountNumber,
                "CONG TY TNHH SNEAKER UNIVERSE",
                product != null && product.priceNew != null ? product.priceNew.replace("₫", "").replace(".", "") : "1200000"
            );

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            imgQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            // If QR code generation fails, show a placeholder or error message
            Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show();
        }
    }
}

