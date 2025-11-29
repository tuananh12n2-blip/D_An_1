package com.poly.ban_giay_app.models;

import java.io.Serializable;

public class Product implements Serializable {
    public String name;
    public String priceOld;
    public String priceNew;
    public int imageRes;

    public Product(String name, String priceOld, String priceNew, int imageRes) {
        this.name = name;
        this.priceOld = priceOld;
        this.priceNew = priceNew;
        this.imageRes = imageRes;
    }
}


