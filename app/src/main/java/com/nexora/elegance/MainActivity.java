package com.nexora.elegance;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.concurrent.TimeUnit;

/**
 * MainActivity is the central hub of the Elegance mobile application.
 * It manages the main navigation between different sections of the app (Home,
 * Wishlist, Cart, Search, Order History),
 * handles fragment transactions, schedules periodic background synchronization,
 * and maintains real-time listeners for wishlist and cart badge counts.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View currentSelected = null;
    private ListenerRegistration wishlistListener;
    private ListenerRegistration cartListener;

    private static final int COLOR_INACTIVE = Color.parseColor("#999999");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize background tasks and setup UI components
        scheduleBackgroundSync();
        loadFragment(new HomeFragment());
        setupNavigation();
        setupWishlistBadge();
        setupCartBadge();
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

    /**
     * Sets up a real-time Firestore listener to keep the Wishlist badge updated.
     * Hides the badge if the user is not logged in or the wishlist is empty.
     */
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

    /**
     * Sets up a real-time Firestore listener to keep the Cart badge updated.
     * Hides the badge if the user is not logged in or the cart is empty.
     */
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
        // Clean up listeners to prevent memory leaks
        if (wishlistListener != null) {
            wishlistListener.remove();
        }
        if (cartListener != null) {
            cartListener.remove();
        }
    }

    /**
     * Handles the bottom navigation selection logic including fragment switching
     * and visual feedback (tinting/animations).
     *
     * @param view The navigation item view that was clicked.
     */
    private void handleSelection(View view) {
        if (view == null)
            return;
        if (view == currentSelected && currentSelected != null)
            return;

        // Reset visual state of all tabs
        resetAll();
        currentSelected = view;

        int id = view.getId();

        // Special handling for the Floating Action Button (Cart)
        if (id == R.id.nav_cart) {
            animateCartFab();
            loadFragment(new com.nexora.elegance.ui.cart.CartFragment());
            return;
        }

        // Highlight selected tab
        tintNavItem(view, true);

        // Load the appropriate fragment based on the clicked view's ID
        if (id == R.id.nav_home)
            loadFragment(new HomeFragment());
        else if (id == R.id.nav_wishlist)
            loadFragment(new WishlistFragment());
        else if (id == R.id.nav_search)
            loadFragment(new com.nexora.elegance.ui.search.SearchFragment());
        else if (id == R.id.nav_order_history)
            loadFragment(new OrderHistoryFragment());
    }

    /**
     * Tints the icons and text of a navigation item to indicate active/inactive
     * state.
     *
     * @param view   The navigation layout view.
     * @param active True to apply active branding, false for inactive gray.
     */
    private void tintNavItem(View view, boolean active) {
        if (!(view instanceof LinearLayout))
            return;

        int color = active
                ? ContextCompat.getColor(this, R.color.md_theme_light_primary)
                : COLOR_INACTIVE;

        LinearLayout layout = (LinearLayout) view;
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(color);
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }
    }

    /**
     * Performs a scale animation on the Cart FAB for a premium feel.
     */
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

    /**
     * Resets all navigation items to their default inactive state.
     */
    private void resetAll() {
        View[] tabs = { binding.navHome, binding.navWishlist, binding.navSearch, binding.navOrderHistory };
        for (View tab : tabs) {
            if (tab != null)
                tintNavItem(tab, false);
        }
        binding.navCart.setScaleX(1f);
        binding.navCart.setScaleY(1f);
    }

    /**
     * Schedules the BackgroundSyncWorker to run periodically every 15 minutes.
     * This ensures offline data or background updates are synced with the server.
     */
    private void scheduleBackgroundSync() {
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                BackgroundSyncWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "EleganceSync", ExistingPeriodicWorkPolicy.KEEP, syncRequest);
    }

    /**
     * Swaps the current fragment in the container with a new one.
     *
     * @param fragment The fragment to display.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Opens the ProfileUpdateActivity when the profile image in the header is
     * clicked.
     */
    public void onProfileImageClick(View view) {
        android.content.Intent intent = new android.content.Intent(this,
                com.nexora.elegance.ui.profile.ProfileUpdateActivity.class);
        startActivity(intent);
    }
}
