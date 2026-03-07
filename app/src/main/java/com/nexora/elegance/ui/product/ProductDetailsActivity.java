package com.nexora.elegance.ui.product;

import android.os.Bundle;
import android.widget.ImageView;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.nexora.elegance.R;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.ui.adapters.ProductImageAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ProductDetailsActivity displays comprehensive information about a specific
 * product.
 * Features include:
 * - Image slider with dot indicators.
 * - Dynamic variant selection (Color and Size).
 * - Real-time stock status display based on variant selection.
 * - Quantity management.
 * - "Buy Now" (direct to checkout) and "Add to Cart" functionality.
 */
public class ProductDetailsActivity extends AppCompatActivity {

    private Product product;

    private ViewPager2 imageSlider;
    private LinearLayout dotsContainer;
    private ImageView[] dots;
    private TextView productName, productShortDesc, oldPrice, currentPrice;
    private LinearLayout productFeaturesContainer;
    private ImageView btnBack;

    private LinearLayout colorsSection, productColorsContainer;
    private TextView selectedColorText;
    private LinearLayout sizesSection, productSizesContainer;
    private TextView selectedSizeText, productStockText;

    private LinearLayout qtySection;
    private ImageView btnQtyMinus, btnQtyPlus;
    private TextView textQty;

    private Product.VariantColor currentlySelectedColor;
    private Product.VariantSize currentlySelectedSize;
    private int currentQty = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Retrieve product object passed via Intent
        if (getIntent().hasExtra("product")) {
            product = (Product) getIntent().getSerializableExtra("product");
        }

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        imageSlider = findViewById(R.id.productImageSlider);
        dotsContainer = findViewById(R.id.sliderDotsContainer);
        productName = findViewById(R.id.productName);
        productShortDesc = findViewById(R.id.productShortDesc);
        productFeaturesContainer = findViewById(R.id.productFeaturesContainer);
        oldPrice = findViewById(R.id.oldPrice);
        currentPrice = findViewById(R.id.currentPrice);
        btnBack = findViewById(R.id.btnBack);

        colorsSection = findViewById(R.id.colorsSection);
        productColorsContainer = findViewById(R.id.productColorsContainer);
        selectedColorText = findViewById(R.id.selectedColorText);

        sizesSection = findViewById(R.id.sizesSection);
        productSizesContainer = findViewById(R.id.productSizesContainer);
        selectedSizeText = findViewById(R.id.selectedSizeText);
        productStockText = findViewById(R.id.productStockText);

