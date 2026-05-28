package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.CartItem;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void onEditItemDetails(CartItem item); // New interface method
    }

    public void setOnCartActionListener(OnCartActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvPrice.setText(CurrencyHelper.format(item.getUnitPrice()));
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvTotal.setText(CurrencyHelper.format(item.getFinalTotal()));

        // Set click listener for the entire item view to open the edit dialog
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditItemDetails(item);
        });

        // The dedicated edit button still works, but clicking the item row now also triggers it.
        holder.btnEditDetails.setOnClickListener(v -> {
            if (listener != null) listener.onEditItemDetails(item);
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) listener.onQuantityChanged(item, item.getQuantity() + 1);
        });
        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null && item.getQuantity() > 1)
                listener.onQuantityChanged(item, item.getQuantity() - 1);
        });
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQty, tvTotal;
        ImageButton btnIncrease, btnDecrease, btnRemove, btnEditDetails; // Added btnEditDetails

        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvQty = itemView.findViewById(R.id.tvCartQty);
            tvTotal = itemView.findViewById(R.id.tvCartItemTotal);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQty);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveFromCart);
            btnEditDetails = itemView.findViewById(R.id.btnEditCartItemDetails); // Initialize new button
        }
    }
}