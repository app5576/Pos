package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.CartItem;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

import java.util.ArrayList;
import java.util.List;

public class CartHorizontalAdapter extends RecyclerView.Adapter<CartHorizontalAdapter.ViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onCartItemClick(CartItem item);
    }

    public void setOnCartItemClickListener(OnCartItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvQtyPrice.setText(item.getQuantity() + "x " + CurrencyHelper.format(item.getUnitPrice()));
        holder.tvTotal.setText(CurrencyHelper.format(item.getFinalTotal()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCartItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQtyPrice, tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHorizontalCartName);
            tvQtyPrice = itemView.findViewById(R.id.tvHorizontalCartQtyPrice);
            tvTotal = itemView.findViewById(R.id.tvHorizontalCartTotal);
        }
    }
}