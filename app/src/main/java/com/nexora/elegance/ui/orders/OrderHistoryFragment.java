package com.nexora.elegance.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexora.elegance.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.nexora.elegance.models.Order;
import com.nexora.elegance.databinding.FragmentOrderHistoryBinding;
import com.nexora.elegance.adapters.OrderAdapter;
import com.nexora.elegance.ui.orders.OrderDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * OrderHistoryFragment displays a list of all past and current orders for the
 * authenticated user.
 * Orders are fetched from Firestore and sorted by timestamp (most recent
 * first).
 */
public class OrderHistoryFragment extends Fragment {

    private FragmentOrderHistoryBinding binding;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        fetchOrders();

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

    /**
     * Configures the RecyclerView for displaying orders.
     */
    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(getContext(), orderList, this::onOrderClicked);
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    /**
     * Fetches the user's order history from Firestore.
     * Includes real-time listening for status updates.
     */
    private void fetchOrders() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyStateText.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Query orders collection sorted by timestamp descending
        mFirestore.collection("users").document(user.getUid())
                .collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null)
                        return;
                    binding.progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading orders", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }

                        // Toggle visibility based on data availability
                        if (orderList.isEmpty()) {
                            binding.emptyStateText.setVisibility(View.VISIBLE);
                            binding.ordersRecyclerView.setVisibility(View.GONE);
                        } else {
                            binding.emptyStateText.setVisibility(View.GONE);
                            binding.ordersRecyclerView.setVisibility(View.VISIBLE);
                            orderAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Handles order item clicks by navigating to the detailed view.
     */
    private void onOrderClicked(Order order) {
        Intent intent = new Intent(getContext(), OrderDetailsActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
