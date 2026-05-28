package com.posmix.mixtuvgag.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.databinding.ItemTransactionBinding;
import com.posmix.mixtuvgag.models.CashTransaction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CashTransactionsAdapter extends ListAdapter<CashTransaction, CashTransactionsAdapter.VH> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));

    public CashTransactionsAdapter() { super(DIFF); }

    private static final DiffUtil.ItemCallback<CashTransaction> DIFF = new DiffUtil.ItemCallback<CashTransaction>() {
        @Override public boolean areItemsTheSame(@NonNull CashTransaction o, @NonNull CashTransaction n) { return o.getId() == n.getId(); }
        @Override public boolean areContentsTheSame(@NonNull CashTransaction o, @NonNull CashTransaction n) { return o.equals(n); }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(ItemTransactionBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) { h.bind(getItem(p)); }

    static class VH extends RecyclerView.ViewHolder {
        ItemTransactionBinding b;
        VH(ItemTransactionBinding b) { super(b.getRoot()); this.b = b; }
        void bind(CashTransaction t) {
            b.tvDesc.setText(t.getDescription() != null ? t.getDescription() : "");
            b.tvDate.setText(SDF.format(new Date(t.getDate())));
            boolean isIn = t.getType() == CashTransaction.TYPE_IN;
            String sign = isIn ? "+ " : "- ";
            b.tvAmount.setText(String.format("%s%.2f ر.س", sign, t.getAmount()));
            b.tvAmount.setTextColor(isIn ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
        }
    }
}
