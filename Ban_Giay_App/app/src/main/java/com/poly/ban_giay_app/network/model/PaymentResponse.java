package com.poly.ban_giay_app.network.model;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;

    @SerializedName("ten_chu_the")
    private String tenChuThe;

    @SerializedName("so_the_cuoi")
    private String soTheCuoi;

    @SerializedName("email")
    private String email;

    @SerializedName("ten_san_pham")
    private String tenSanPham;

    @SerializedName("gia_thanh_toan")
    private String giaThanhToan;

    @SerializedName("trang_thai")
    private String trangThai;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public String getTenChuThe() {
        return tenChuThe;
    }

    public String getSoTheCuoi() {
        return soTheCuoi;
    }

    public String getEmail() {
        return email;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public String getGiaThanhToan() {
        return giaThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

