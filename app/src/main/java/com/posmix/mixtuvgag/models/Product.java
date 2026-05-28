package com.posmix.mixtuvgag.models;

import java.util.Objects;

public class Product {
    private int id;
    private String name, barcode; // Removed 'category' here
    private double buyPrice, sellPrice, taxPercentage;
    private int stockQuantity, minStockAlert;
    private boolean isActive = true;
    private String notes;
    
    // الحقول الجديدة للمجموعة والوحدة الأساسية
    private int categoryId;
    private String categoryName;
    private int baseUnitId;
    private String baseUnitName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    // Removed getter/setter for 'category'
    // public String getCategory() { return category; }
    // public void setCategory(String category) { this.category = category; }
    
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double b) { buyPrice = b; }
    
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double s) { sellPrice = s; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int q) { stockQuantity = q; }
    
    public int getMinStockAlert() { return minStockAlert; }
    public void setMinStockAlert(int m) { minStockAlert = m; }
    
    public double getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(double t) { taxPercentage = t; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean a) { this.isActive = a; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Getters and Setters للحقول الجديدة
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public int getBaseUnitId() { return baseUnitId; }
    public void setBaseUnitId(int baseUnitId) { this.baseUnitId = baseUnitId; }
    
    public String getBaseUnitName() { return baseUnitName; }
    public void setBaseUnitName(String baseUnitName) { this.baseUnitName = baseUnitName; }

    public boolean isLowStock() { return stockQuantity <= minStockAlert && stockQuantity > 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return id == p.id &&
               Double.compare(p.sellPrice, sellPrice) == 0 &&
               stockQuantity == p.stockQuantity &&
               isActive == p.isActive &&
               categoryId == p.categoryId && // Added for comparison
               baseUnitId == p.baseUnitId && // Added for comparison
               Objects.equals(name, p.name) &&
               Objects.equals(barcode, p.barcode) &&
               Objects.equals(categoryName, p.categoryName) && // Changed from 'category'
               Objects.equals(baseUnitName, p.baseUnitName) && // Added for comparison
               Double.compare(p.buyPrice, buyPrice) == 0 &&
               Double.compare(p.taxPercentage, taxPercentage) == 0 &&
               Objects.equals(notes, p.notes);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, barcode, categoryId, categoryName, baseUnitId, baseUnitName, buyPrice, sellPrice, stockQuantity, minStockAlert, taxPercentage, isActive, notes); }
}