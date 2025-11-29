package com.poly.ban_giay_app.network;

import com.poly.ban_giay_app.network.model.AuthResponse;
import com.poly.ban_giay_app.network.model.BaseResponse;
import com.poly.ban_giay_app.network.model.UserResponse;
import com.poly.ban_giay_app.network.request.ForgotPasswordRequest;
import com.poly.ban_giay_app.network.request.LoginRequest;
import com.poly.ban_giay_app.network.request.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Khai báo các API dùng với Retrofit.
 * TODO: Chỉnh lại đường dẫn (@POST) cho đúng với backend thực tế của bạn.
 */
public interface ApiService {

    // Ví dụ backend: POST http://localhost:3000/api/auth/login
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // Ví dụ backend: POST http://localhost:3000/api/auth/register
    @POST("auth/register")
    Call<BaseResponse<UserResponse>> register(@Body RegisterRequest request);

    // Ví dụ backend: POST http://localhost:3000/api/auth/forgot-password
    @POST("auth/forgot-password")
    Call<BaseResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);
}
