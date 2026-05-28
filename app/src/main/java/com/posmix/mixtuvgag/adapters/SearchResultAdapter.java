package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> list) {
        this.products = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(CurrencyHelper.format(product.getSellPrice()));
        holder.tvStock.setText("المخزون: " + product.getStockQuantity());

        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            holder.tvBarcode.setText("#" + product.getBarcode());
            holder.tvBarcode.setVisibility(View.VISIBLE);
        } else {
            holder.tvBarcode.setVisibility(View.GONE);
        }

        if (product.getStockQuantity() == 0) {
            holder.tvStock.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else if (product.isLowStock()) {
            holder.tvStock.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvStock.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvBarcode, tvStock;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_search_product_name);
            tvPrice = itemView.findViewById(R.id.tv_search_product_price);
            tvBarcode = itemView.findViewById(R.id.tv_search_product_barcode);
            tvStock = itemView.findViewById(R.id.tv_search_product_stock);
        }
    }
}