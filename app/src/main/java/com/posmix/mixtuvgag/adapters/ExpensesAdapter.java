package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Expense;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpensesAdapter extends ListAdapter<Expense, ExpensesAdapter.VH> {

    public interface ExpenseClickListener {
        void onDelete(Expense expense);
    }

    private final ExpenseClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ar"));

    public ExpensesAdapter(ExpenseClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Expense> DIFF = new DiffUtil.ItemCallback<Expense>() {
        @Override public boolean areItemsTheSame(@NonNull Expense a, @NonNull Expense b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Expense a, @NonNull Expense b) { return a.getAmount() == b.getAmount(); }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Expense e = getItem(pos);
        h.tvCategory.setText(e.getCategory());
        h.tvAmount.setText(String.format("%.2f ر.س", e.getAmount()));
        h.tvDate.setText(sdf.format(new Date(e.getDate())));
        h.tvNotes.setText(e.getNotes() != null ? e.getNotes() : "");
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDate, tvNotes;
        ImageButton btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_expense_category);
            tvAmount = v.findViewById(R.id.tv_expense_amount);
            tvDate = v.findViewById(R.id.tv_expense_date);
            tvNotes = v.findViewById(R.id.tv_expense_notes);
            btnDelete = v.findViewById(R.id.btn_delete_expense);
        }
    }
}
