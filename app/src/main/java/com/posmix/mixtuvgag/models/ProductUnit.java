package com.posmix.mixtuvgag.models;

public class ProductUnit {
    private int id;
    private int productId;
    private int unitId;
    private String unitName;
    private int quantity; // عدد الوحدات الأساسية في هذه العبوة
    private String barcode;
    private double sellPrice;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
}
