package com.nexora.elegance.models;

import java.io.Serializable;
import java.util.List;

/**
 * Order represents a completed purchase transaction.
 * It contains order metadata, shipping details, and the list of items
 * purchased.
 */
public class Order implements Serializable {
    private String orderId;
    private String userId;
    private long timestamp;
    private String status; // "Processing", "Shipped", "Completed", "Cancelled"
    private double totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private List<CartItem> items;

    public Order() {
        // Required empty constructor for Firestore
    }

    public Order(String orderId, String userId, long timestamp, String status, double totalAmount,
            String shippingAddress, String paymentMethod, List<CartItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
