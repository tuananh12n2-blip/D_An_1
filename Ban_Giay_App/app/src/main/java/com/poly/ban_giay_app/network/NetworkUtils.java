package com.poly.ban_giay_app.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    private NetworkUtils() {
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    public static String extractErrorMessage(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }
        ResponseBody errorBody = response.errorBody();
        try {
            String raw = errorBody.string();
            if (TextUtils.isEmpty(raw)) {
                return getDefaultErrorMessage(response.code());
            }
            
            // Check if response is HTML (starts with <!DOCTYPE, <html, etc.)
            String trimmed = raw.trim();
            if (trimmed.startsWith("<!DOCTYPE") || trimmed.startsWith("<html") || 
                trimmed.startsWith("<?xml") || trimmed.startsWith("<")) {
                Log.w(TAG, "Server returned HTML instead of JSON. Response code: " + response.code());
                return getDefaultErrorMessage(response.code());
            }
            
            // Try to parse as JSON
            try {
                JSONObject json = new JSONObject(raw);
                if (json.has("message")) {
                    return json.getString("message");
                }
                if (json.has("error")) {
                    return json.getString("error");
                }
                // If JSON is valid but doesn't have message/error, return default
                return getDefaultErrorMessage(response.code());
            } catch (org.json.JSONException e) {
                // Not valid JSON, return default error message
                Log.w(TAG, "Response is not valid JSON: " + raw.substring(0, Math.min(100, raw.length())));
                return getDefaultErrorMessage(response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "extractErrorMessage: ", e);
            return getDefaultErrorMessage(response != null ? response.code() : 0);
        } finally {
            try {
                errorBody.close();
            } catch (Exception ignored) {
            }
        }
    }
    
    private static String getDefaultErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại thông tin.";
            case 401:
                return "Không có quyền truy cập. Vui lòng đăng nhập lại.";
            case 403:
                return "Bạn không có quyền thực hiện thao tác này.";
            case 404:
                return "Không tìm thấy tài nguyên. Vui lòng thử lại.";
            case 500:
            case 502:
            case 503:
                return "Máy chủ đang gặp sự cố. Vui lòng thử lại sau.";
            case 0:
                return "Không thể kết nối đến máy chủ. Kiểm tra kết nối mạng.";
            default:
                if (statusCode >= 500) {
                    return "Lỗi máy chủ. Vui lòng thử lại sau.";
                } else if (statusCode >= 400) {
                    return "Có lỗi xảy ra. Vui lòng thử lại.";
                } else {
                    return "Không thể kết nối máy chủ.";
                }
        }
    }
}
