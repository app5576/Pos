package com.posmix.mixtuvgag.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.databinding.ItemProductBinding;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

public class ProductsAdapter extends ListAdapter<Product, ProductsAdapter.ProductViewHolder> {

    private final ProductClickListener listener;

    public interface ProductClickListener {
        void onProductClick(Product product);
        void onDeleteClick(Product product);
        // New: Add a callback for editing sell price
        void onEditSellPriceClick(Product product);
    }

    public ProductsAdapter(ProductClickListener listener) {
        super(new DiffUtil.ItemCallback<Product>() {
            @Override
            public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                return oldItem.getId() == newItem.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                    listener.onProductClick(getItem(getAdapterPosition()));
            });

            binding.btnDeleteProduct.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                    listener.onDeleteClick(getItem(getAdapterPosition()));
            });
            
            // New: Listener for the edit sell price button
            binding.btnEditSellPrice.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                    listener.onEditSellPriceClick(getItem(getAdapterPosition()));
            });
        }

        void bind(Product product) {
            binding.tvName.setText(product.getName());

            // Sell price
            binding.tvPrice.setText(CurrencyHelper.format(product.getSellPrice()));

            // Buy price
            binding.tvBuyPrice.setText(CurrencyHelper.format(product.getBuyPrice()));

            // Barcode
            if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
                binding.tvBarcode.setText("# " + product.getBarcode());
                binding.tvBarcode.setVisibility(View.VISIBLE);
            } else {
                binding.tvBarcode.setVisibility(View.GONE);
            }

            // Category badge
            if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) { // Changed from getCategory()
                binding.tvCategory.setText(product.getCategoryName()); // Changed from getCategory()
                binding.tvCategory.setVisibility(View.VISIBLE);
            } else {
                binding.tvCategory.setVisibility(View.GONE);
            }

            // Stock quantity
            binding.tvStock.setText("الكمية: " + product.getStockQuantity() +
                    (product.getMinStockAlert() > 0 ? "  (حد أدنى: " + product.getMinStockAlert() + ")" : ""));

            // Status badge with colored left bar
            if (product.getStockQuantity() == 0) {
                binding.tvStockStatus.setText("نفد المخزون");
                binding.tvStockStatus.setTextColor(Color.WHITE);
                binding.tvStockStatus.setBackgroundResource(0);
                binding.tvStockStatus.setBackgroundColor(Color.parseColor("#EF5350"));
                setPaddingOnBadge(binding.tvStockStatus);
                binding.viewStatusBar.setBackgroundColor(Color.parseColor("#EF5350"));
            } else if (product.isLowStock()) {
                binding.tvStockStatus.setText("مخزون منخفض");
                binding.tvStockStatus.setTextColor(Color.WHITE);
                binding.tvStockStatus.setBackgroundColor(Color.parseColor("#FFA000"));
                setPaddingOnBadge(binding.tvStockStatus);
                binding.viewStatusBar.setBackgroundColor(Color.parseColor("#FFA000"));
            } else {
                binding.tvStockStatus.setText("متوفر");
                binding.tvStockStatus.setTextColor(Color.WHITE);
                binding.tvStockStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                setPaddingOnBadge(binding.tvStockStatus);
                binding.viewStatusBar.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
        }

        private void setPaddingOnBadge(android.widget.TextView tv) {
            int px = (int) (tv.getContext().getResources().getDisplayMetrics().density * 6);
            int pxV = (int) (tv.getContext().getResources().getDisplayMetrics().density * 2);
            tv.setPadding(px, pxV, px, pxV);
        }
    }
}