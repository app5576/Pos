package com.posmix.mixtuvgag.models;

public class Expense {
    private int id;
    private String category;
    private double amount;
    private long date;
    private String notes;

    public int getId() { return id; } public void setId(int i) { id = i; }
    public String getCategory() { return category; } public void setCategory(String c) { category = c; }
    public double getAmount() { return amount; } public void setAmount(double a) { amount = a; }
    public long getDate() { return date; } public void setDate(long d) { date = d; }
    public String getNotes() { return notes; } public void setNotes(String n) { notes = n; }
}
