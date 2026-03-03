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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.nexora.elegance.data.models.Order;
import com.nexora.elegance.databinding.FragmentOrderHistoryBinding;
import com.nexora.elegance.ui.adapters.OrderAdapter;
import com.nexora.elegance.ui.orders.OrderDetailsActivity;

import java.util.ArrayList;
import java.util.List;

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
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(getContext(), orderList, this::onOrderClicked);
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void fetchOrders() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyStateText.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

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
