package com.posmix.mixtuvgag.models;
import java.util.Objects;

public class Customer {
    private int id;
    private String name, phone, address, email;
    private double creditLimit = 10000;
    private double currentBalance = 0;

    public int getId() { return id; } 
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; } 
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; } 
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; } 
    public void setAddress(String a) { address = a; }
    
    public String getEmail() { return email; } 
    public void setEmail(String e) { email = e; }
    
    public double getCreditLimit() { return creditLimit; } 
    public void setCreditLimit(double c) { creditLimit = c; }
    
    public double getCurrentBalance() { return currentBalance; } 
    public void setCurrentBalance(double b) { this.currentBalance = b; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer c = (Customer) o;
        return id == c.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
