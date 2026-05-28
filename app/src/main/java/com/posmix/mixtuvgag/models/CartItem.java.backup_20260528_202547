
package com.posmix.mixtuvgag.models;
public class CartItem {
    private int productId, quantity;
    private String productName;
    private double unitPrice, taxPercentage;
    public CartItem(int pid, String name, double price, double tax, int qty) {
        this.productId = pid; this.productName = name; this.unitPrice = price; this.taxPercentage = tax; this.quantity = qty;
    }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double p) { unitPrice = p; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { quantity = q; }
    public double getFinalTotal() { return quantity * unitPrice; }
    public double getTaxPercentage() { return taxPercentage; }
}
    