package com.nexora.elegance.ui.product;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexora.elegance.R;
import com.nexora.elegance.adapters.ProductAdapter;
import com.nexora.elegance.databinding.ActivityCategoryBinding;
import com.nexora.elegance.models.Category;
import com.nexora.elegance.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryActivity extends AppCompatActivity {

    private ActivityCategoryBinding binding;
    private FirebaseFirestore mFirestore;
    private ProductAdapter productAdapter;
    private List<Product> originalProductList = new ArrayList<>();
    private List<Product> displayedProductList = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();
    private Set<String> wishlistedIds = new HashSet<>();
    
    private String selectedCategory;
    private String currentSort = "Recommended";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirestore = FirebaseFirestore.getInstance();
        
        selectedCategory = getIntent().getStringExtra("category_name");
        if (selectedCategory == null) {
            selectedCategory = "All Categories";
        }
        
        binding.categoryTitle.setText(selectedCategory);
        binding.backButton.setOnClickListener(v -> finish());
        
        setupRecyclerView();
        setupButtons();
        fetchProducts();
        fetchCategories();
        fetchWishlistIds();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(displayedProductList,
                product -> {
                    Intent intent = new Intent(this, ProductDetailsActivity.class);
                    intent.putExtra("product", product);
                    startActivity(intent);
                },
                product -> {
                    com.nexora.elegance.ui.cart.AddToCartBottomSheet bottomSheet = new com.nexora.elegance.ui.cart.AddToCartBottomSheet(product);
                    bottomSheet.show(getSupportFragmentManager(), "AddToCartBottomSheet");
                },
                this::toggleWishlist);

        binding.categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.categoryRecyclerView.setAdapter(productAdapter);
    }

    private void toggleWishlist(Product product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            if (wishlistedIds.contains(product.getId())) {
                mFirestore.collection("users").document(uid).collection("wishlist").document(product.getId()).delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show());
            } else {
                mFirestore.collection("users").document(uid).collection("wishlist").document(product.getId()).set(product)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Wishlist", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWishlistIds() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        mFirestore.collection("users").document(user.getUid()).collection("wishlist")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    wishlistedIds.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        wishlistedIds.add(doc.getId());
                    }
                    if (productAdapter != null) {
                        productAdapter.setWishlistedIds(wishlistedIds);
                    }
                });
    }

    private void setupButtons() {
        binding.sortButton.setOnClickListener(v -> showSortBottomSheet());
        binding.filterButton.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void fetchProducts() {
        mFirestore.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    originalProductList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        originalProductList.add(product);
                    }
                    applyFiltersAndSort();
                });
    }

    private void fetchCategories() {
        mFirestore.collection("categories")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    allCategories.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        allCategories.add(doc.toObject(Category.class));
                    }
                });
    }

    private void applyFiltersAndSort() {
        displayedProductList.clear();
        for (Product product : originalProductList) {
            if (selectedCategory.equalsIgnoreCase("All Categories") || 
                (product.getCategory() != null && product.getCategory().equalsIgnoreCase(selectedCategory))) {
                displayedProductList.add(product);
            }
        }

        if (currentSort.equals("Price: Low to High")) {
            Collections.sort(displayedProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if (currentSort.equals("Price: High to Low")) {
            Collections.sort(displayedProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else if (currentSort.equals("Newest Arrivals")) {
            Collections.sort(displayedProductList, (p1, p2) -> p2.getId().compareTo(p1.getId()));
        }

        binding.itemsCountText.setText(displayedProductList.size() + " Items");
        productAdapter.updateList(displayedProductList);
        
        binding.noDataText.setVisibility(displayedProductList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showSortBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
        dialog.setContentView(view);

        setupDialogOption(dialog, view.findViewById(R.id.sortRecommended), "Recommended", true);
        setupDialogOption(dialog, view.findViewById(R.id.sortNewest), "Newest Arrivals", true);
        setupDialogOption(dialog, view.findViewById(R.id.sortPriceLow), "Price: Low to High", true);
        setupDialogOption(dialog, view.findViewById(R.id.sortPriceHigh), "Price: High to Low", true);

        dialog.show();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        dialog.setContentView(view);

        setupDialogOption(dialog, view.findViewById(R.id.filterAll), "All Categories", false);

        android.widget.LinearLayout container = view.findViewById(R.id.dynamicCategoriesContainer);
        float density = getResources().getDisplayMetrics().density;
        int paddingVertical = (int) (12 * density);

        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        for (Category category : allCategories) {
            String catName = category.getName();
            TextView textView = new TextView(this);
            textView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setPadding(0, paddingVertical, 0, paddingVertical);
            textView.setText(catName);
            textView.setTextSize(16);
            textView.setBackgroundResource(outValue.resourceId);

            setupDialogOption(dialog, textView, catName, false);
            container.addView(textView);
        }

        dialog.show();
    }

    private void setupDialogOption(BottomSheetDialog dialog, TextView textView, String value, boolean isSort) {
        if (textView == null) return;
        boolean isSelected = isSort ? currentSort.equals(value) : selectedCategory.equalsIgnoreCase(value);
        
        if (isSelected) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.brand_pink));
            textView.setTypeface(null, Typeface.BOLD);
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            textView.setTypeface(null, Typeface.NORMAL);
        }

        textView.setOnClickListener(v -> {
            if (isSort) currentSort = value;
            else {
                selectedCategory = value;
                binding.categoryTitle.setText(selectedCategory);
            }
            applyFiltersAndSort();
            dialog.dismiss();
        });
    }
}
