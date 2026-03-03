package com.nexora.elegance.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.CartItem;
import com.nexora.elegance.data.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {

    private Product product;

    private ImageView modalProductImage;
    private TextView modalProductName;
    private TextView modalProductPrice;
    private Spinner spinnerModalColor;
    private Spinner spinnerModalSize;
    private EditText etModalQty;
    private MaterialButton btnConfirmAddToCart;

    private List<String> availableColors = new ArrayList<>();
    private List<String> availableSizes = new ArrayList<>();

    private int currentMaxQty = 1;

    public AddToCartBottomSheet(Product product) {
        this.product = product;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_to_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modalProductImage = view.findViewById(R.id.modalProductImage);
        modalProductName = view.findViewById(R.id.modalProductName);
        modalProductPrice = view.findViewById(R.id.modalProductPrice);
        spinnerModalColor = view.findViewById(R.id.spinnerModalColor);
        spinnerModalSize = view.findViewById(R.id.spinnerModalSize);
        etModalQty = view.findViewById(R.id.etModalQty);
        btnConfirmAddToCart = view.findViewById(R.id.btnConfirmAddToCart);

        setupData();
        setupListeners();
    }

    private void setupData() {
        if (product == null)
            return;

        modalProductName.setText(product.getName());
        modalProductPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice()));

        Glide.with(requireContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_logo)
                .into(modalProductImage);

        // Extract colors
        if (product.getVariants() != null) {
            for (Product.VariantColor vc : product.getVariants()) {
                availableColors.add(vc.getColor());
            }
        }

        if (availableColors.isEmpty()) {
            availableColors.add("Default");
        }

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item,
                availableColors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModalColor.setAdapter(colorAdapter);

        spinnerModalColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSizeSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerModalSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateQtyLimit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Trigger default sizes
        updateSizeSpinner(0);
    }

    private void updateSizeSpinner(int colorIndex) {
        availableSizes.clear();

        if (product.getVariants() != null && colorIndex < product.getVariants().size()) {
            Product.VariantColor vc = product.getVariants().get(colorIndex);
            if (vc.getSizes() != null) {
                for (Product.VariantSize vs : vc.getSizes()) {
                    // Only show in stock
                    // if (vs.getQuantity() > 0)
                    availableSizes.add(vs.getSize());
                }
            }
        }

        if (availableSizes.isEmpty()) {
            availableSizes.add("Default");
        }

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item,
                availableSizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModalSize.setAdapter(sizeAdapter);

        updateQtyLimit();
    }

    private void updateQtyLimit() {
        int maxQty = 1;

        if (product.getVariants() != null) {
            int colorIndex = spinnerModalColor.getSelectedItemPosition();
            if (colorIndex >= 0 && colorIndex < product.getVariants().size()) {
                Product.VariantColor vc = product.getVariants().get(colorIndex);
                if (vc.getSizes() != null) {
                    int sizeIndex = spinnerModalSize.getSelectedItemPosition();
                    if (sizeIndex >= 0 && sizeIndex < vc.getSizes().size()) {
                        Product.VariantSize vs = vc.getSizes().get(sizeIndex);
                        maxQty = vs.getQuantity();
                    }
                }
            }
        }

        if (maxQty <= 0) {
            maxQty = 1; // Fallback so user can at least try or we show out of stock elsewhere
        }
        currentMaxQty = maxQty;

        // Verify current input against new maxQty
        String currentText = etModalQty.getText().toString();
        if (!currentText.isEmpty()) {
            try {
                int qty = Integer.parseInt(currentText);
                if (qty > currentMaxQty) {
                    etModalQty.setText(String.valueOf(currentMaxQty));
                    etModalQty.setSelection(etModalQty.getText().length());
                }
            } catch (NumberFormatException e) {
                etModalQty.setText("1");
                etModalQty.setSelection(etModalQty.getText().length());
            }
        }
    }

    private void setupListeners() {
        btnConfirmAddToCart.setOnClickListener(v -> saveToFirebase());

        etModalQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if (val.isEmpty())
                    return; // allow empty while typing
                try {
                    int qty = Integer.parseInt(val);
                    if (qty > currentMaxQty) {
                        etModalQty.setText(String.valueOf(currentMaxQty));
                        etModalQty.setSelection(etModalQty.getText().length());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Max available is " + currentMaxQty, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else if (qty == 0) {
                        etModalQty.setText("1");
                        etModalQty.setSelection(etModalQty.getText().length());
                    }
                } catch (NumberFormatException e) {
                    etModalQty.setText("1");
                    etModalQty.setSelection(etModalQty.getText().length());
                }
            }
        });
    }

    private void saveToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        String qtyStr = etModalQty.getText().toString();
        if (qtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedQty = Integer.parseInt(qtyStr);
        if (selectedQty <= 0)
            selectedQty = 1;
        if (selectedQty > currentMaxQty)
            selectedQty = currentMaxQty;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String selectedColor = (String) spinnerModalColor.getSelectedItem();
        String selectedSize = (String) spinnerModalSize.getSelectedItem();

        // We use a combination of Product ID + Size + Color as the cart doc ID to merge
        // identical items
        String cartItemId = product.getId() + "_" + selectedColor.replaceAll("\\s+", "") + "_"
                + selectedSize.replaceAll("\\s+", "");

        // Generate stockMap to snapshot maximum quantities per variant
        Map<String, Integer> stockMap = new HashMap<>();
        if (product.getVariants() != null) {
            for (Product.VariantColor vc : product.getVariants()) {
                String c = vc.getColor() != null ? vc.getColor() : "Default";
                if (vc.getSizes() != null) {
                    for (Product.VariantSize vs : vc.getSizes()) {
                        String s = vs.getSize() != null ? vs.getSize() : "Default";
                        stockMap.put(c + "_" + s, vs.getQuantity());
                    }
                }
            }
        }

        CartItem item = new CartItem(
                cartItemId,
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                selectedQty,
                selectedSize,
                selectedColor,
                availableSizes,
                availableColors,
                stockMap);

        db.collection("users").document(uid).collection("cart")
                .document(cartItemId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Added to Cart successfully", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
