package com.nexora.elegance.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import com.bumptech.glide.Glide;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onItemRemoved(CartItem item);

        void onItemUpdated(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemInteractionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.productName.setText(item.getName());
        holder.productCategory.setText(item.getCategory());
        holder.cartProductPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getPrice()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_logo)
                .into(holder.productImage);

        // SIZE SPINNER
        List<String> sizes = item.getAvailableSizes() != null && !item.getAvailableSizes().isEmpty()
                ? item.getAvailableSizes()
                : java.util.Collections.singletonList(item.getSize());
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, sizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerSize.setAdapter(sizeAdapter);
        if (item.getSize() != null) {
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).equals(item.getSize())) {
                    holder.spinnerSize.setSelection(i);
                    break;
                }
            }
        }

        // COLOR SPINNER
        List<String> colors = item.getAvailableColors() != null && !item.getAvailableColors().isEmpty()
                ? item.getAvailableColors()
                : java.util.Collections.singletonList(item.getColor());
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerColor.setAdapter(colorAdapter);
        if (item.getColor() != null) {
            for (int i = 0; i < colors.size(); i++) {
                if (colors.get(i).equals(item.getColor())) {
                    holder.spinnerColor.setSelection(i);
                    break;
                }
            }
        }

        // QTY BUTTONS
        holder.textQty.setText(String.valueOf(item.getQuantity()));

        holder.btnQtyMinus.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            if (currentQty > 0) {
                item.setQuantity(currentQty - 1);
                holder.textQty.setText(String.valueOf(item.getQuantity()));
                if (listener != null) {
                    listener.onItemUpdated(item);
                }
            }
        });

        holder.btnQtyPlus.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            int maxQty = 10;
            if (item.getStockMap() != null) {
                String c = item.getColor() != null ? item.getColor() : "Default";
                String s = item.getSize() != null ? item.getSize() : "Default";
                Integer available = item.getStockMap().get(c + "_" + s);
                if (available != null) {
                    maxQty = available;
                }
            }
            if (currentQty < maxQty) {
                item.setQuantity(currentQty + 1);
                holder.textQty.setText(String.valueOf(item.getQuantity()));
                if (listener != null) {
                    listener.onItemUpdated(item);
                }
            } else {
                Toast.makeText(context, "Maximum available stock is " + maxQty, Toast.LENGTH_SHORT).show();
            }
        });

        // LISTENERS
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Ensure manual trigger vs programmatic initialization
                boolean changed = false;

                String selectedSize = (String) holder.spinnerSize.getSelectedItem();
                if (selectedSize != null && !selectedSize.equals(item.getSize())) {
                    item.setSize(selectedSize);
                    changed = true;
                }

                String selectedColor = (String) holder.spinnerColor.getSelectedItem();
                if (selectedColor != null && !selectedColor.equals(item.getColor())) {
                    item.setColor(selectedColor);
                    changed = true;
                }

                if (changed) {
                    // Check bounds for the newly selected combination
                    int maxQty = 10;
                    if (item.getStockMap() != null) {
                        String c = item.getColor() != null ? item.getColor() : "Default";
                        String s = item.getSize() != null ? item.getSize() : "Default";
                        Integer available = item.getStockMap().get(c + "_" + s);
                        if (available != null) {
                            maxQty = available;
                        } else {
                            maxQty = 1;
                        }
                    }

                    if (item.getQuantity() > maxQty) {
                        item.setQuantity(maxQty);
                        holder.textQty.setText(String.valueOf(maxQty));
                        Toast.makeText(context, "Quantity lowered to match available stock", Toast.LENGTH_SHORT).show();
                    }

                    if (listener != null) {
                        listener.onItemUpdated(item);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        // We set the listeners in post so we don't trigger updates during
        // initialization
        holder.spinnerSize.post(() -> holder.spinnerSize.setOnItemSelectedListener(spinnerListener));
        holder.spinnerColor.post(() -> holder.spinnerColor.setOnItemSelectedListener(spinnerListener));

        holder.btnRemove.setVisibility(View.VISIBLE);
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productCategory;
        TextView cartProductPrice;
        Spinner spinnerSize;
        Spinner spinnerColor;
        ImageView btnQtyMinus;
        ImageView btnQtyPlus;
        TextView textQty;
        TextView deliveryDate;
        ImageView btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            productCategory = itemView.findViewById(R.id.cartProductCategory);
            cartProductPrice = itemView.findViewById(R.id.cartProductPrice);
            spinnerSize = itemView.findViewById(R.id.spinnerSize);
            spinnerColor = itemView.findViewById(R.id.spinnerColor);
            btnQtyMinus = itemView.findViewById(R.id.btnQtyMinus);
            textQty = itemView.findViewById(R.id.textQty);
            btnQtyPlus = itemView.findViewById(R.id.btnQtyPlus);
            deliveryDate = itemView.findViewById(R.id.deliveryDate);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
