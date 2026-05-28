package com.posmix.mixtuvgag.models;

public class Supplier {
    private int id;
    private String name, phone, address;
    private double currentBalance = 0;

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; } public void setPhone(String p) { phone = p; }
    public String getAddress() { return address; } public void setAddress(String a) { address = a; }
    public double getCurrentBalance() { return currentBalance; } public void setCurrentBalance(double b) { currentBalance = b; }
}
