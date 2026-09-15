package com.coffeenidawgz.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String orderNumber;
    private int cashierId;
    private String cashierName;
    private double subtotal;
    private double discountAmount;
    private String discountName;
    private double vatAmount;
    private double total;
    private String paymentMethod; // CASH or GCASH
    private double amountReceived;
    private double changeAmount;
    private String status; // Pending, Preparing, Completed, Cancelled
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(int id, String orderNumber, int cashierId, String cashierName, double subtotal,
                 double discountAmount, String discountName, double vatAmount, double total,
                 String paymentMethod, double amountReceived, double changeAmount,
                 String status, LocalDateTime createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.cashierId = cashierId;
        this.cashierName = cashierName;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.discountName = discountName;
        this.vatAmount = vatAmount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.amountReceived = amountReceived;
        this.changeAmount = changeAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getCashierId() { return cashierId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public String getDiscountName() { return discountName; }
    public void setDiscountName(String discountName) { this.discountName = discountName; }

    public double getVatAmount() { return vatAmount; }
    public void setVatAmount(double vatAmount) { this.vatAmount = vatAmount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getAmountReceived() { return amountReceived; }
    public void setAmountReceived(double amountReceived) { this.amountReceived = amountReceived; }

    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
