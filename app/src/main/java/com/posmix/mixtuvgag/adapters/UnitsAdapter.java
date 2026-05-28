package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Unit;

public class UnitsAdapter extends ListAdapter<Unit, UnitsAdapter.ViewHolder> {
    
    public interface OnItemClickListener {
        void onEdit(Unit unit);
        void onDelete(Unit unit);
    }
    
    private final OnItemClickListener listener;
    
    public UnitsAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Unit>() {
            @Override
            public boolean areItemsTheSame(@NonNull Unit oldItem, @NonNull Unit newItem) {
                return oldItem.getId() == newItem.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Unit oldItem, @NonNull Unit newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false));
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Unit unit = getItem(position);
        holder.tvName.setText(unit.getName());
        holder.itemView.setOnClickListener(v -> listener.onEdit(unit));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(unit);
            return true;
        });
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_unit_name);
        }
    }
}
