package com.nexora.elegance.ui.checkout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nexora.elegance.R;
import com.nexora.elegance.models.CartItem;
import com.nexora.elegance.models.Order;
import com.nexora.elegance.models.Product;
import com.nexora.elegance.adapters.CheckoutAdapter;
import com.nexora.elegance.ui.orders.OrderDetailsActivity;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.nexora.elegance.models.UserModel;
import com.nexora.elegance.ui.checkout.CheckoutAddressBottomSheet;

/**
 * CheckoutActivity handles the final purchase flow.
 * It manages:
 * - Shipping address selection (via BottomSheet or User Profile).
 * - Review of items to be purchased.
 * - Integration with PayHere Payment Gateway.
 * - Persistence of the order to Firestore.
 * - Clearing the cart post-purchase.
 */
public class CheckoutActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001;
    private static final String TAG = "CheckoutActivity";

    private RecyclerView checkoutRecyclerView;
    private TextView checkoutTotalText;
    private ImageView btnBack;
    private View btnPayHere;

    private CheckoutAdapter adapter;
    private List<CartItem> checkoutList = new ArrayList<>();

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private TextView addressDetails;
    private ImageView btnEditAddress;
    private View btnAddAddressCard;

    private String currentAddress = "";
    private String currentCity = "";
    private String currentDistrict = "";
    private String currentProvince = "";
    private String currentCountry = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    /**
     * Finds and initializes various UI components.
     */
    private void initViews() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        checkoutTotalText = findViewById(R.id.checkoutTotalText);
        btnBack = findViewById(R.id.btnBack);
        btnPayHere = findViewById(R.id.btnPayHere);

        addressDetails = findViewById(R.id.addressDetails);
        btnEditAddress = findViewById(R.id.btnEditAddress);
        btnAddAddressCard = findViewById(R.id.btnAddAddressCard);
    }

    /**
     * Configures the RecyclerView for displaying items in the checkout summary.
     */
    private void setupRecyclerView() {
        adapter = new CheckoutAdapter(this, checkoutList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Initialize PayHere payment request
        btnPayHere.setOnClickListener(v -> {
            if (checkoutList.isEmpty()) {
                Toast.makeText(this, "Your checkout is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null || currentAddress.isEmpty()) {
                Toast.makeText(this, "Please add a shipping address", Toast.LENGTH_SHORT).show();
                return;
            }

            double total = 0;
            String topItemName = checkoutList.get(0).getName();
            for (CartItem item : checkoutList) {
                int qty = Math.max(1, item.getQuantity());
                total += (item.getPrice() * qty);
            }

            // Prepare InitRequest for PayHere SDK
            InitRequest req = new InitRequest();
            req.setMerchantId("1234228"); // Mock Merchant ID
            req.setCurrency("LKR");
            req.setAmount(total);
            req.setOrderId(UUID.randomUUID().toString());
            req.setItemsDescription(topItemName + (checkoutList.size() > 1 ? " and others" : ""));
            req.setCustom1("Elegance App Purchase");
            req.setCustom2("");

            // Map user details to the request
            req.getCustomer().setFirstName("Elegance");
            req.getCustomer().setLastName("Customer");
            req.getCustomer().setEmail(user.getEmail() != null ? user.getEmail() : "customer@elegance.com");
            req.getCustomer().setPhone("+94770000000");
            req.getCustomer().getAddress().setAddress(currentAddress);
            req.getCustomer().getAddress().setCity(currentCity.isEmpty() ? "Colombo" : currentCity);
            req.getCustomer().getAddress().setCountry(currentCountry.isEmpty() ? "Sri Lanka" : currentCountry);

            for (CartItem item : checkoutList) {
                req.getItems().add(new Item(item.getProductId(), item.getName(), item.getQuantity(), item.getPrice()));
            }

            Intent intent = new Intent(this, PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
            startActivityForResult(intent, PAYHERE_REQUEST);
        });

        View.OnClickListener editAddressListener = v -> {
            CheckoutAddressBottomSheet bottomSheet = new CheckoutAddressBottomSheet(
                    currentAddress, currentCity, currentDistrict, currentProvince, currentCountry);
            bottomSheet.setOnAddressSavedListener((address, city, district, province, country) -> {
                currentAddress = address;
                currentCity = city;
                currentDistrict = district;
                currentProvince = province;
                currentCountry = country;

                updateAddressUI();
            });
            bottomSheet.show(getSupportFragmentManager(), "CheckoutAddressBottomSheet");
        };

        btnEditAddress.setOnClickListener(editAddressListener);
        btnAddAddressCard.setOnClickListener(editAddressListener);
    }

    /**
     * Clears the user's cart in Firestore after a successful order.
     */
    private void clearCartAndExit(String uid) {
        if (!getIntent().hasExtra("checkout_list")) {
            finish();
            return;
        }

        mFirestore.collection("users").document(uid).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    finish();
                })
                .addOnFailureListener(e -> finish());
    }

    /**
     * Loads the items to be checked out from the Intent.
     * Supports both multi-item cart and single-product "Buy Now".
     */
    private void loadData() {
        if (getIntent().hasExtra("checkout_list")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                java.util.List<com.nexora.elegance.models.CartItem> customList = getIntent().getSerializableExtra("checkout_list", java.util.ArrayList.class);
                if (customList != null) {
                    checkoutList.clear();
                    checkoutList.addAll(customList);
                }
            } else {
                java.util.List<com.nexora.elegance.models.CartItem> customList = (java.util.List<com.nexora.elegance.models.CartItem>) getIntent().getSerializableExtra("checkout_list");
                if (customList != null) {
                    checkoutList.clear();
                    checkoutList.addAll(customList);
                }
            }
            adapter.notifyDataSetChanged();
            updateTotal();
        } else if (getIntent().hasExtra("product")) {
            Product product;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                product = getIntent().getSerializableExtra("product", Product.class);
            } else {
                product = (Product) getIntent().getSerializableExtra("product");
            }
            if (product != null) {
                CartItem item = new CartItem();
                item.setProductId(product.getId());
                item.setName(product.getName());
                item.setCategory(product.getCategory() != null ? product.getCategory() : "");
                item.setPrice(product.getPrice());
                item.setImageUrl(product.getImageUrl());
                item.setQuantity(1);

                checkoutList.clear();
                checkoutList.add(item);
                adapter.notifyDataSetChanged();
                updateTotal();
            }
        }

        loadUserAddress();
    }

    private void loadUserAddress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        mFirestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null) {
                            currentAddress = userModel.getAddress() != null ? userModel.getAddress() : "";
                            currentCity = userModel.getCity() != null ? userModel.getCity() : "";
                            currentDistrict = userModel.getDistrict() != null ? userModel.getDistrict() : "";
                            currentProvince = userModel.getState() != null ? userModel.getState() : "";
                            currentCountry = userModel.getCountry() != null ? userModel.getCountry() : "";

                            updateAddressUI();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user address limit", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAddressUI() {
        StringBuilder sb = new StringBuilder();

        if (!currentAddress.isEmpty())
            sb.append(currentAddress).append("\n");

        List<String> locParts = new ArrayList<>();
        if (!currentCity.isEmpty())
            locParts.add(currentCity);
        if (!currentDistrict.isEmpty())
            locParts.add(currentDistrict);
        if (!currentProvince.isEmpty())
            locParts.add(currentProvince);

        if (!locParts.isEmpty()) {
            sb.append(String.join(", ", locParts)).append("\n");
        }

        if (!currentCountry.isEmpty())
            sb.append(currentCountry);

        String finalAddress = sb.toString().trim();
        if (finalAddress.isEmpty()) {
            addressDetails.setText("Please tap + to add a delivery address.");
        } else {
            addressDetails.setText(finalAddress);
        }
    }

    // loadCartFromFirebase method removed explicitly so Checkout relies entirely
    // upon passed Intents

    private void updateTotal() {
        double total = 0;
        for (CartItem item : checkoutList) {
            int qty = Math.max(1, item.getQuantity());
            total += (item.getPrice() * qty);
        }
        checkoutTotalText.setText(String.format(Locale.getDefault(), "LKR %.2f", total));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data
                    .getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                if (response != null && response.isSuccess()) {
                    Log.d(TAG, "Payment Success: " + response.getData().toString());
                    finalizeOrderPlacement();
                } else {
                    Log.e(TAG, "Payment Result Failed: " + (response != null ? response.toString() : "No response"));
                    Toast.makeText(this, "Payment was not successful. Please try again.", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    Log.d(TAG, "Payment Canceled: " + response.toString());
                    Toast.makeText(this, "Payment Canceled: " + response.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "User canceled the request");
                    Toast.makeText(this, "Payment was canceled by user.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Finalizes the order object and saves it to Firestore.
     * Navigates to OrderDetailsActivity upon completion.
     */
    private void finalizeOrderPlacement() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        double total = 0;
        for (CartItem item : checkoutList) {
            int qty = Math.max(1, item.getQuantity());
            total += (item.getPrice() * qty);
        }

        String orderId = UUID.randomUUID().toString();
        String fullAddress = addressDetails.getText().toString();
        long timestamp = System.currentTimeMillis();

        // Create Order model with list of items
        Order newOrder = new Order(
                orderId,
                user.getUid(),
                timestamp,
                "Processing",
                total,
                fullAddress,
                "PayHere",
                new ArrayList<>(checkoutList));

        mFirestore.collection("users").document(user.getUid())
                .collection("orders").document(orderId)
                .set(newOrder)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to the Order Detail view for the newly created order
                    Intent intent = new Intent(CheckoutActivity.this, OrderDetailsActivity.class);
                    intent.putExtra("order", newOrder);
                    startActivity(intent);

                    clearCartAndExit(user.getUid());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
