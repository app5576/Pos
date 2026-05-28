package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceSummaryAdapter extends ListAdapter<Invoice, InvoiceSummaryAdapter.ViewHolder> {

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    private final OnInvoiceClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("en"));

    public InvoiceSummaryAdapter(OnInvoiceClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Invoice> DIFF = new DiffUtil.ItemCallback<Invoice>() {
        @Override
        public boolean areItemsTheSame(@NonNull Invoice oldItem, @NonNull Invoice newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Invoice oldItem, @NonNull Invoice newItem) {
            return oldItem.getTotal() == newItem.getTotal() &&
                   oldItem.getRemainingAmount() == newItem.getRemainingAmount();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = getItem(position);
        holder.tvInvoiceNumber.setText(invoice.getInvoiceNumber());
        holder.tvInvoiceDate.setText(sdf.format(new Date(invoice.getDate())));
        holder.tvInvoiceTotal.setText(String.format(Locale.US, "%.2f %s", 
            invoice.getTotal(), CurrencyHelper.getSymbol()));
        holder.tvRemainingAmount.setText(String.format(Locale.US, "Remaining: %.2f %s", 
            invoice.getRemainingAmount(), CurrencyHelper.getSymbol()));
        
        if (invoice.getRemainingAmount() > 0) {
            holder.tvRemainingAmount.setTextColor(holder.itemView.getContext()
                .getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvRemainingAmount.setTextColor(holder.itemView.getContext()
                .getColor(android.R.color.holo_green_dark));
        }
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInvoiceClick(invoice);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvInvoiceDate, tvInvoiceTotal, tvRemainingAmount;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            tvInvoiceDate = itemView.findViewById(R.id.tv_invoice_date);
            tvInvoiceTotal = itemView.findViewById(R.id.tv_invoice_total);
            tvRemainingAmount = itemView.findViewById(R.id.tv_remaining_amount);
            cardView = itemView.findViewById(R.id.card_invoice);
        }
    }
}
