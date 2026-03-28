# Elegance: Modern Android E-Commerce Application

**Elegance** is a feature-rich Android application built with Java, leveraging Firebase for real-time data and Google Maps for logistics integration. This README provides a "strictly explained" guide to the project's codebase, designed for academic presentation and viva preparation.

---

## 🚀 1. Project Architecture

The application is structured into several layers for clear separation of concerns:
- **UI Layer**: Activities and Fragments (e.g., `MainActivity`, `HomeFragment`).
- **Data Layer**: Models (`Product.java`, `CartItem.java`) and SQLite (`DatabaseHelper.java`).
- **Network Layer**: Retrofit clients (`RetrofitClient.java`) for API communication.
- **Service Layer**: Firebase Messaging (`MyFirebaseMessagingService.java`) for notifications.
- **Session Layer**: SharedPreferences-based session management (`SessionManager.java`).

---

## 🔑 2. Core Functional Logic

### 2.1 The Entry Point: [SplashActivity.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/ui/splash/SplashActivity.java)
The app starts here. It performs two critical checks before letting the user in:
1.  **Internet Check**: Uses `NetworkUtils` to ensure the device is online.
2.  **Session Check**: Uses `SessionManager` to see if a valid user is logged in.
    - If logged in -> `MainActivity`.
    - If not logged in -> `LoginActivity`.

### 2.2 The Main Hub: [MainActivity.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/MainActivity.java)
This is the coordinator. It manages the **DrawerLayout** (sidebar) and the **Bottom Navigation Bar**.
- **Sidebar**: Permits theme switching (Dark/Light) and opening shop locations in Google Maps.
- **Real-time Badges**: It listens to Firestore changes in the user's `cart` and `wishlist` collections. When a user adds an item, the badge on the navigation bar updates instantly without a page refresh.

### 2.3 Authentication: [LoginActivity.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/ui/auth/LoginActivity.java)
Handles standard Email/Password authentication and **Google Sign-In**.
- **Logic**: Upon successful login, it fetches the user's profile from Firestore to determine if they are a "buyer" or "administrator," then redirects accordingly.

---

## 🛒 3. E-Commerce Flow & Logic

### 3.1 Shopping Cart: [CartAdapter.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/adapters/CartAdapter.java)
This is one of the most complex logic files. It manages:
- **Variant Selection**: Spinners for Size/Color that dynamically check a `StockMap` from Firestore. If a user selects "Red-XL" and only 2 are in stock, they cannot increase quantity beyond 2.
- **Quantity Logic**: Decrementing to 0 removes the item from the cart.

### 3.2 Product Browsing: [HomeFragment.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/ui/home/HomeFragment.java)
- Uses a `GridLayoutManager` to display products in two columns.
- Uses `addSnapshotListener` to ensure that if a product's price or image changes in the database, it reflects on the user's screen immediately.

---

## 🛠️ 4. Technical Stack Highlights

| Component | Library / Technology |
| :--- | :--- |
| **Database** | Firebase Firestore & SQLite |
| **Networking** | Retrofit 2 & OkHttp |
| **Auth** | Firebase Authentication & Google Identity |
| **Images** | Glide (Loading & Caching) |
| **UI** | Material Design Components & ViewBinding |

---

## 📜 5. Code-Level Logic (Strict Explanation)

### **Session Management Logic**
```java
// How the app remembers you
public void setLogin(boolean isLoggedIn, String email, String role) {
    prefs.edit()
            .putBoolean("isLoggedIn", isLoggedIn)
            .putString("userEmail", email)
            .putString("userRole", role)
            .apply(); // Saves to device storage
}
```

### **Theme Switch Logic**
Located in `MainActivity`, this allows for dynamic theme transitions:
```java
themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
    // Save preference and apply globally
    editor.putBoolean("dark_mode", isChecked).apply();
    AppCompatDelegate.setDefaultNightMode(
        isChecked ? MODE_NIGHT_YES : MODE_NIGHT_NO
    );
});
```

---

## 💡 Viva Preparation Tips
For your final project presentation, focus on explaining:
1.  **State Management**: How `SessionManager` and `Firestore` keep the app state synchronized.
2.  **Navigation**: The Fragment transaction logic in `MainActivity`.
3.  **Third-Party Integration**: How Google Maps and Firebase Cloud Messaging add industry-standard features to the app.
