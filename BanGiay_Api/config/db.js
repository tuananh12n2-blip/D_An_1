const mongoose = require("mongoose");

const connectDB = async () => {
  try {
    const mongoUri =
      process.env.MONGODB_URI || "mongodb://localhost:27017/BanGiay_App";
    await mongoose.connect(mongoUri);
    console.log("MongoDB kết nối thành công!");
  } catch (error) {
    console.log("Lỗi kết nối MongoDB: ", error);
    process.exit(1);
  }
};

module.exports = connectDB;
