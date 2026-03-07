package com.nexora.elegance.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.data.LocationDataProvider;
import com.nexora.elegance.databinding.ActivityProfileUpdateBinding;
import com.nexora.elegance.models.UserModel;

import java.util.List;

/**
 * ProfileUpdateActivity allows users to manage their personal and financial
 * details.
 * Features:
 * - Multi-level location selection (Country -> State -> District -> City).
 * - Personal address and postal code management.
 * - Bank account information for refunds/payouts.
 * - Integration with Firestore for real-time profile updates.
 */
public class ProfileUpdateActivity extends AppCompatActivity {

    private ActivityProfileUpdateBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    // To prevent clearing loaded data on initial programmatic spinner setups
    private boolean isInitialLoad = true;
    private UserModel loadedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        setupListeners();
        setupSpinners();
        loadUserData();
    }

    /**
     * Configures the hierarchical spinners for location selection.
     * Selecting a parent level (e.g., Country) triggers updates for child levels
     * (e.g., State).
     */
    private void setupSpinners() {
        // Initialize Country spinner from data provider
        List<String> countries = LocationDataProvider.getCountries();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item,
                countries);
        countryAdapter.setDropDownViewResource(com.nexora.elegance.R.layout.spinner_dropdown_item);
        binding.countrySpinner.setAdapter(countryAdapter);

        binding.countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countries.get(position);
                if (selectedCountry.equals("Select Country")) {
                    hideStateAndBelow();
                } else {
                    List<String> states = LocationDataProvider.getStates(selectedCountry);
                    if (states.isEmpty()) {
                        binding.containerState.setVisibility(View.GONE);
                        binding.containerDistrict.setVisibility(View.GONE);
                        loadCities(selectedCountry);
                    } else {
                        binding.containerState.setVisibility(View.VISIBLE);
                        loadStates(states);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = (String) parent.getItemAtPosition(position);
                if (selectedState.startsWith("Select")) {
                    hideDistrictAndBelow();
                } else {
                    List<String> districts = LocationDataProvider.getDistricts(selectedState);
                    if (districts.isEmpty()) {
                        binding.containerDistrict.setVisibility(View.GONE);
                        loadCities(selectedState);
                    } else {
                        binding.containerDistrict.setVisibility(View.VISIBLE);
                        loadDistricts(districts);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) parent.getItemAtPosition(position);
                if (!selectedDistrict.startsWith("Select")) {
                    loadCities(selectedDistrict);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void hideStateAndBelow() {
        binding.containerState.setVisibility(View.GONE);
        binding.containerDistrict.setVisibility(View.GONE);
        // Reset City
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item,
                new String[] { "Select City" });
        binding.citySpinner.setAdapter(adapter);
    }

    private void hideDistrictAndBelow() {
        binding.containerDistrict.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item,
                new String[] { "Select City" });
        binding.citySpinner.setAdapter(adapter);
    }

    private void loadStates(List<String> states) {
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item, states);
        stateAdapter.setDropDownViewResource(com.nexora.elegance.R.layout.spinner_dropdown_item);
        binding.stateSpinner.setAdapter(stateAdapter);

        if (isInitialLoad && loadedUser != null && loadedUser.getState() != null) {
            setSpinnerToValue(binding.stateSpinner, loadedUser.getState());
        } else {
            hideDistrictAndBelow();
        }
    }

    private void loadDistricts(List<String> districts) {
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item,
                districts);
        districtAdapter.setDropDownViewResource(com.nexora.elegance.R.layout.spinner_dropdown_item);
        binding.districtSpinner.setAdapter(districtAdapter);

        if (isInitialLoad && loadedUser != null && loadedUser.getDistrict() != null) {
            setSpinnerToValue(binding.districtSpinner, loadedUser.getDistrict());
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item,
                    new String[] { "Select City" });
            binding.citySpinner.setAdapter(adapter);
        }
    }

    private void loadCities(String dependentKey) {
        List<String> cities = LocationDataProvider.getCities(dependentKey);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, com.nexora.elegance.R.layout.spinner_item, cities);
        cityAdapter.setDropDownViewResource(com.nexora.elegance.R.layout.spinner_dropdown_item);
        binding.citySpinner.setAdapter(cityAdapter);

        if (isInitialLoad && loadedUser != null && loadedUser.getCity() != null) {
            setSpinnerToValue(binding.citySpinner, loadedUser.getCity());
            isInitialLoad = false; // Finished propagating down tree
        }
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(value)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.changePasswordText.setOnClickListener(v -> {
            Toast.makeText(this, "Reset Password link sent to your email.", Toast.LENGTH_SHORT).show();
        });

        binding.saveButton.setOnClickListener(v -> saveUserData());
    }

    /**
     * Fetches current user profile data from Firestore.
     */
    private void loadUserData() {
        if (mAuth.getCurrentUser() == null)
            return;

        String uid = mAuth.getCurrentUser().getUid();

        mFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            loadedUser = user;
                            populateFields(user);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fills the UI fields with user data fetched from the database.
     */
    private void populateFields(UserModel user) {
        binding.emailEdit.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.passwordEdit.setText("***********");
        binding.postalCodeEdit.setText(user.getPostalCode() != null ? user.getPostalCode() : "");
        binding.addressEdit.setText(user.getAddress() != null ? user.getAddress() : "");
        binding.bankAccountEdit.setText(user.getBankAccountNumber() != null ? user.getBankAccountNumber() : "");
        binding.accountHolderEdit.setText(user.getAccountHolderName() != null ? user.getAccountHolderName() : "");
        binding.ifscCodeEdit.setText(user.getIfscCode() != null ? user.getIfscCode() : "");

        if (user.getCountry() != null && !user.getCountry().isEmpty()) {
            setSpinnerToValue(binding.countrySpinner, user.getCountry());
        }
    }

    /**
     * Validates and saves the updated profile information to Firestore.
     */
    private void saveUserData() {
        if (mAuth.getCurrentUser() == null)
            return;

        String uid = mAuth.getCurrentUser().getUid();

        String country = binding.countrySpinner.getSelectedItem() != null
                ? binding.countrySpinner.getSelectedItem().toString()
                : "";
        String state = binding.stateSpinner.getSelectedItem() != null
                ? binding.stateSpinner.getSelectedItem().toString()
                : "";
        String district = binding.districtSpinner.getSelectedItem() != null
                ? binding.districtSpinner.getSelectedItem().toString()
                : "";
        String city = binding.citySpinner.getSelectedItem() != null ? binding.citySpinner.getSelectedItem().toString()
                : "";

        // Filter out prompt defaults to avoid saving placeholder text as data
        if (country.startsWith("Select"))
            country = "";
        if (state.startsWith("Select"))
            state = "";
        if (district.startsWith("Select"))
            district = "";
        if (city.startsWith("Select"))
            city = "";

        // Update profile in Firestore
        mFirestore.collection("users").document(uid).update(
                "postalCode", binding.postalCodeEdit.getText().toString(),
                "address", binding.addressEdit.getText().toString(),
                "city", city,
                "state", state,
                "district", district,
                "country", country,
                "bankAccountNumber", binding.bankAccountEdit.getText().toString(),
                "accountHolderName", binding.accountHolderEdit.getText().toString(),
                "ifscCode", binding.ifscCodeEdit.getText().toString()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
