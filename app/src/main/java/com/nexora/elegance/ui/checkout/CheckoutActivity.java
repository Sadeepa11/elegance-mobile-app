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
import com.nexora.elegance.data.models.CartItem;
import com.nexora.elegance.data.models.Order;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.ui.adapters.CheckoutAdapter;
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

    private void initViews() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        checkoutTotalText = findViewById(R.id.checkoutTotalText);
        btnBack = findViewById(R.id.btnBack);
        btnPayHere = findViewById(R.id.btnPayHere);

        addressDetails = findViewById(R.id.addressDetails);
        btnEditAddress = findViewById(R.id.btnEditAddress);
        btnAddAddressCard = findViewById(R.id.btnAddAddressCard);
    }

    private void setupRecyclerView() {
        adapter = new CheckoutAdapter(this, checkoutList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

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

            InitRequest req = new InitRequest();
            req.setMerchantId("1234228"); // Sandbox Merchant ID
            req.setCurrency("LKR");
            req.setAmount(total);
            req.setOrderId(UUID.randomUUID().toString());
            req.setItemsDescription(topItemName + (checkoutList.size() > 1 ? " and others" : ""));
            req.setCustom1("Elegance App Purchase");
            req.setCustom2("");

            // Note: Since we don't have first/last name splitting, using dummy mapping
            // explicitly.
            req.getCustomer().setFirstName("Elegance");
            req.getCustomer().setLastName("Customer");
            req.getCustomer().setEmail(user.getEmail() != null ? user.getEmail() : "customer@elegance.com");
            req.getCustomer().setPhone("+94770000000"); // Using default explicit numbers mapping
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

    private void clearCartAndExit(String uid) {
        if (!getIntent().hasExtra("checkout_list")) {
            // It was a single product instantly bought; no cart to clear explicitly.
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

    private void loadData() {
        if (getIntent().hasExtra("checkout_list")) {
            // Load custom checkout list containing explicitly picked sizes, colors, and
            // qtys
            java.util.List<CartItem> customList = (java.util.List<CartItem>) getIntent()
                    .getSerializableExtra("checkout_list");
            if (customList != null) {
                checkoutList.clear();
                checkoutList.addAll(customList);
                adapter.notifyDataSetChanged();
                updateTotal();
            }
        } else if (getIntent().hasExtra("product")) {
            // Legacy load single product from intent (baseline specs)
            Product product = (Product) getIntent().getSerializableExtra("product");
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

                    // Route user explicitly mapping the newly placed receipt
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
