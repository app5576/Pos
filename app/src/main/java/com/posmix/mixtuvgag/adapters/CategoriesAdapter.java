package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Category;

public class CategoriesAdapter extends ListAdapter<Category, CategoriesAdapter.ViewHolder> {
    
    public interface OnItemClickListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }
    
    private final OnItemClickListener listener;
    
    public CategoriesAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Category>() {
            @Override
            public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getId() == newItem.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = getItem(position);
        holder.tvName.setText(category.getName());
        holder.tvDescription.setText(category.getDescription() != null ? category.getDescription() : "");
        holder.itemView.setOnClickListener(v -> listener.onEdit(category));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(category);
            return true;
        });
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvDescription = itemView.findViewById(R.id.tv_category_description);
        }
    }
}
