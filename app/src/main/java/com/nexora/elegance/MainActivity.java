package com.nexora.elegance;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nexora.elegance.databinding.ActivityMainBinding;
import com.nexora.elegance.ui.admin.AdminFragment;
import com.nexora.elegance.ui.home.HomeFragment;
import com.nexora.elegance.ui.map.MapFragment;
import com.nexora.elegance.ui.profile.ProfileFragment;
import com.nexora.elegance.ui.orders.OrderHistoryFragment;
import com.nexora.elegance.ui.wishlist.WishlistFragment;
import com.nexora.elegance.workers.BackgroundSyncWorker;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View currentSelected = null;
    private ListenerRegistration wishlistListener;
    private ListenerRegistration cartListener;

    private static final int REQUEST_CODE_NOTIFICATION = 1001;
    private static final int COLOR_INACTIVE = Color.parseColor("#999999");
    private static final String PREFS_NAME = "elegance_prefs";
    private static final String PREF_DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        applySettingsTheme();
        
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize background tasks and setup UI components
        scheduleBackgroundSync();
        loadFragment(new HomeFragment());
        setupNavigation();
        setupSidebar();
        setupWishlistBadge();
        setupCartBadge();
        
        checkNotificationPermission();
        getFcmToken();
    }

    private void applySettingsTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(PREF_DARK_MODE, false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupSidebar() {
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_theme_toggle || id == R.id.nav_notification_toggle) {
                // Return false to prevent item selection background
                return false;
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Toggle Theme logic
        MenuItem themeItem = binding.navigationView.getMenu().findItem(R.id.nav_theme_toggle);
        if (themeItem != null && themeItem.getActionView() != null) {
            View actionView = themeItem.getActionView();
            android.widget.Switch themeSwitch = actionView.findViewById(R.id.themeSwitch);
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            themeSwitch.setChecked(prefs.getBoolean(PREF_DARK_MODE, false));

            themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(PREF_DARK_MODE, isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        }

        // Toggle Notification logic
        MenuItem notificationItem = binding.navigationView.getMenu().findItem(R.id.nav_notification_toggle);
        if (notificationItem != null && notificationItem.getActionView() != null) {
            View actionView = notificationItem.getActionView();
            androidx.appcompat.widget.SwitchCompat notificationSwitch = actionView.findViewById(R.id.notificationSwitch);
            
            // Sync initial state
            syncNotificationSwitch(notificationSwitch);

            notificationSwitch.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                } else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);
                }
                startActivity(intent);
            });
        }
    }

    private void syncNotificationSwitch(androidx.appcompat.widget.SwitchCompat notificationSwitch) {
        if (notificationSwitch == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationSwitch.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
        } else {
            notificationSwitch.setChecked(androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update notification switch state when returning from settings
        MenuItem notificationItem = binding.navigationView.getMenu().findItem(R.id.nav_notification_toggle);
        if (notificationItem != null && notificationItem.getActionView() != null) {
            androidx.appcompat.widget.SwitchCompat notificationSwitch = notificationItem.getActionView().findViewById(R.id.notificationSwitch);
            syncNotificationSwitch(notificationSwitch);
        }
    }

    public void openDrawer() {
        if (binding.drawerLayout != null) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * Configures the click listeners for the custom bottom navigation bar.
     */
    private void setupNavigation() {
        binding.navHome.setOnClickListener(v -> handleSelection(v));
        binding.navWishlist.setOnClickListener(v -> handleSelection(v));
        binding.navCart.setOnClickListener(v -> handleSelection(v));
        binding.navSearch.setOnClickListener(v -> handleSelection(v));
        binding.navOrderHistory.setOnClickListener(v -> handleSelection(v));

        // Default selection: Home set to active
        handleSelection(binding.navHome);
    }

    private void setupWishlistBadge() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.wishlistBadge.setVisibility(View.GONE);
            return;
        }

        String uid = user.getUid();
        wishlistListener = FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("wishlist")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        int count = snapshot.size();
                        binding.wishlistBadge.setText(String.valueOf(count));
                        binding.wishlistBadge.setVisibility(View.VISIBLE);
                    } else {
                        binding.wishlistBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void setupCartBadge() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.cartBadge.setVisibility(View.GONE);
            return;
        }

        String uid = user.getUid();
        cartListener = FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("cart")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        int count = snapshot.size();
                        binding.cartBadge.setText(String.valueOf(count));
                        binding.cartBadge.setVisibility(View.VISIBLE);
                    } else {
                        binding.cartBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wishlistListener != null) {
            wishlistListener.remove();
        }
        if (cartListener != null) {
            cartListener.remove();
        }
    }

    private void handleSelection(View view) {
        if (view == null)
            return;
        if (view == currentSelected && currentSelected != null)
            return;

        resetAll();
        currentSelected = view;

        int id = view.getId();

        if (id == R.id.nav_cart) {
            animateCartFab();
            loadFragment(new com.nexora.elegance.ui.cart.CartFragment());
            return;
        }

        tintNavItem(view, true);

        if (id == R.id.nav_home)
            loadFragment(new HomeFragment());
        else if (id == R.id.nav_wishlist)
            loadFragment(new WishlistFragment());
        else if (id == R.id.nav_search)
            loadFragment(new com.nexora.elegance.ui.search.SearchFragment());
        else if (id == R.id.nav_order_history)
            loadFragment(new OrderHistoryFragment());
    }

    private void tintNavItem(View view, boolean active) {
        int color = active
                ? ContextCompat.getColor(this, R.color.md_theme_light_primary)
                : COLOR_INACTIVE;

        applyTintRecursive(view, color);
    }

    private void applyTintRecursive(View view, int color) {
        if (view == null)
            return;

        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(color);
        } else if (view instanceof TextView) {
            if (view.getId() != R.id.wishlistBadge && view.getId() != R.id.cartBadge) {
                ((TextView) view).setTextColor(color);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTintRecursive(group.getChildAt(i), color);
            }
        }
    }

    private void animateCartFab() {
        binding.navCart.animate()
                .scaleX(0.85f).scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() -> binding.navCart.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .start())
                .start();
    }

    private void resetAll() {
        View[] tabs = { binding.navHome, binding.navWishlist, binding.navSearch, binding.navOrderHistory };
        for (View tab : tabs) {
            if (tab != null)
                tintNavItem(tab, false);
        }
        binding.navCart.setScaleX(1f);
        binding.navCart.setScaleY(1f);
    }

    private void scheduleBackgroundSync() {
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                BackgroundSyncWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "EleganceSync", ExistingPeriodicWorkPolicy.KEEP, syncRequest);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentSelected != null && currentSelected.getId() != R.id.nav_home) {
            handleSelection(binding.navHome);
        } else {
            super.onBackPressed();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void onProfileImageClick(View view) {
        android.content.Intent intent = new android.content.Intent(this,
                com.nexora.elegance.ui.profile.ProfileUpdateActivity.class);
        startActivity(intent);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("MainActivity", "FCM Token: " + token);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && token != null) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("MainActivity", "FCM Token saved to Firestore"))
                                .addOnFailureListener(e -> {
                                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                                    data.put("fcmToken", token);
                                    FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(user.getUid())
                                            .set(data, com.google.firebase.firestore.SetOptions.merge());
                                });
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. You might miss important updates.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
