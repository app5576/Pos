package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.ProductUnit;

public class ProductUnitsAdapter extends ListAdapter<ProductUnit, ProductUnitsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEdit(ProductUnit productUnit);
        void onDelete(ProductUnit productUnit);
    }
    
    private OnItemClickListener listener;
    
    public ProductUnitsAdapter() {
        super(new DiffUtil.ItemCallback<ProductUnit>() {
            @Override public boolean areItemsTheSame(@NonNull ProductUnit oldItem, @NonNull ProductUnit newItem) { return oldItem.getId() == newItem.getId(); }
            @Override public boolean areContentsTheSame(@NonNull ProductUnit oldItem, @NonNull ProductUnit newItem) { return oldItem.getUnitName().equals(newItem.getUnitName()); }
        });
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }
    
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_unit, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductUnit pu = getItem(position);
        holder.tvUnitName.setText(pu.getUnitName());
        holder.tvUnitDetails.setText(pu.getQuantity() + " وحدة | السعر: " + pu.getSellPrice() + " | باركود: " + (pu.getBarcode() != null ? pu.getBarcode() : ""));
        holder.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(pu); });
        holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(pu); });
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnitName, tvUnitDetails;
        ImageButton btnEdit, btnDelete;
        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvUnitName = itemView.findViewById(R.id.tv_unit_name);
            tvUnitDetails = itemView.findViewById(R.id.tv_unit_details);
            btnEdit = itemView.findViewById(R.id.btn_edit_unit);
            btnDelete = itemView.findViewById(R.id.btn_delete_unit);
        }
    }
}
