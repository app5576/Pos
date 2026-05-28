package com.posmix.mixtuvgag.models;

import java.util.Objects;

public class CashTransaction {
    public static final int TYPE_IN = 1, TYPE_OUT = 2;
    private int id, type, referenceId;
    private double amount;
    private String referenceType, description;
    private long date;

    public int getId() { return id; } public void setId(int i) { id = i; }
    public int getType() { return type; } public void setType(int t) { type = t; }
    public double getAmount() { return amount; } public void setAmount(double a) { amount = a; }
    public int getReferenceId() { return referenceId; } public void setReferenceId(int i) { referenceId = i; }
    public String getReferenceType() { return referenceType; } public void setReferenceType(String r) { referenceType = r; }
    public long getDate() { return date; } public void setDate(long d) { date = d; }
    public String getDescription() { return description; } public void setDescription(String d) { description = d; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashTransaction)) return false;
        CashTransaction t = (CashTransaction) o;
        return id == t.id && type == t.type && Double.compare(t.amount, amount) == 0 &&
               date == t.date && Objects.equals(description, t.description);
    }

    @Override
    public int hashCode() { return Objects.hash(id, type, amount, date); }
}
