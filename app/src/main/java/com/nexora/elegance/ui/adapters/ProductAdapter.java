package com.nexora.elegance.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nexora.elegance.R;
import com.nexora.elegance.data.models.Product;
import com.nexora.elegance.databinding.ItemProductBinding;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import android.graphics.Color;
import com.nexora.elegance.utils.AnimationHelper;

/**
 * ProductAdapter manages the grid display of products on Home and Category
 * screens.
 * It supports wishlist state management and quick "Add to Cart" actions.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private Set<String> wishlistedIds = new HashSet<>();
    private final OnProductClickListener onProductClick;
    private final OnAddToCartClickListener onAddToCartClick;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public interface OnWishlistClickListener {
        void onWishlistClick(Product product);
    }

    private final OnWishlistClickListener onWishlistClick;

    public ProductAdapter(List<Product> products, OnProductClickListener onProductClick,
            OnAddToCartClickListener onAddToCartClick, OnWishlistClickListener onWishlistClick) {
        this.products = products;
        this.onProductClick = onProductClick;
        this.onAddToCartClick = onAddToCartClick;
        this.onWishlistClick = onWishlistClick;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.productName.setText(product.getName());
        holder.binding.productPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice()));

        com.bumptech.glide.Glide.with(holder.binding.getRoot().getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(holder.binding.productImage);

        if (wishlistedIds.contains(product.getId())) {
            holder.binding.wishlistIcon.setColorFilter(Color.parseColor("#E91E63"));
        } else {
            holder.binding.wishlistIcon.setColorFilter(Color.parseColor("#1A1A1A"));
        }

        AnimationHelper.addPressAnimation(holder.binding.getRoot());
        holder.binding.getRoot().setOnClickListener(v -> onProductClick.onProductClick(product));

        AnimationHelper.addPressAnimation(holder.binding.addToCartButton);
        holder.binding.addToCartButton.setOnClickListener(v -> onAddToCartClick.onAddToCartClick(product));
        if (holder.binding.wishlistButton != null) {
            AnimationHelper.addPressAnimation(holder.binding.wishlistButton);
            holder.binding.wishlistButton.setOnClickListener(v -> {
                if (onWishlistClick != null) {
                    onWishlistClick.onWishlistClick(product);
                }
            });
        }

        // Entrance Animation
        holder.binding.getRoot().setAlpha(0f);
        holder.binding.getRoot().setTranslationY(100f);
        holder.binding.getRoot().animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(position * 50L) // Subtle staggered effect
                .start();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newList) {
        this.products = newList;
        notifyDataSetChanged();
    }

    public void setWishlistedIds(Set<String> wishlistedIds) {
        this.wishlistedIds = wishlistedIds;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
