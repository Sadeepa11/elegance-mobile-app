package com.nexora.elegance.ui.checkout;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.nexora.elegance.R;

public class CheckoutAddressBottomSheet extends BottomSheetDialogFragment {

    private EditText etAddress, etCity, etDistrict, etProvince, etCountry;
    private MaterialButton btnSaveAddress;

    private String address = "", city = "", district = "", province = "", country = "";

    // Interface for broadcasting result back to CheckoutActivity
    private OnAddressSavedListener listener;

    public interface OnAddressSavedListener {
        void onAddressSaved(String address, String city, String district, String province, String country);
    }

    public void setOnAddressSavedListener(OnAddressSavedListener listener) {
        this.listener = listener;
    }

    public CheckoutAddressBottomSheet(String address, String city, String district, String province, String country) {
        if (address != null)
            this.address = address;
        if (city != null)
            this.city = city;
        if (district != null)
            this.district = district;
        if (province != null)
            this.province = province;
        if (country != null)
            this.country = country;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_checkout_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAddress = view.findViewById(R.id.etAddress);
        etCity = view.findViewById(R.id.etCity);
        etDistrict = view.findViewById(R.id.etDistrict);
        etProvince = view.findViewById(R.id.etProvince);
        etCountry = view.findViewById(R.id.etCountry);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);

        populateFields();

        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void populateFields() {
        etAddress.setText(address);
        etCity.setText(city);
        etDistrict.setText(district);
        etProvince.setText(province);
        etCountry.setText(country);
    }

    private void saveAddress() {
        String newAddress = etAddress.getText().toString().trim();
        String newCity = etCity.getText().toString().trim();
        String newDistrict = etDistrict.getText().toString().trim();
        String newProvince = etProvince.getText().toString().trim();
        String newCountry = etCountry.getText().toString().trim();

        if (listener != null) {
            listener.onAddressSaved(newAddress, newCity, newDistrict, newProvince, newCountry);
        }
        dismiss();
    }
}
