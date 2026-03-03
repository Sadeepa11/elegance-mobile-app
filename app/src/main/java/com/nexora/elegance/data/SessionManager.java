package com.nexora.elegance.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "ElegancePrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_ROLE = "userRole"; // buyer or seller

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLogin(boolean isLoggedIn, String email, String role) {
        prefs.edit()
                .putBoolean(IS_LOGGED_IN, isLoggedIn)
                .putString(USER_EMAIL, email)
                .putString(USER_ROLE, role)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return prefs.getString(USER_EMAIL, null);
    }

    public String getUserRole() {
        return prefs.getString(USER_ROLE, "buyer");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
