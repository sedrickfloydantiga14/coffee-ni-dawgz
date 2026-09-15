package com.coffeenidawgz.models;

public class AddOn {
    private int id;
    private String name;
    private double price;
    private String status; // ACTIVE, INACTIVE

    public AddOn() {}

    public AddOn(int id, String name, double price, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s (+₱%.2f)", name, price);
    }
}
