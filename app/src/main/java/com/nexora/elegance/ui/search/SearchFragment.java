package com.nexora.elegance.ui.search;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.nexora.elegance.R;
import com.nexora.elegance.data.models.Category;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.databinding.FragmentSearchBinding;
import com.nexora.elegance.ui.adapters.SearchAdapter;
import com.nexora.elegance.ui.product.ProductDetailsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private FirebaseFirestore mFirestore;
    private SearchAdapter searchAdapter;
    private List<Product> originalProductList = new ArrayList<>();
    private List<Product> displayedProductList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private String currentSort = "Recommended";
    private String currentCategory = "All Categories";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        setupRecyclerView();
        setupButtons();
        fetchProducts();
        fetchCategories();

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

    private void setupButtons() {
        binding.sortButton.setOnClickListener(v -> showSortBottomSheet());
        binding.filterButton.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showSortBottomSheet() {
        if (getContext() == null)
            return;
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
        dialog.setContentView(view);

        TextView optRecommended = view.findViewById(R.id.sortRecommended);
        TextView optNewest = view.findViewById(R.id.sortNewest);
        TextView optLow = view.findViewById(R.id.sortPriceLow);
        TextView optHigh = view.findViewById(R.id.sortPriceHigh);

        setupDialogOption(dialog, optRecommended, "Recommended", true);
        setupDialogOption(dialog, optNewest, "Newest Arrivals", true);
        setupDialogOption(dialog, optLow, "Price: Low to High", true);
        setupDialogOption(dialog, optHigh, "Price: High to Low", true);

        // Make background transparent if bottom sheet behaves weirdly, but usually fine
        dialog.show();
    }

    private void fetchCategories() {
        mFirestore.collection("categories")
                .addSnapshotListener((value, error) -> {
                    if (binding == null)
                        return;
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Category category = doc.toObject(Category.class);
                            categoryList.add(category);
                        }
                    }
                });
    }

    private void showFilterBottomSheet() {
        if (getContext() == null)
            return;
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        dialog.setContentView(view);

        TextView optAll = view.findViewById(R.id.filterAll);
        setupDialogOption(dialog, optAll, "All Categories", false);

        android.widget.LinearLayout container = view.findViewById(R.id.dynamicCategoriesContainer);
        float density = getResources().getDisplayMetrics().density;
        int paddingVertical = (int) (12 * density);

        android.util.TypedValue outValue = new android.util.TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        for (Category category : categoryList) {
            String catName = category.getName();
            TextView textView = new TextView(getContext());
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
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
        if (textView == null)
            return;
        boolean isSelected = isSort ? currentSort.equals(value) : currentCategory.equals(value);
        if (isSelected) {
            textView.setTextColor(Color.parseColor("#9C4258"));
            textView.setTypeface(null, Typeface.BOLD);
        } else {
            textView.setTextColor(Color.parseColor("#1A1A1A"));
            textView.setTypeface(null, Typeface.NORMAL);
        }

        textView.setOnClickListener(v -> {
            if (isSort)
                currentSort = value;
            else
                currentCategory = value;

            applyFiltersAndSort();
            dialog.dismiss();
        });
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchAdapter(displayedProductList, product -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), ProductDetailsActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }
        });

        // Use StaggeredGridLayoutManager to mimic the UI correctly
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.searchRecyclerView.setLayoutManager(layoutManager);
        binding.searchRecyclerView.setAdapter(searchAdapter);
    }

    private void fetchProducts() {
        mFirestore.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (binding == null)
                        return;
                    if (error != null) {
                        return; // Handle error silently or show a toast
                    }

                    if (value != null) {
                        originalProductList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());
                            originalProductList.add(product);
                        }

                        applyFiltersAndSort();
                    }
                });
    }

    private void applyFiltersAndSort() {
        displayedProductList.clear();
        for (Product product : originalProductList) {
            if (currentCategory.equals("All Categories") ||
                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase(currentCategory))) {
                displayedProductList.add(product);
            }
        }

        if (currentSort.equals("Price: Low to High")) {
            Collections.sort(displayedProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if (currentSort.equals("Price: High to Low")) {
            Collections.sort(displayedProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else if (currentSort.equals("Newest Arrivals")) {
            // Basic approximation: sort by ID descending (mocking newest)
            Collections.sort(displayedProductList, (p1, p2) -> p2.getId().compareTo(p1.getId()));
        }

        binding.itemsCountText.setText(displayedProductList.size() + "+ Items");
        searchAdapter.updateList(displayedProductList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
