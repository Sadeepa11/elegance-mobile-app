package com.nexora.elegance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexora.elegance.R;
import com.nexora.elegance.models.CartItem;

import java.util.List;
import java.util.Locale;

/**
 * OrderDetailsAdapter displays items belonging to a specific historical order.
 * It is used in the OrderDetailsActivity to show purchased variants and prices.
 */
public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.DetailViewHolder> {

    private final Context context;
    private final List<CartItem> itemList;

    public OrderDetailsAdapter(Context context, List<CartItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.detailItemName.setText(item.getName());
        holder.detailItemPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getPrice()));
        holder.detailItemQty.setText("x" + Math.max(1, item.getQuantity()));

        if (item.getSize() != null && !item.getSize().isEmpty() && !item.getSize().equals("Default")) {
            holder.detailItemSize.setText("Size: " + item.getSize());
            holder.detailItemSize.setVisibility(View.VISIBLE);
        } else {
            holder.detailItemSize.setVisibility(View.GONE);
        }

        if (item.getColor() != null && !item.getColor().isEmpty() && !item.getColor().equals("Default")) {
            holder.detailItemColor.setText("Color: " + item.getColor());
            holder.detailItemColor.setVisibility(View.VISIBLE);
        } else {
            holder.detailItemColor.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.rounded_white_square)
                .into(holder.detailItemImage);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        ImageView detailItemImage;
        TextView detailItemName, detailItemSize, detailItemColor, detailItemPrice, detailItemQty;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            detailItemImage = itemView.findViewById(R.id.detailItemImage);
            detailItemName = itemView.findViewById(R.id.detailItemName);
            detailItemSize = itemView.findViewById(R.id.detailItemSize);
            detailItemColor = itemView.findViewById(R.id.detailItemColor);
            detailItemPrice = itemView.findViewById(R.id.detailItemPrice);
            detailItemQty = itemView.findViewById(R.id.detailItemQty);
        }
    }
}
