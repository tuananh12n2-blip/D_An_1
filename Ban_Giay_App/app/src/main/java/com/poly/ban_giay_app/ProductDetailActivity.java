package com.poly.ban_giay_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.poly.ban_giay_app.models.Product;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView btnBack, imgProduct;
    private TextView txtProductName, txtBrand, txtRating, txtPriceOld, txtPriceNew, txtQuantity;
    private Button btnSize37, btnSize38, btnSize39, btnDecrease, btnIncrease, btnAddToCart, btnBuyNow;
    private Product product;
    private int quantity = 1;
    private String selectedSize = "";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        // Apply insets - chỉ padding top và sides, không padding bottom để nút mua không bị che
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        
        // Đảm bảo layoutBuyButton có padding bottom từ system bars
        View layoutBuyButton = findViewById(R.id.layoutBuyButton);
        if (layoutBuyButton != null) {
            ViewCompat.setOnApplyWindowInsetsListener(layoutBuyButton, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    Math.max(v.getPaddingBottom(), systemBars.bottom)
                );
                return insets;
            });
        }

        // Get product from intent
        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        
        // Set context cho CartManager để có thể gọi API
        CartManager.getInstance().setContext(this);

        initViews();
        bindActions();
        displayProduct();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        txtBrand = findViewById(R.id.txtBrand);
        txtRating = findViewById(R.id.txtRating);
        txtPriceOld = findViewById(R.id.txtPriceOld);
        txtPriceNew = findViewById(R.id.txtPriceNew);
        txtQuantity = findViewById(R.id.txtQuantity);
        
        btnSize37 = findViewById(R.id.btnSize37);
        btnSize38 = findViewById(R.id.btnSize38);
        btnSize39 = findViewById(R.id.btnSize39);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> finish());

        // Size selection
        btnSize37.setOnClickListener(v -> selectSize("37", btnSize37));
        btnSize38.setOnClickListener(v -> selectSize("38", btnSize38));
        btnSize39.setOnClickListener(v -> selectSize("39", btnSize39));

        // Quantity controls
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
                updateBottomBar();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            txtQuantity.setText(String.valueOf(quantity));
            updateBottomBar();
        });

        // Add to cart
        btnAddToCart.setOnClickListener(v -> {
            // Kiểm tra đăng nhập
            if (!ensureLoggedIn()) {
                return;
            }
            
            // Kiểm tra size đã được chọn chưa
            if (selectedSize.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn kích thước trước khi thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Disable button để tránh click nhiều lần
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Đang thêm...");
            
            // Hiển thị thông báo ngay lập tức
            Toast.makeText(this, "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
            
            CartManager.getInstance().addToCart(product, selectedSize, quantity, new CartManager.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        // Nếu message khác với thông báo mặc định, hiển thị lại
                        if (message.contains("chỉ lưu cục bộ") && !message.equals("Đã thêm vào giỏ hàng thành công!")) {
                            Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setText("THÊM VÀO GIỎ");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        // Nếu có lỗi khi lưu lên server, vẫn đã thêm vào giỏ hàng cục bộ
                        // Không cần hiển thị lại vì đã hiển thị ở trên
                        Log.w("ProductDetailActivity", "Error saving to server: " + error);
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setText("THÊM VÀO GIỎ");
                    });
                }
            });
        });

        // Buy now
        btnBuyNow.setOnClickListener(v -> {
            if (!ensureLoggedIn()) {
                return;
            }
            if (selectedSize.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn kích thước", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Buy now logic
            Toast.makeText(this, "Đang chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectSize(String size, Button button) {
        selectedSize = size;
        
        // Reset all buttons - chữ màu trắng, không có gạch chân
        resetButton(btnSize37, "37");
        resetButton(btnSize38, "38");
        resetButton(btnSize39, "39");
        
        // Highlight selected button - có gạch chân
        String buttonText = button.getText().toString();
        SpannableString spannableString = new SpannableString(buttonText);
        spannableString.setSpan(new UnderlineSpan(), 0, buttonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        button.setText(spannableString);
        button.setBackgroundResource(R.drawable.bg_size_button_selected);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }
    
    private void resetButton(Button button, String text) {
        button.setBackgroundResource(R.drawable.bg_size_button);
        button.setText(text);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    /**
     * Kiểm tra đăng nhập. Nếu chưa đăng nhập thì chuyển sang màn Login.
     *
     * @return true nếu đã đăng nhập, false nếu đã chuyển sang Login.
     */
    private boolean ensureLoggedIn() {
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            return true;
        }

        Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục mua hàng", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return false;
    }

    private void displayProduct() {
        // Load image from URL if available, otherwise use resource
        if (product.imageUrl != null && !product.imageUrl.isEmpty()) {
            // Nếu là URL từ server
            if (product.imageUrl.startsWith("http://") || product.imageUrl.startsWith("https://")) {
                Glide.with(this)
                        .load(product.imageUrl)
                        .placeholder(R.drawable.giaymau)
                        .error(R.drawable.giaymau)
                        .into(imgProduct);
            } else {
                // Nếu là tên file ảnh (giay15, giay14, etc.), load từ drawable
                int imageResId = getImageResourceId(product.imageUrl);
                if (imageResId != 0) {
                    imgProduct.setImageResource(imageResId);
                } else {
                    imgProduct.setImageResource(R.drawable.giaymau);
                }
            }
        } else if (product.imageRes != 0) {
            imgProduct.setImageResource(product.imageRes);
        } else {
            imgProduct.setImageResource(R.drawable.giaymau);
        }
        
        txtProductName.setText(product.name);
        
        // Extract brand from product name (first word after "Giày")
        String brand = product.name.replace("Giày ", "").split(" ")[0];
        txtBrand.setText(brand);
        
        // Rating (5 stars)
        txtRating.setText("★★★★★");
        
        // Price
        String priceOldText = "Giá gốc: " + product.priceOld;
        SpannableString ss = new SpannableString(priceOldText);
        int startIndex = priceOldText.indexOf(product.priceOld);
        ss.setSpan(new StrikethroughSpan(), startIndex, priceOldText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtPriceOld.setText(ss);
        
        txtPriceNew.setText("Giá khuyến mãi: " + product.priceNew);
        
        // Quantity
        txtQuantity.setText(String.valueOf(quantity));
        
        // Update bottom bar
        updateBottomBar();
    }
    
    /**
     * Cập nhật nút mua với giá hiển thị bên trong
     */
    private void updateBottomBar() {
        if (btnBuyNow != null && product != null) {
            // Parse giá từ product.priceNew (format: "899.000₫")
            String priceStr = product.priceNew.replace("₫", "").replace(".", "").trim();
            try {
                long pricePerUnit = Long.parseLong(priceStr);
                long totalPrice = pricePerUnit * quantity;
                String totalPriceFormatted = formatPrice(totalPrice);
                
                // Hiển thị giá trong nút mua: "MUA NGAY" trên, giá dưới
                String buttonText = "MUA NGAY\n" + totalPriceFormatted;
                btnBuyNow.setText(buttonText);
            } catch (NumberFormatException e) {
                // Nếu không parse được, dùng giá gốc
                String buttonText = "MUA NGAY\n" + product.priceNew;
                btnBuyNow.setText(buttonText);
            }
        }
    }
    
    /**
     * Format giá thành định dạng VNĐ
     */
    private String formatPrice(long price) {
        return String.format("%,d₫", price).replace(",", ".");
    }
    
    /**
     * Lấy resource ID từ tên file ảnh (giay15, giay14, etc.)
     */
    private int getImageResourceId(String imageName) {
        // Loại bỏ extension và path nếu có
        String name = imageName;
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        
        // Map tên file với resource ID
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}

