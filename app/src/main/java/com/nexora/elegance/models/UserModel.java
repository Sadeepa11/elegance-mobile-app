package com.nexora.elegance.models;

/**
 * UserModel represents a User in the Elegance system.
 * This class is a "POJO" (Plain Old Java Object) used to transfer user data
 * between the Firebase Firestore database and the App's UI.
 */
public class UserModel {
    // Unique ID from Firebase Authentication
    private String uid;
    private String name;
    private String email;
    // defines if the user is a customer ("buyer") or a shop owner ("seller")
    private String role; 

    // Location details for shipping and profile management
    private String postalCode;
    private String address;
    private String city;
    private String state;
    private String district;
    private String country;

    // Link to the user's profile picture stored in the cloud
    private String profileImageUrl;

    /**
     * Empty constructor required by Firebase Firestore to convert 
     * database documents into Java Objects.
     */
    public UserModel() {
    }

    public UserModel(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
