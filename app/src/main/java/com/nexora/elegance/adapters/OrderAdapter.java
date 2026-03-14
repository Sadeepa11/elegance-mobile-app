package com.nexora.elegance.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexora.elegance.R;
import com.nexora.elegance.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * OrderAdapter manages the list of orders in the user's order history.
 * It provides status-based styling and formatting for order timestamps.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Map data directly to views
        holder.orderIdText.setText("Order #" + order.getOrderId().substring(0, 8));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        holder.orderDateText.setText(sdf.format(new Date(order.getTimestamp())));

        holder.orderTotalText.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotalAmount()));

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.orderItemsText.setText(itemCount + (itemCount == 1 ? " Item" : " Items"));

        String status = order.getStatus() != null ? order.getStatus() : "Processing";
        holder.orderStatusText.setText(status);

        // Dynamically style the status badge
        if (status.equalsIgnoreCase("Completed")) {
            holder.orderStatusText.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else if (status.equalsIgnoreCase("Cancelled")) {
            holder.orderStatusText.setBackgroundColor(Color.parseColor("#F44336")); // Red
        } else {
            // Keep default bg_fab_gradient assigned in XML for "Processing" or others
            holder.orderStatusText.setBackgroundResource(R.drawable.bg_fab_gradient);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, orderDateText, orderStatusText, orderItemsText, orderTotalText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            orderStatusText = itemView.findViewById(R.id.orderStatusText);
            orderItemsText = itemView.findViewById(R.id.orderItemsText);
            orderTotalText = itemView.findViewById(R.id.orderTotalText);
        }
    }
}
