package com.nexora.elegance.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.databinding.ItemProductSearchBinding;

import java.util.List;
import java.util.Locale;

/**
 * SearchAdapter displays search results in a staggered grid layout.
 * It dynamically adjusts image heights to create a modern masonry effect.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Product> products;
    private final OnProductClickListener onProductClick;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public SearchAdapter(List<Product> products, OnProductClickListener onProductClick) {
        this.products = products;
        this.onProductClick = onProductClick;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductSearchBinding binding = ItemProductSearchBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new SearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.productName.setText(product.getName());
        holder.binding.productDescription.setText(product.getDescription());
        holder.binding.productPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice()));

        // Apply a staggered effect by varying the image height
        int imageHeight = (position % 4 == 0 || position % 4 == 3) ? 240 : 180;
        int heightPixels = (int) (imageHeight
                * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        android.view.ViewGroup.LayoutParams params = holder.binding.productImage.getLayoutParams();
        params.height = heightPixels;
        holder.binding.productImage.setLayoutParams(params);

        Glide.with(holder.binding.getRoot().getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.slider_model)
                .error(R.drawable.slider_model)
                .centerCrop()
                .into(holder.binding.productImage);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (onProductClick != null) {
                onProductClick.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newList) {
        this.products = newList;
        notifyDataSetChanged();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        final ItemProductSearchBinding binding;

        public SearchViewHolder(ItemProductSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
