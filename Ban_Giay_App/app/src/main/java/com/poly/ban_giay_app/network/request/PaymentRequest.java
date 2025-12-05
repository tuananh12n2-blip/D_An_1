package com.poly.ban_giay_app.network.request;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("phuong_thuc_thanh_toan")
    private final String phuongThucThanhToan;

    @SerializedName("ten_chu_the")
    private final String tenChuThe;

    @SerializedName("so_the_day_du")
    private final String soTheDayDu;

    @SerializedName("ngay_het_han")
    private final String ngayHetHan;

    @SerializedName("email")
    private final String email;

    @SerializedName("ten_san_pham")
    private final String tenSanPham;

    @SerializedName("gia_thanh_toan")
    private final String giaThanhToan;

    @SerializedName("so_tai_khoan_chuyen_tien")
    private final String soTaiKhoanChuyenTien;

    public PaymentRequest(String phuongThucThanhToan, String tenChuThe, String soTheDayDu,
                          String ngayHetHan, String email, String tenSanPham, String giaThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.tenChuThe = tenChuThe;
        this.soTheDayDu = soTheDayDu;
        this.ngayHetHan = ngayHetHan;
        this.email = email;
        this.tenSanPham = tenSanPham;
        this.giaThanhToan = giaThanhToan;
        this.soTaiKhoanChuyenTien = null;
    }

    public PaymentRequest(String phuongThucThanhToan, String tenChuThe, String soTheDayDu,
                          String ngayHetHan, String email, String tenSanPham, String giaThanhToan,
                          String soTaiKhoanChuyenTien) {
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.tenChuThe = tenChuThe;
        this.soTheDayDu = soTheDayDu;
        this.ngayHetHan = ngayHetHan;
        this.email = email;
        this.tenSanPham = tenSanPham;
        this.giaThanhToan = giaThanhToan;
        this.soTaiKhoanChuyenTien = soTaiKhoanChuyenTien;
    }
}

