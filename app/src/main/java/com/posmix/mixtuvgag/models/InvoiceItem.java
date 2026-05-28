package com.posmix.mixtuvgag.models;

public class InvoiceItem {
    private int id, invoiceId, productId;
    private String productName;
    private double quantity, unitPrice, taxPercentage, discount, total;
    private String notes; // Added notes field

    public int getId() { return id; } public void setId(int i) { id = i; }
    public int getInvoiceId() { return invoiceId; } public void setInvoiceId(int i) { invoiceId = i; }
    public int getProductId() { return productId; } public void setProductId(int i) { productId = i; }
    public String getProductName() { return productName; } public void setProductName(String n) { productName = n; }
    public double getQuantity() { return quantity; } public void setQuantity(double q) { quantity = q; }
    public double getUnitPrice() { return unitPrice; } public void setUnitPrice(double u) { unitPrice = u; }
    public double getTaxPercentage() { return taxPercentage; } public void setTaxPercentage(double t) { taxPercentage = t; }
    public double getDiscount() { return discount; } public void setDiscount(double d) { discount = d; }
    public double getTotal() { return total; } public void setTotal(double t) { total = t; }
    public void setTotalPrice(double t) { total = t; }
    public double getTotalPrice() { return total; }

    public String getNotes() { return notes; } // Getter for notes
    public void setNotes(String notes) { this.notes = notes; } // Setter for notes
}