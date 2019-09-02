package com.example.scanandbuy_staff;

public class ProductClass {
    String id;
    String barcode;
    String productName;
    String price;
    String quantity;

    public ProductClass() {
    }

    public ProductClass(String barcode, String productName, String price, String quantity) {
        this.barcode = barcode;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }
}