        qtySection = findViewById(R.id.qtySection);
        btnQtyMinus = findViewById(R.id.btnQtyMinus);
        btnQtyPlus = findViewById(R.id.btnQtyPlus);
        textQty = findViewById(R.id.textQty);
    }

    /**
     * Binds the product data to the UI components.
     * Handles images, descriptions, features, and pricing.
     */
    private void setupData() {
        if (product == null)
            return;

        productName.setText(product.getName());
        if (product.getShortDescription() != null && !product.getShortDescription().isEmpty()) {
            productShortDesc.setText(product.getShortDescription());
            productShortDesc.setVisibility(android.view.View.VISIBLE);
        } else {
            productShortDesc.setVisibility(android.view.View.GONE);
        }

        // Dynamically add feature bullet points
        productFeaturesContainer.removeAllViews();
        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            for (String feature : product.getFeatures()) {
                TextView featureView = new TextView(this);
                featureView.setText("•  " + feature);
                featureView.setTextColor(android.graphics.Color.parseColor("#424242"));
                featureView.setTextSize(12);
                featureView.setPadding(0, 4, 0, 4);
                productFeaturesContainer.addView(featureView);
            }
        }

        currentPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice()));
        oldPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice() * 2));
        oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Configure Image ViewPager
        List<String> displayImages = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            displayImages.addAll(product.getImageUrls());
        }
        if (displayImages.isEmpty() && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            displayImages.add(product.getImageUrl());
        }

        ProductImageAdapter adapter = new ProductImageAdapter(displayImages);
        imageSlider.setAdapter(adapter);

        setupDots(displayImages.size());

        // Setup variants if available
        List<Product.VariantColor> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            colorsSection.setVisibility(android.view.View.VISIBLE);
            setupColors(variants);
        } else {
            colorsSection.setVisibility(android.view.View.GONE);
            sizesSection.setVisibility(android.view.View.GONE);
            qtySection.setVisibility(android.view.View.GONE);
        }
    }

    /**
     * Initializes and maintains the circle indicators for the image carousel.
     */
    private void setupDots(int count) {
        if (dotsContainer == null || count == 0)
            return;
        dots = new ImageView[count];
        dotsContainer.removeAllViews();

        int size = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(size / 2, 0, size / 2, 0);

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.bg_white_button);
            dots[i].setColorFilter(android.graphics.Color.parseColor("#E0E0E0"));
            dots[i].setLayoutParams(params);
            dotsContainer.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[0].setColorFilter(android.graphics.Color.parseColor("#FD6E8A"));
        }

        imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < dots.length; i++) {
                    if (i == position) {
                        dots[i].setColorFilter(android.graphics.Color.parseColor("#FD6E8A"));
                    } else {
                        dots[i].setColorFilter(android.graphics.Color.parseColor("#E0E0E0"));
                    }
                }
            }
        });
    }

    /**
     * Renders color selection chips. Changing color resets the selected size.
     */
    private void setupColors(List<Product.VariantColor> variants) {
        productColorsContainer.removeAllViews();
        if (variants.isEmpty())
            return;

        if (currentlySelectedColor == null)
            currentlySelectedColor = variants.get(0);
        selectedColorText.setText("Color: " + currentlySelectedColor.getColor());

        for (Product.VariantColor vc : variants) {
            TextView chip = createChip(vc.getColor(), vc == currentlySelectedColor);
            chip.setOnClickListener(v -> {
                currentlySelectedColor = vc;
                currentlySelectedSize = null;
                setupColors(variants);
                setupSizes(currentlySelectedColor);
            });
            productColorsContainer.addView(chip);
        }

        setupSizes(currentlySelectedColor);
    }

    /**
     * Renders size selection chips for the currently selected color.
     */
    private void setupSizes(Product.VariantColor colorVariant) {
        productSizesContainer.removeAllViews();
        List<Product.VariantSize> sizes = colorVariant.getSizes();
        if (sizes == null || sizes.isEmpty()) {
            sizesSection.setVisibility(android.view.View.GONE);
            return;
        }

        sizesSection.setVisibility(android.view.View.VISIBLE);
        if (currentlySelectedSize == null)
            currentlySelectedSize = sizes.get(0);
        updateSizeSelection();

        for (Product.VariantSize vs : sizes) {
            TextView chip = createChip(vs.getSize(), vs == currentlySelectedSize);
            if (vs.getQuantity() == 0) {
                chip.setAlpha(0.5f);
            }
            chip.setOnClickListener(v -> {
                currentlySelectedSize = vs;
                updateSizeSelection();
                setupSizes(colorVariant);
            });
            productSizesContainer.addView(chip);
        }
    }

    private void updateSizeSelection() {
        if (currentlySelectedSize != null) {
            selectedSizeText.setText("Size: " + currentlySelectedSize.getSize());
            productStockText.setText(currentlySelectedSize.getQuantity() > 0
                    ? "In Stock: " + currentlySelectedSize.getQuantity()
                    : "Out of Stock");

            // Clamp quantity down if stock is lower than current selection
            if (currentlySelectedSize.getQuantity() < currentQty) {
                currentQty = Math.max(1, currentlySelectedSize.getQuantity());
            } else if (currentQty == 0 && currentlySelectedSize.getQuantity() > 0) {
                currentQty = 1;
            }
            updateQtyUI();
        }
    }

    private void updateQtyUI() {
        textQty.setText(String.valueOf(currentQty));
    }

    private TextView createChip(String text, boolean isActive) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), 0);
        chip.setLayoutParams(params);
        chip.setText(text);
        chip.setTextSize(12);

        int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
        int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
        chip.setPadding(paddingH, paddingV, paddingH, paddingV);

        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_active_indicator);
            chip.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FD6E8A")));
            chip.setTextColor(android.graphics.Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_white_button);
            chip.setTextColor(android.graphics.Color.parseColor("#FD6E8A"));
        }

        return chip;
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnQtyMinus.setOnClickListener(v -> {
            if (currentQty > 1) {
                currentQty--;
                updateQtyUI();
            }
        });

        btnQtyPlus.setOnClickListener(v -> {
            int maxStock = (currentlySelectedSize != null) ? currentlySelectedSize.getQuantity() : 1;
            if (currentQty < maxStock) {
                currentQty++;
                updateQtyUI();
            } else {
                Toast.makeText(this, "Maximum available stock is " + maxStock, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
            if (product != null) {
                // Determine color explicitly
                String colorStr = "";
                if (currentlySelectedColor != null && currentlySelectedColor.getColor() != null) {
                    colorStr = currentlySelectedColor.getColor();
                } else if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                    colorStr = product.getVariants().get(0).getColor();
                }

                // Determine size explicitly
                String sizeStr = "";
                if (currentlySelectedSize != null && currentlySelectedSize.getSize() != null) {
                    sizeStr = currentlySelectedSize.getSize();
                }

                int maxStock = (currentlySelectedSize != null) ? currentlySelectedSize.getQuantity() : 0;

                if (maxStock > 0 && currentQty > 0) {
                    String finalColor = colorStr;
                    String finalSize = sizeStr;
                    addToCartNatively(finalColor, finalSize, maxStock);
                } else {
                    Toast.makeText(this, "This variant is currently out of stock.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnBuyNow).setOnClickListener(v -> {
            if (product != null) {
                int maxStock = (currentlySelectedSize != null) ? currentlySelectedSize.getQuantity() : 0;
                if (maxStock == 0) {
                    Toast.makeText(this, "Variant is out of stock.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String colorStr = (currentlySelectedColor != null) ? currentlySelectedColor.getColor() : "";
                String sizeStr = (currentlySelectedSize != null) ? currentlySelectedSize.getSize() : "";

                java.util.List<String> availableColors = new java.util.ArrayList<>();
                java.util.List<String> availableSizes = new java.util.ArrayList<>();
                java.util.Map<String, Integer> stockMap = new java.util.HashMap<>();

                if (product.getVariants() != null) {
                    for (Product.VariantColor vc : product.getVariants()) {
                        availableColors.add(vc.getColor());
                        if (vc.getSizes() != null) {
                            for (Product.VariantSize vs : vc.getSizes()) {
                                if (!availableSizes.contains(vs.getSize())) {
                                    availableSizes.add(vs.getSize());
                                }
                                String mapKey = vc.getColor() + "_" + vs.getSize();
                                stockMap.put(mapKey, vs.getQuantity());
                            }
                        }
                    }
                }

                com.nexora.elegance.data.models.CartItem activeItem = new com.nexora.elegance.data.models.CartItem(
                        product.getId(),
                        product.getId(),
                        product.getName(),
                        product.getCategory() != null ? product.getCategory() : "",
                        product.getPrice(),
                        product.getImageUrl(),
                        currentQty,
                        sizeStr,
                        colorStr,
                        availableSizes,
                        availableColors,
                        stockMap);

                java.util.List<com.nexora.elegance.data.models.CartItem> customCartList = new java.util.ArrayList<>();
                customCartList.add(activeItem);

                android.content.Intent intent = new android.content.Intent(this,
                        com.nexora.elegance.ui.checkout.CheckoutActivity.class);
                intent.putExtra("checkout_list", new java.util.ArrayList<>(customCartList));
                startActivity(intent);
            }
        });
    }

    /**
     * Adds the selected variant to the user's cart in Firestore.
     * 
     * @param colorStr Selected color name.
     * @param sizeStr  Selected size name.
     * @param maxStock Available stock for the selected variant.
     */
    private void addToCartNatively(String colorStr, String sizeStr, int maxStock) {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();

        // Build comprehensive maps for the CartItem model
        java.util.List<String> availableColors = new java.util.ArrayList<>();
        java.util.List<String> availableSizes = new java.util.ArrayList<>();
        java.util.Map<String, Integer> stockMap = new java.util.HashMap<>();

        if (product.getVariants() != null) {
            for (Product.VariantColor vc : product.getVariants()) {
                availableColors.add(vc.getColor());
                if (vc.getSizes() != null) {
                    for (Product.VariantSize vs : vc.getSizes()) {
                        if (!availableSizes.contains(vs.getSize())) {
                            availableSizes.add(vs.getSize());
                        }
                        String mapKey = vc.getColor() + "_" + vs.getSize();
                        stockMap.put(mapKey, vs.getQuantity());
                    }
                }
            }
        }

        // Create Cart Item and persist to Firestore
        com.nexora.elegance.data.models.CartItem cartItem = new com.nexora.elegance.data.models.CartItem(
                product.getId() + "_" + System.currentTimeMillis(),
                product.getId(),
                product.getName(),
                product.getCategory() != null ? product.getCategory() : "",
                product.getPrice(),
                product.getImageUrl(),
                currentQty,
                sizeStr,
                colorStr,
                availableSizes,
                availableColors,
                stockMap);

        firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(cartItem.getProductId())
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Item successfully added to cart!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
