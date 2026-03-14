package com.nexora.elegance.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nexora.elegance.models.CartItem;
import com.nexora.elegance.databinding.BottomSheetAddReviewBinding;

/**
 * AddReviewBottomSheet allows users to provide a rating and a text review
 * for a specific item from a completed order.
 */
public class AddReviewBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddReviewBinding binding;
    private final CartItem cartItem;
    private final OnReviewSubmittedListener listener;

    /**
     * Interface to communicate the submitted review back to the parent
     * activity/fragment.
     */
    public interface OnReviewSubmittedListener {
        void onReviewSubmitted(CartItem item, float rating, String reviewText);
    }

    public AddReviewBottomSheet(CartItem cartItem, OnReviewSubmittedListener listener) {
        this.cartItem = cartItem;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.reviewTitleText.setText("Rate " + cartItem.getName());

        // Validate and submit the review
        binding.btnSubmitReview.setOnClickListener(v -> {
            float rating = binding.reviewRatingBar.getRating();
            String reviewText = binding.reviewInput.getText() != null ? binding.reviewInput.getText().toString().trim()
                    : "";

            if (rating == 0) {
                Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onReviewSubmitted(cartItem, rating, reviewText);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
