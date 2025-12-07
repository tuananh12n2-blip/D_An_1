package com.poly.ban_giay_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.poly.ban_giay_app.adapter.CartAdapter;
import com.poly.ban_giay_app.models.CartItem;
import com.poly.ban_giay_app.network.ApiClient;
import com.poly.ban_giay_app.network.ApiService;
import com.poly.ban_giay_app.network.NetworkUtils;
import com.poly.ban_giay_app.network.model.BaseResponse;
import com.poly.ban_giay_app.network.model.OrderResponse;
import com.poly.ban_giay_app.network.request.OrderRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private CheckBox checkBoxSelectAll;
    private TextView txtTotalPrice;
    private Button btnCheckout;
    private LinearLayout layoutSelectAll, layoutBottom, layoutEmptyCart;
    private EditText edtSearch;
    private ImageView imgBell;
    private ImageView btnBack;
    private ImageView btnViewOrders;
    private View navAccount;
    private ImageView imgAccountIcon;
    private TextView tvAccountLabel;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        sessionManager = new SessionManager(this);
        cartManager = CartManager.getInstance();
        ApiClient.init(this);
        apiService = ApiClient.getApiService();

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initAccountNav();
        updateAccountNavUi();
        setupRecyclerView();
        setupNavigation();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        updateAccountNavUi();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        checkBoxSelectAll = findViewById(R.id.checkBoxSelectAll);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        layoutSelectAll = findViewById(R.id.layoutSelectAll);
        layoutBottom = findViewById(R.id.layoutBottom);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        edtSearch = findViewById(R.id.edtSearch);
        imgBell = findViewById(R.id.imgBell);
        btnBack = findViewById(R.id.btnBack);
        btnViewOrders = findViewById(R.id.btnViewOrders);
    }

    private void setupNavigation() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // Quay về màn hình trước
            });
        }

        // Home navigation
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // View Orders button
        if (btnViewOrders != null) {
            btnViewOrders.setOnClickListener(v -> {
                if (sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void initAccountNav() {
        navAccount = findViewById(R.id.navAccount);
        imgAccountIcon = findViewById(R.id.imgAccountIcon);
        tvAccountLabel = findViewById(R.id.tvAccountLabel);

        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                if (sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(CartActivity.this, AccountActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void updateAccountNavUi() {
        if (tvAccountLabel != null) {
            if (sessionManager.isLoggedIn()) {
                tvAccountLabel.setText(sessionManager.getUserName());
            } else {
                tvAccountLabel.setText(R.string.account);
            }
        }

        if (imgAccountIcon != null) {
            imgAccountIcon.setImageResource(R.drawable.ic_user);
            int color = ContextCompat.getColor(this, sessionManager.isLoggedIn()
                    ? android.R.color.holo_green_dark
                    : android.R.color.black);
            imgAccountIcon.setColorFilter(color);
        }
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartManager.getCartItems(), new CartAdapter.OnCartItemListener() {
            @Override
            public void onItemSelectedChanged(int position, boolean isSelected) {
                cartManager.setItemSelected(position, isSelected);
                updateTotalPrice();
                updateSelectAllCheckbox();
            }

            @Override
            public void onItemRemoved(int position) {
                cartManager.removeFromCart(position);
                cartAdapter.notifyDataSetChanged();
                updateUI();
            }
        });
        rvCartItems.setAdapter(cartAdapter);

        // Select all checkbox
        checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartManager.selectAll(isChecked);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        // Checkout button
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getSelectedCount() == 0) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }
            
            createOrder();
        });
    }

    private void updateUI() {
        if (cartManager.getCartItems().isEmpty()) {
            // Hiển thị giỏ hàng trống
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutSelectAll.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.GONE);
        } else {
            // Hiển thị danh sách sản phẩm
            layoutEmptyCart.setVisibility(View.GONE);
            layoutSelectAll.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.VISIBLE);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
            updateSelectAllCheckbox();
        }
    }

    private void updateTotalPrice() {
        long total = cartManager.getTotalPrice();
        txtTotalPrice.setText(formatPrice(total));
    }

    private void updateSelectAllCheckbox() {
        checkBoxSelectAll.setChecked(cartManager.areAllSelected());
    }

    private String formatPrice(long price) {
        // Format giống như MainActivity: "199.000₫"
        return String.format("%,d₫", price).replace(",", ".");
    }

    private void createOrder() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> selectedItems = cartManager.getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo OrderRequest
        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        
        List<OrderRequest.OrderItemRequest> orderItems = new ArrayList<>();
        long totalPrice = 0;
        
        for (CartItem cartItem : selectedItems) {
            if (cartItem.product.id == null || cartItem.product.id.isEmpty()) {
                Toast.makeText(this, "Sản phẩm " + cartItem.product.name + " không có ID, không thể tạo đơn hàng", Toast.LENGTH_LONG).show();
                return;
            }
            
            long itemPrice = Long.parseLong(cartItem.product.priceNew.replaceAll("[^0-9]", ""));
            long itemTotal = itemPrice * cartItem.quantity;
            totalPrice += itemTotal;
            
            OrderRequest.OrderItemRequest orderItem = new OrderRequest.OrderItemRequest(
                cartItem.product.id,
                cartItem.product.name,
                cartItem.quantity,
                cartItem.size,
                itemPrice
            );
            orderItems.add(orderItem);
        }
        
        request.setItems(orderItems);
        request.setTongTien(totalPrice);
        request.setDiaChiGiaoHang(""); // TODO: Lấy từ form nhập địa chỉ
        request.setSoDienThoai(""); // TODO: Lấy từ form nhập số điện thoại
        request.setGhiChu("");

        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        apiService.createOrder(request).enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Thanh toán");
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<OrderResponse> body = response.body();
                    if (body.getSuccess()) {
                        Toast.makeText(CartActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        // Xóa các sản phẩm đã chọn khỏi giỏ hàng
                        cartManager.removeSelectedItems();
                        cartAdapter.notifyDataSetChanged();
                        updateUI();
                        // Chuyển đến màn hình đơn hàng
                        Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CartActivity.this, body.getMessage() != null ? body.getMessage() : "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CartActivity.this, NetworkUtils.extractErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Thanh toán");
                Toast.makeText(CartActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

