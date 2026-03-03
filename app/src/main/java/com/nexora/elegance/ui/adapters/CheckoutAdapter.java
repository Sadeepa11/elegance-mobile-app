package com.nexora.elegance.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.CartItem;

import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> checkoutItems;
    private Context context;

    public CheckoutAdapter(Context context, List<CartItem> checkoutItems) {
        this.context = context;
        this.checkoutItems = checkoutItems;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_product, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = checkoutItems.get(position);

        holder.productName.setText(item.getName());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_logo)
                .into(holder.productImage);

        // Price formatting
        holder.productPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getPrice()));
        holder.orderTotalText.setText(
                String.format(Locale.getDefault(), "LKR %.2f", item.getPrice() * Math.max(1, item.getQuantity())));

        holder.orderQtyText
                .setText(String.format(Locale.getDefault(), "Total Order (%d)  :", Math.max(1, item.getQuantity())));

        // Specific Variations
        if (item.getSize() != null && !item.getSize().isEmpty()) {
            holder.productSize.setText("Size: " + item.getSize());
            holder.productSize.setVisibility(View.VISIBLE);
        } else {
            holder.productSize.setVisibility(View.GONE);
        }

        if (item.getColor() != null && !item.getColor().isEmpty()) {
            holder.productColor.setText("Color: " + item.getColor());
            holder.productColor.setVisibility(View.VISIBLE);
        } else {
            holder.productColor.setVisibility(View.GONE);
        }

        holder.productQty.setText("Qty: " + Math.max(1, item.getQuantity()));

        // Mock old price and discount for UI
        double oldPrice = item.getPrice() * 1.5;
        holder.oldPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", oldPrice));
        holder.oldPrice.setPaintFlags(holder.oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.discount.setText("upto 33% off");
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productSize;
        TextView productColor;
        TextView productQty;
        TextView productPrice;
        TextView oldPrice;
        TextView discount;
        TextView orderQtyText;
        TextView orderTotalText;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkoutProductImage);
            productName = itemView.findViewById(R.id.checkoutProductName);
            productSize = itemView.findViewById(R.id.checkoutProductSize);
            productColor = itemView.findViewById(R.id.checkoutProductColor);
            productQty = itemView.findViewById(R.id.checkoutProductQty);
            productPrice = itemView.findViewById(R.id.checkoutProductPrice);
            oldPrice = itemView.findViewById(R.id.checkoutProductOldPrice);
            discount = itemView.findViewById(R.id.checkoutProductDiscount);
            orderQtyText = itemView.findViewById(R.id.checkoutOrderQtyText);
            orderTotalText = itemView.findViewById(R.id.checkoutOrderTotalText);
        }
    }
}
