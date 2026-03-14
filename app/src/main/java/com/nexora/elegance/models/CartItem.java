package com.nexora.elegance.models;

import java.io.Serializable;

/**
 * CartItem represents a specific product variant added to the user's shopping
 * cart.
 * It contains variant-specific details (color, size) selected by the user.
 */
public class CartItem implements Serializable {
    private String id;
    private String productId;
    private String name;
    private String category;
    private double price;
    private String imageUrl;
    private int quantity;
    private String size;
    private String color;
    private java.util.List<String> availableSizes;
    private java.util.List<String> availableColors;
    private java.util.Map<String, Integer> stockMap;

    public CartItem() {
        // Required empty constructor for Firestore
    }

    public CartItem(String id, String productId, String name, String category, double price, String imageUrl,
            int quantity, String size, String color, java.util.List<String> availableSizes,
            java.util.List<String> availableColors, java.util.Map<String, Integer> stockMap) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.availableSizes = availableSizes;
        this.availableColors = availableColors;
        this.stockMap = stockMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public java.util.List<String> getAvailableSizes() {
        return availableSizes;
    }

    public void setAvailableSizes(java.util.List<String> availableSizes) {
        this.availableSizes = availableSizes;
    }

    public java.util.List<String> getAvailableColors() {
        return availableColors;
    }

    public void setAvailableColors(java.util.List<String> availableColors) {
        this.availableColors = availableColors;
    }

    public java.util.Map<String, Integer> getStockMap() {
        return stockMap;
    }

    public void setStockMap(java.util.Map<String, Integer> stockMap) {
        this.stockMap = stockMap;
    }
}
