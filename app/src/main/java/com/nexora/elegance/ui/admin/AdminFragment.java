package com.nexora.elegance.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.databinding.FragmentAdminBinding;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addProductButton.setOnClickListener(v -> {
            String name = binding.prodNameEdit.getText().toString();
            String priceStr = binding.prodPriceEdit.getText().toString();
            double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
            String category = binding.prodCategoryEdit.getText().toString();
            String stockStr = binding.prodStockEdit.getText().toString();
            int stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);

            if (!name.isEmpty() && !category.isEmpty()) {
                Product product = new Product(
                        String.valueOf(System.currentTimeMillis()),
                        name,
                        "",
                        price,
                        "",
                        category,
                        "M",
                        "Red",
                        stock,
                        "",
                        new java.util.ArrayList<String>(),
                        new java.util.ArrayList<String>(),
                        new java.util.ArrayList<Product.VariantColor>());
                // Save logic
                Toast.makeText(getContext(), "Product Added Successfully (Java)", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        binding.prodNameEdit.setText("");
        binding.prodPriceEdit.setText("");
        binding.prodCategoryEdit.setText("");
        binding.prodStockEdit.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
