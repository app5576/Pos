package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Invoice;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoicesAdapter extends ListAdapter<Invoice, InvoicesAdapter.VH> {

    public interface InvoiceClickListener { void onClick(Invoice invoice); }
    private final InvoiceClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));

    public InvoicesAdapter(InvoiceClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Invoice> DIFF = new DiffUtil.ItemCallback<Invoice>() {
        @Override public boolean areItemsTheSame(@NonNull Invoice a, @NonNull Invoice b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Invoice a, @NonNull Invoice b) { return a.getTotal() == b.getTotal(); }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Invoice inv = getItem(pos);
        h.tvNumber.setText(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "#" + inv.getId());
        h.tvDate.setText(sdf.format(new Date(inv.getDate())));
        h.tvTotal.setText(String.format("%.2f ر.س", inv.getTotal()));
        String status = inv.getPaymentStatus() == Invoice.STATUS_CASH ? "نقدي" :
                        inv.getPaymentStatus() == Invoice.STATUS_CREDIT ? "آجل" : "جزئي";
        h.tvStatus.setText(status);
        h.itemView.setOnClickListener(v -> listener.onClick(inv));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNumber, tvDate, tvTotal, tvStatus;
        VH(@NonNull View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tv_invoice_number);
            tvDate = v.findViewById(R.id.tv_invoice_date);
            tvTotal = v.findViewById(R.id.tv_invoice_total);
            tvStatus = v.findViewById(R.id.tv_invoice_status);
        }
    }
}
