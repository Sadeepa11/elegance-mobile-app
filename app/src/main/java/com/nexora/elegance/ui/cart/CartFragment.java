package com.nexora.elegance.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexora.elegance.models.CartItem;
import com.nexora.elegance.databinding.FragmentCartBinding;
import com.nexora.elegance.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CartFragment displays the list of products currently in the user's shopping
 * cart.
 * It allows users to:
 * - View cart items with details (price, size, color).
 * - Remove items or adjust quantities.
 * - See an order summary with subtotal and total estimations.
 * - Proceed to the checkout activity.
 */
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private CartAdapter cartAdapter;
    private List<CartItem> cartList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupListeners();
        fetchCartItems();
    }

    /**
     * Initializes the RecyclerView and handles item interactions via callbacks.
     */
    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(getContext(), cartList, new CartAdapter.OnCartItemInteractionListener() {
            @Override
            public void onItemRemoved(CartItem item) {
                removeCartItem(item);
            }

            @Override
            public void onItemUpdated(CartItem item) {
                updateCartItem(item);
            }
        });

        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Navigate to CheckoutActivity if cart is not empty
        binding.btnProceed.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                android.content.Intent intent = new android.content.Intent(getContext(),
                        com.nexora.elegance.ui.checkout.CheckoutActivity.class);
                intent.putExtra("checkout_list", new java.util.ArrayList<>(cartList));
                startActivity(intent);
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Fetches the user's cart from Firestore and sets up a real-time listener.
     */
    private void fetchCartItems() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view cart.", Toast.LENGTH_SHORT).show();
            updateTotals();
            return;
        }

        mFirestore.collection("users").document(user.getUid()).collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (binding == null)
                        return;

                    if (error != null) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching cart: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        cartList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem cartItem = doc.toObject(CartItem.class);
                            cartItem.setId(doc.getId());
                            cartList.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateTotals();
                    }
                });
    }

    /**
     * Deletes a specific item from the user's Firestore cart.
     */
    private void removeCartItem(CartItem item) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        mFirestore.collection("users").document(user.getUid()).collection("cart").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates an existing cart item (e.g., quantity changes) in Firestore.
     */
    private void updateCartItem(CartItem item) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        // If qty becomes 0, remove instead of update
        if (item.getQuantity() <= 0) {
            removeCartItem(item);
            return;
        }

        mFirestore.collection("users").document(user.getUid()).collection("cart").document(item.getId())
                .set(item)
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Calculates and displays the order summary totals based on the current cart
     * content.
     */
    private void updateTotals() {
        double orderAmount = 0.0;
        for (CartItem item : cartList) {
            int qty = item.getQuantity() > 0 ? item.getQuantity() : 1;
            orderAmount += (item.getPrice() * qty);
        }

        // Potential fees (currently set to 0)
        double convenienceFee = 0.0;
        double deliveryFee = 0.0;

        double total = orderAmount + convenienceFee + deliveryFee;

        if (binding == null)
            return;

        binding.orderAmountsText.setText(String.format(Locale.getDefault(), "LKR %.2f", orderAmount));
        binding.orderTotalText.setText(String.format(Locale.getDefault(), "LKR %.2f", total));
        binding.bottomTotalText.setText(String.format(Locale.getDefault(), "LKR %.2f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
