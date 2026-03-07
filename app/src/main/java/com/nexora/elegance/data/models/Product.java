package com.nexora.elegance.data.models;

import java.io.Serializable;

/**
 * Product represents a searchable item in the store.
 * It supports complex variant hierarchies (Color -> Size -> Stock).
 */
public class Product implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private String size; // S, M, L, XL
    private String color;
    private int stock;
    private String shortDescription;
    private java.util.List<String> features;
    private java.util.List<String> imageUrls;
    private java.util.List<VariantColor> variants;

    public static class VariantSize implements Serializable {
        private String size;
        private int quantity;

        public VariantSize() {
        }

        public VariantSize(String size, int quantity) {
            this.size = size;
            this.quantity = quantity;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class VariantColor implements Serializable {
        private String color;
        private java.util.List<VariantSize> sizes;

        public VariantColor() {
        }

        public VariantColor(String color, java.util.List<VariantSize> sizes) {
            this.color = color;
            this.sizes = sizes;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public java.util.List<VariantSize> getSizes() {
            return sizes;
        }

        public void setSizes(java.util.List<VariantSize> sizes) {
            this.sizes = sizes;
        }
    }

    public Product() {
        // Required for Firestore
    }

    public Product(String id, String name, String description, double price, String imageUrl, String category,
            String size, String color, int stock, String shortDescription, java.util.List<String> features,
            java.util.List<String> imageUrls, java.util.List<VariantColor> variants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.size = size;
        this.color = color;
        this.stock = stock;
        this.shortDescription = shortDescription;
        this.features = features;
        this.imageUrls = imageUrls;
        this.variants = variants;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public java.util.List<String> getFeatures() {
        return features;
    }

    public void setFeatures(java.util.List<String> features) {
        this.features = features;
    }

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public java.util.List<VariantColor> getVariants() {
        return variants;
    }

    public void setVariants(java.util.List<VariantColor> variants) {
        this.variants = variants;
    }
}
