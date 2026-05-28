package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

import java.util.ArrayList;
import java.util.List;

public class SalesProductGridAdapter extends RecyclerView.Adapter<SalesProductGridAdapter.ViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnAddToCartListener listener;
    private boolean allowOutOfStockSales; // New field

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    // Modified constructor to accept allowOutOfStockSales
    public SalesProductGridAdapter(boolean allowOutOfStockSales) {
        this.allowOutOfStockSales = allowOutOfStockSales;
    }

    public void setOnAddToCartListener(OnAddToCartListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> list) {
        this.products = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(CurrencyHelper.format(product.getSellPrice()));

        if (product.getStockQuantity() == 0) {
            holder.tvStockStatus.setText("نفد المخزون");
            holder.tvStockStatus.setTextColor(holder.itemView.getContext()
                    .getColor(android.R.color.holo_red_dark));
            // Use the allowOutOfStockSales preference here
            holder.btnAddToCart.setEnabled(allowOutOfStockSales);
        } else if (product.isLowStock()) {
            holder.tvStockStatus.setText("مخزون منخفض");
            holder.tvStockStatus.setTextColor(holder.itemView.getContext()
                    .getColor(android.R.color.holo_orange_dark));
            holder.btnAddToCart.setEnabled(true);
        } else {
            holder.tvStockStatus.setText("");
            holder.btnAddToCart.setEnabled(true);
        }

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStockStatus;
        Button btnAddToCart;

        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}