package com.nexora.elegance.ui.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.nexora.elegance.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.nexora.elegance.models.Product;
import com.nexora.elegance.databinding.FragmentWishlistBinding;
import com.nexora.elegance.adapters.ProductAdapter;
import com.nexora.elegance.ui.product.ProductDetailsActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private FirebaseFirestore mFirestore;
    private ProductAdapter productAdapter;
    private List<Product> wishlistProducts = new ArrayList<>();
    private Set<String> wishlistedIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        setupRecyclerView();
        fetchWishlist();

        // Setup Sidebar Toggle
        View headerView = binding.getRoot().findViewById(R.id.menuIcon);
        if (headerView != null) {
            headerView.setOnClickListener(v -> {
                if (getActivity() instanceof com.nexora.elegance.MainActivity) {
                    ((com.nexora.elegance.MainActivity) getActivity()).openDrawer();
                }
            });
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(wishlistProducts,
                product -> {
                    if (getContext() != null) {
                        Intent intent = new Intent(getContext(), ProductDetailsActivity.class);
                        intent.putExtra("product", product);
                        startActivity(intent);
                    }
                },
                product -> {
                    com.nexora.elegance.ui.cart.AddToCartBottomSheet bottomSheet = new com.nexora.elegance.ui.cart.AddToCartBottomSheet(
                            product);
                    bottomSheet.show(getParentFragmentManager(), "AddToCartBottomSheet");
                },
                product -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        // Clicking heart on wishlist page removes it
                        mFirestore.collection("users").document(uid).collection("wishlist")
                                .document(product.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Removed from Wishlist", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    }
                });

        binding.wishlistRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.wishlistRecyclerView.setAdapter(productAdapter);
    }

    private void fetchWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (binding != null) {
                binding.noDataText.setVisibility(View.VISIBLE);
                binding.noDataText.setText("Please log in to see your wishlist.");
            }
            return;
        }

        String uid = user.getUid();
        mFirestore.collection("users").document(uid).collection("wishlist")
                .addSnapshotListener((value, error) -> {
                    if (binding == null)
                        return;
                    if (error != null) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    if (value != null) {
                        wishlistProducts.clear();
                        wishlistedIds.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());
                            wishlistProducts.add(product);
                            wishlistedIds.add(product.getId());
                        }

                        productAdapter.setWishlistedIds(wishlistedIds);
                        productAdapter.notifyDataSetChanged();

                        if (wishlistProducts.isEmpty()) {
                            binding.noDataText.setVisibility(View.VISIBLE);
                            binding.noDataText.setText("Your wishlist is empty.");
                        } else {
                            binding.noDataText.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
