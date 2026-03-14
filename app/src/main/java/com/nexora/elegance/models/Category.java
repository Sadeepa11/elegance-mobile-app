package com.nexora.elegance.models;

/**
 * Category represents a group of products (e.g., "Electronics", "Men's Wear").
 */
public class Category {
    private String name;
    private String imageUrl;

    public Category() {
        // Required for Firestore
    }

    public Category(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
