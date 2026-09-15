package com.coffeenidawgz.services;

import com.coffeenidawgz.dao.OrderDAO;
import com.coffeenidawgz.dao.SettingDAO;
import com.coffeenidawgz.models.Discount;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.models.OrderItem;
import com.coffeenidawgz.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class POSService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final SettingDAO settingDAO = new SettingDAO();

    private final List<OrderItem> cart = new ArrayList<>();
    private Discount selectedDiscount;

    public void addItemToCart(OrderItem item) {
        // Check if identical item (same product, size, temp, sugar, milk, addons)
        // exists
        for (OrderItem existing : cart) {
            if (isSameItemConfiguration(existing, item)) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                existing.calculateTotal();
                return;
            }
        }
        item.calculateTotal();
        cart.add(item);
    }

    public void removeItemFromCart(int index) {
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
    }

    public void updateQuantity(int index, int newQty) {
        if (index >= 0 && index < cart.size()) {
            if (newQty <= 0) {
                cart.remove(index);
            } else {
                OrderItem item = cart.get(index);
                item.setQuantity(newQty);
                item.calculateTotal();
            }
        }
    }

    public void clearCart() {
        cart.clear();
        selectedDiscount = null;
    }

    public List<OrderItem> getCart() {
        return cart;
    }

    public void setSelectedDiscount(Discount discount) {
        this.selectedDiscount = discount;
    }

    public Discount getSelectedDiscount() {
        return selectedDiscount;
    }

    public double getSubtotal() {
        double sub = 0.0;
        for (OrderItem item : cart) {
            sub += item.getTotalPrice();
        }
        return sub;
    }

    public double getDiscountAmount() {
        if (selectedDiscount == null)
            return 0.0;
        return selectedDiscount.calculateDiscount(getSubtotal());
    }

    public double getVatRate() {
        try {
            String val = settingDAO.getSetting("VAT_RATE", "12.0");
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 12.0;
        }
    }

    public double getVatAmount() {
        double subAfterDiscount = getSubtotal() - getDiscountAmount();
        return subAfterDiscount * (getVatRate() / 100.0);
    }

    public double getTotal() {
        double subAfterDiscount = getSubtotal() - getDiscountAmount();
        return subAfterDiscount + getVatAmount();
    }

    public double calculateChange(double cashReceived) {
        return cashReceived - getTotal();
    }

    public boolean validateCashPayment(double cashReceived) {
        return cashReceived >= getTotal();
    }

    public Order checkout(User cashier, String paymentMethod, double amountReceived) {
        if (cart.isEmpty())
            return null;
        if ("CASH".equalsIgnoreCase(paymentMethod) && !validateCashPayment(amountReceived)) {
            return null;
        }

        Order order = new Order();
        order.setOrderNumber(orderDAO.generateNextOrderNumber());
        order.setCashierId(cashier.getId());
        order.setCashierName(cashier.getFullName());
        order.setSubtotal(getSubtotal());
        order.setDiscountAmount(getDiscountAmount());
        order.setDiscountName(selectedDiscount != null ? selectedDiscount.getName() : "None");
        order.setVatAmount(getVatAmount());
        order.setTotal(getTotal());
        order.setPaymentMethod(paymentMethod);
        order.setAmountReceived(amountReceived);
        order.setChangeAmount(paymentMethod.equalsIgnoreCase("CASH") ? calculateChange(amountReceived) : 0.0);
        order.setStatus("Completed");
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>(cart));

        boolean success = orderDAO.createOrder(order);
        if (success) {
            clearCart();
            return order;
        }
        return null;
    }

    private boolean isSameItemConfiguration(OrderItem a, OrderItem b) {
        if (a.getProductId() != b.getProductId())
            return false;
        if (!a.getSize().equals(b.getSize()))
            return false;
        if (!a.getTemperature().equals(b.getTemperature()))
            return false;
        if (!a.getSugarLevel().equals(b.getSugarLevel()))
            return false;
        if (!a.getMilkOption().equals(b.getMilkOption()))
            return false;

        // Compare addons
        if (a.getAddOns().size() != b.getAddOns().size())
            return false;
        for (int i = 0; i < a.getAddOns().size(); i++) {
            if (a.getAddOns().get(i).getId() != b.getAddOns().get(i).getId())
                return false;
        }
        return true;
    }
}
