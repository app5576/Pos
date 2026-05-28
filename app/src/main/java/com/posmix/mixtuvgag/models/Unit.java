package com.posmix.mixtuvgag.models;

public class Unit {
    private int id;
    private String name;
    private boolean isDefault;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() { return name; }
}
