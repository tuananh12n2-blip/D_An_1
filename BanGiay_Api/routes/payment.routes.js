const express = require("express");
const router = express.Router();
const PaymentController = require("../controllers/payment.controller");

// Tạo thanh toán mới
router.post("/", PaymentController.createPayment);

// Lấy tất cả thanh toán (có thể dùng cho admin)
router.get("/", PaymentController.getAllPayments);

// Lấy thanh toán theo ID
router.get("/:id", PaymentController.getPaymentById);

module.exports = router;

