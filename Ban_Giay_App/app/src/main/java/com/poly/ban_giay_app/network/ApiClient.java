package com.poly.ban_giay_app.network;

import android.content.Context;
import android.util.Log;

import com.poly.ban_giay_app.BuildConfig;
import com.poly.ban_giay_app.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final String TAG = "ApiClient";
    private static final long CONNECT_TIMEOUT_SECONDS = 15L;
    private static final long READ_TIMEOUT_SECONDS = 30L;
    private static final long WRITE_TIMEOUT_SECONDS = 30L;
    private static final int MAX_RETRIES = 2;
    
    private static ApiService apiService;
    private static Context applicationContext;

    private ApiClient() {
        // no-op
    }

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    apiService = buildRetrofit().create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    private static Retrofit buildRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor để thêm token vào header
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                // Thêm token vào header nếu có
                if (applicationContext != null) {
                    SessionManager sessionManager = new SessionManager(applicationContext);
                    String token = sessionManager.getToken();
                    if (token != null && !token.isEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer " + token);
                        Log.d(TAG, "Added token to request header");
                    }
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };

        // Retry interceptor để tự động retry khi có lỗi network
        Interceptor retryInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = null;
                IOException exception = null;
                
                // Thử request tối đa MAX_RETRIES + 1 lần
                for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                    try {
                        if (attempt > 0) {
                            // Đợi một chút trước khi retry
                            try {
                                Thread.sleep(1000 * attempt); // 1s, 2s, ...
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Interrupted during retry", e);
                            }
                            Log.d(TAG, "Retrying request (attempt " + (attempt + 1) + "/" + (MAX_RETRIES + 1) + "): " + request.url());
                        }
                        
                        response = chain.proceed(request);
                        
                        // Nếu response thành công hoặc lỗi không phải network error, return ngay
                        if (response.isSuccessful() || response.code() >= 400) {
                            return response;
                        }
                        
                    } catch (IOException e) {
                        exception = e;
                        Log.w(TAG, "Request failed (attempt " + (attempt + 1) + "/" + (MAX_RETRIES + 1) + "): " + e.getMessage());
                        
                        // Nếu đã thử hết số lần, throw exception
                        if (attempt == MAX_RETRIES) {
                            throw exception;
                        }
                    }
                }
                
                // Nếu đến đây mà vẫn chưa có response, throw exception
                if (exception != null) {
                    throw exception;
                }
                
                return response;
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor) // Thêm token vào header
                .addInterceptor(retryInterceptor)
                .addInterceptor(logging)
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) // Tự động retry khi connection fail
                .build();

        String baseUrl = BuildConfig.API_BASE_URL;
        Log.d(TAG, "Building Retrofit with base URL: " + baseUrl);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
