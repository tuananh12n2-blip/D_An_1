const mongoose = require("mongoose");

const PaymentSchema = new mongoose.Schema(
  {
    phuong_thuc_thanh_toan: {
      type: String,
      enum: ["credit_card", "atm_card", "bank_payment"],
      required: true,
    },
    ten_chu_the: {
      type: String,
      required: true,
    },
    so_the_cuoi: {
      type: String,
      required: true,
      maxlength: 4,
    },
    email: {
      type: String,
      required: true,
      lowercase: true,
      trim: true,
    },
    ten_san_pham: {
      type: String,
      required: true,
    },
    gia_thanh_toan: {
      type: String,
      required: true,
    },
    // Thông tin bổ sung
    so_the_day_du: {
      type: String,
      required: false, // Chỉ lưu 4 số cuối, không lưu số đầy đủ vì bảo mật
    },
    ngay_het_han: {
      type: String,
      required: false,
    },
    // Thông tin chuyển khoản ngân hàng
    so_tai_khoan_chuyen_tien: {
      type: String,
      required: false,
    },
    trang_thai: {
      type: String,
      enum: ["pending", "completed", "failed"],
      default: "pending",
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model("Payment", PaymentSchema);

