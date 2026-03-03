package com.nexora.elegance.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.Category;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.databinding.FragmentHomeBinding;
import com.nexora.elegance.ui.adapters.CategoryAdapter;
import com.nexora.elegance.ui.adapters.ProductAdapter;
import com.nexora.elegance.ui.product.ProductDetailsActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

        private FragmentHomeBinding binding;
        private FirebaseFirestore mFirestore;
        private ProductAdapter productAdapter;
        private CategoryAdapter categoryAdapter;
        private List<Product> productList = new ArrayList<>();
        private List<Category> categoryList = new ArrayList<>();
        private Set<String> wishlistedIds = new HashSet<>();

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                binding = FragmentHomeBinding.inflate(inflater, container, false);
                return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);

                mFirestore = FirebaseFirestore.getInstance();

                setupCategories();
                setupRecyclerView();

                fetchCategories();
                fetchProducts();
                fetchWishlistIds();
        }

        private void fetchWishlistIds() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null)
                        return;

                String uid = user.getUid();
                mFirestore.collection("users").document(uid).collection("wishlist")
                                .addSnapshotListener((value, error) -> {
                                        if (binding == null)
                                                return;
                                        if (error != null)
                                                return;

                                        if (value != null) {
                                                wishlistedIds.clear();
                                                for (QueryDocumentSnapshot doc : value) {
                                                        wishlistedIds.add(doc.getId());
                                                }
                                                if (productAdapter != null) {
                                                        productAdapter.setWishlistedIds(wishlistedIds);
                                                }
                                        }
                                });
        }

        private void setupCategories() {
                categoryAdapter = new CategoryAdapter(categoryList, category -> Toast
                                .makeText(getContext(), "Category: " + category.getName(), Toast.LENGTH_SHORT).show());

                binding.categoryRecyclerView.setLayoutManager(
                                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.categoryRecyclerView.setAdapter(categoryAdapter);
        }

        private void fetchCategories() {
                mFirestore.collection("categories")
                                .addSnapshotListener((value, error) -> {
                                        if (binding == null)
                                                return;
                                        if (error != null) {
                                                if (getContext() != null) {
                                                        Toast.makeText(getContext(), "Error: " + error.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                        }
                                        if (value != null) {
                                                categoryList.clear();
                                                for (QueryDocumentSnapshot doc : value) {
                                                        Category category = doc.toObject(Category.class);
                                                        categoryList.add(category);
                                                }
                                                categoryAdapter.notifyDataSetChanged();
                                        }
                                });
        }

        private void setupRecyclerView() {
                productAdapter = new ProductAdapter(productList,
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

                                                if (wishlistedIds.contains(product.getId())) {
                                                        // Remove from wishlist
                                                        mFirestore.collection("users").document(uid)
                                                                        .collection("wishlist")
                                                                        .document(product.getId())
                                                                        .delete()
                                                                        .addOnSuccessListener(aVoid -> {
                                                                                if (getContext() != null) {
                                                                                        Toast.makeText(getContext(),
                                                                                                        "Removed from Wishlist",
                                                                                                        Toast.LENGTH_SHORT)
                                                                                                        .show();
                                                                                }
                                                                        });
                                                } else {
                                                        // Add to wishlist
                                                        mFirestore.collection("users").document(uid)
                                                                        .collection("wishlist")
                                                                        .document(product.getId())
                                                                        .set(product)
                                                                        .addOnSuccessListener(aVoid -> {
                                                                                if (getContext() != null) {
                                                                                        Toast.makeText(getContext(),
                                                                                                        "Added to Wishlist",
                                                                                                        Toast.LENGTH_SHORT)
                                                                                                        .show();
                                                                                }
                                                                        });
                                                }
                                        } else {
                                                if (getContext() != null) {
                                                        Toast.makeText(getContext(), "Please login first",
                                                                        Toast.LENGTH_SHORT).show();
                                                }
                                        }
                                });

                binding.productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                binding.productRecyclerView.setAdapter(productAdapter);
                binding.productRecyclerView.setNestedScrollingEnabled(false);

        }

        private void fetchProducts() {
                mFirestore.collection("products")
                                .addSnapshotListener((value, error) -> {
                                        if (binding == null)
                                                return;
                                        if (error != null) {
                                                if (getContext() != null) {
                                                        Toast.makeText(getContext(),
                                                                        "Error fetching: " + error.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                        }

                                        if (value != null) {
                                                productList.clear();
                                                for (QueryDocumentSnapshot doc : value) {
                                                        Product product = doc.toObject(Product.class);
                                                        product.setId(doc.getId());
                                                        productList.add(product);
                                                }
                                                productAdapter.notifyDataSetChanged();

                                                if (productList.isEmpty()) {
                                                        binding.noDataText.setVisibility(View.VISIBLE);
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
