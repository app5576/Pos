package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.InvoiceItem;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import Locale

public class InvoiceItemsAdapter extends RecyclerView.Adapter<InvoiceItemsAdapter.ViewHolder> {

    private List<InvoiceItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InvoiceItem item);
    }

    public InvoiceItemsAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<InvoiceItem> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_invoice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceItem item = items.get(position);
        holder.tvName.setText(item.getProductName() != null ? item.getProductName() : "منتج");
        // Use Locale.US for integer formatting to ensure English digits
        holder.tvDetails.setText(String.format(Locale.US, "%d × %s = %s", 
            (int)item.getQuantity(), 
            CurrencyHelper.format(item.getUnitPrice()), 
            CurrencyHelper.format(item.getTotalPrice())));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_edit_item_name);
            tvDetails = itemView.findViewById(R.id.tv_edit_item_details);
        }
    }
}