package com.posmix.mixtuvgag.models;
import java.util.List;

public class Invoice {
    public static final int TYPE_SALE = 1, TYPE_PURCHASE = 2;
    public static final int STATUS_CASH = 1, STATUS_CREDIT = 2, STATUS_PARTIAL = 3, STATUS_CARD = 4;
    
    private int id, type, paymentStatus;
    private String invoiceNumber, notes;
    private Integer customerId, supplierId;
    private long date;
    private double subtotal, taxAmount, discount, total, paidAmount, remainingAmount;
    private boolean printed = false;
    private List<InvoiceItem> items;

    public int getId() { return id; } public void setId(int i) { id = i; }
    public String getInvoiceNumber() { return invoiceNumber; } public void setInvoiceNumber(String n) { invoiceNumber = n; }
    public int getType() { return type; } public void setType(int t) { type = t; }
    public Integer getCustomerId() { return customerId; } public void setCustomerId(Integer i) { customerId = i; }
    public Integer getSupplierId() { return supplierId; } public void setSupplierId(Integer i) { supplierId = i; }
    public long getDate() { return date; } public void setDate(long d) { date = d; }
    public double getSubtotal() { return subtotal; } public void setSubtotal(double s) { subtotal = s; }
    public double getTaxAmount() { return taxAmount; } public void setTaxAmount(double t) { taxAmount = t; }
    public double getDiscount() { return discount; } public void setDiscount(double d) { discount = d; }
    public double getTotal() { return total; } public void setTotal(double t) { total = t; }
    public double getPaidAmount() { return paidAmount; } public void setPaidAmount(double p) { paidAmount = p; }
    public double getRemainingAmount() { return remainingAmount; } public void setRemainingAmount(double r) { remainingAmount = r; }
    public int getPaymentStatus() { return paymentStatus; } public void setPaymentStatus(int p) { paymentStatus = p; }
    public String getNotes() { return notes; } public void setNotes(String n) { notes = n; }
    public boolean isPrinted() { return printed; } public void setPrinted(boolean p) { printed = p; }
    public List<InvoiceItem> getItems() { return items; } public void setItems(List<InvoiceItem> items) { this.items = items; }
    
    public void calculateTotals() { 
        total = subtotal + taxAmount - discount; 
        remainingAmount = total - paidAmount; 
    }
}
