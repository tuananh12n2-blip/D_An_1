package com.poly.ban_giay_app;

import android.content.Context;
import android.util.Log;

import com.poly.ban_giay_app.models.CartItem;
import com.poly.ban_giay_app.models.Product;
import com.poly.ban_giay_app.network.ApiClient;
import com.poly.ban_giay_app.network.ApiService;
import com.poly.ban_giay_app.network.NetworkUtils;
import com.poly.ban_giay_app.network.model.BaseResponse;
import com.poly.ban_giay_app.network.request.CartRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;
    private Context context;

    // Callback interface để thông báo kết quả
    public interface CartCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
        // Khởi tạo ApiClient nếu chưa được khởi tạo
        if (context != null) {
            ApiClient.init(context);
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product, String size, int quantity) {
        addToCart(product, size, quantity, null);
    }

    public void addToCart(Product product, String size, int quantity, CartCallback callback) {
        // Debug: Log thông tin sản phẩm khi add vào cart
        Log.d("CartManager", "=== Adding product to cart ===");
        Log.d("CartManager", "Product name: " + product.name);
        Log.d("CartManager", "Product ID: " + product.id);
        Log.d("CartManager", "Product imageUrl: " + product.imageUrl);
        Log.d("CartManager", "Product imageRes: " + product.imageRes);
        Log.d("CartManager", "Size: " + size + ", Quantity: " + quantity);
        
        // Kiểm tra xem sản phẩm với size này đã có trong giỏ chưa
        for (CartItem item : cartItems) {
            if (item.product.name.equals(product.name) && item.size.equals(size)) {
                // Nếu đã có, tăng số lượng
                item.quantity += quantity;
                Log.d("CartManager", "Product already in cart, increased quantity");
                // Cập nhật lên server
                updateCartOnServer(product, size, item.quantity, callback);
                return;
            }
        }
        // Nếu chưa có, thêm mới
        cartItems.add(new CartItem(product, size, quantity));
        Log.d("CartManager", "Product added to cart. Total items: " + cartItems.size());
        
        // Lưu lên server
        saveCartToServer(product, size, quantity, callback);
    }

    private void saveCartToServer(Product product, String size, int quantity, CartCallback callback) {
        if (context == null) {
            String error = "Không thể kết nối đến server";
            Log.w("CartManager", error);
            if (callback != null) {
                callback.onSuccess("Đã thêm vào giỏ hàng thành công! (chỉ lưu cục bộ)");
            }
            return;
        }

        SessionManager sessionManager = new SessionManager(context);
        if (!sessionManager.isLoggedIn()) {
            String error = "Vui lòng đăng nhập để đồng bộ với server";
            Log.w("CartManager", error);
            if (callback != null) {
                callback.onSuccess("Đã thêm vào giỏ hàng thành công! (chỉ lưu cục bộ)");
            }
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            String error = "Không tìm thấy thông tin người dùng";
            Log.w("CartManager", error);
            if (callback != null) {
                callback.onSuccess("Đã thêm vào giỏ hàng thành công! (chỉ lưu cục bộ)");
            }
            return;
        }

        if (product.id == null || product.id.isEmpty()) {
            String warning = "Sản phẩm này không có ID từ server, chỉ lưu cục bộ. Vui lòng chọn sản phẩm từ danh sách chính thức để đồng bộ với server.";
            Log.w("CartManager", warning);
            // Vẫn thông báo thành công cho user vì đã lưu vào local cart
            // Nhưng cảnh báo rằng không lưu được lên server
            if (callback != null) {
                callback.onSuccess("Đã thêm vào giỏ hàng thành công! (chỉ lưu cục bộ)");
            }
            return;
        }

        if (!NetworkUtils.isConnected(context)) {
            String error = "Không có kết nối mạng";
            Log.w("CartManager", error);
            if (callback != null) {
                callback.onSuccess("Đã thêm vào giỏ hàng thành công! (chỉ lưu cục bộ, chưa đồng bộ với server)");
            }
            return;
        }

        // Đảm bảo ApiClient đã được init với context
        ApiClient.init(context);
        ApiService apiService = ApiClient.getApiService();
        CartRequest request = new CartRequest(userId, product.id, size, quantity);

        Log.d("CartManager", "=== SAVING CART TO SERVER ===");
        Log.d("CartManager", "UserId: " + userId);
        Log.d("CartManager", "ProductId: " + product.id);
        Log.d("CartManager", "ProductName: " + product.name);
        Log.d("CartManager", "Size: " + size);
        Log.d("CartManager", "Quantity: " + quantity);
        Log.d("CartManager", "Request: " + request.getUserId() + ", " + request.getProductId() + ", " + request.getSize() + ", " + request.getQuantity());

        apiService.addToCart(request).enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                Log.d("CartManager", "=== API RESPONSE ===");
                Log.d("CartManager", "Response code: " + response.code());
                Log.d("CartManager", "Response isSuccessful: " + response.isSuccessful());
                Log.d("CartManager", "Response body: " + (response.body() != null ? "not null" : "null"));
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Void> body = response.body();
                    Log.d("CartManager", "Response success: " + body.getSuccess());
                    Log.d("CartManager", "Response message: " + body.getMessage());
                    
                    if (body.getSuccess()) {
                        // Luôn hiển thị thông báo thành công rõ ràng
                        String message = body.getMessage() != null && !body.getMessage().isEmpty() 
                            ? body.getMessage() 
                            : "Đã thêm vào giỏ hàng thành công!";
                        Log.d("CartManager", "✅ Cart saved to server successfully: " + message);
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        String error = body.getMessage() != null ? body.getMessage() : "Không thể thêm vào giỏ hàng";
                        Log.e("CartManager", "❌ Failed to save cart: " + error);
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                } else {
                    String error = NetworkUtils.extractErrorMessage(response);
                    Log.e("CartManager", "❌ Failed to save cart to server. Code: " + response.code() + ", Error: " + error);
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("CartManager", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("CartManager", "Cannot read error body", e);
                        }
                    }
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                String error = t.getMessage() != null ? t.getMessage() : "Không thể kết nối đến server";
                Log.e("CartManager", "❌ Error saving cart to server: " + error);
                if (t.getCause() != null) {
                    Log.e("CartManager", "Cause: " + t.getCause().getMessage());
                }
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    private void updateCartOnServer(Product product, String size, int quantity, CartCallback callback) {
        // TODO: Implement update cart item on server
        // For now, just save again (backend should handle update if item exists)
        saveCartToServer(product, size, quantity, callback);
    }

    public void removeFromCart(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
        }
    }

    public void updateQuantity(int position, int quantity) {
        if (position >= 0 && position < cartItems.size()) {
            if (quantity > 0) {
                cartItems.get(position).quantity = quantity;
            } else {
                cartItems.remove(position);
            }
        }
    }

    public void setItemSelected(int position, boolean selected) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.get(position).isSelected = selected;
        }
    }

    public void selectAll(boolean selectAll) {
        for (CartItem item : cartItems) {
            item.isSelected = selectAll;
        }
    }

    public boolean areAllSelected() {
        if (cartItems.isEmpty()) return false;
        for (CartItem item : cartItems) {
            if (!item.isSelected) return false;
        }
        return true;
    }

    public long getTotalPrice() {
        long total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    public int getSelectedCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected) {
                count++;
            }
        }
        return count;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected) {
                selected.add(item);
            }
        }
        return selected;
    }

    public void removeSelectedItems() {
        cartItems.removeIf(item -> item.isSelected);
    }
}

