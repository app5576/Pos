package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.database.DatabaseHelper;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class DebtsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecyclerView rv;
    private TextView tvTotalDebts;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ar"));
    private int currentFilter = 0; // 0=all, 1=customers, 2=suppliers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debts);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("كشف الديون المستحقة");
        }
        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rv_debts);
        tvTotalDebts = findViewById(R.id.tv_total_debts);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ChipGroup chipGroup = findViewById(R.id.chip_group_debt_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all_debts) currentFilter = 0;
                else if (id == R.id.chip_customer_debts) currentFilter = 1;
                else if (id == R.id.chip_supplier_debts) currentFilter = 2;
                loadDebts();
            }
        });
        loadDebts();
    }

    private void loadDebts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Invoice> debts = db.getCreditInvoices(currentFilter);
            double total = 0;
            for (Invoice inv : debts) total += inv.getRemainingAmount();
            double finalTotal = total;
            runOnUiThread(() -> {
                tvTotalDebts.setText("إجمالي الديون: " + CurrencyHelper.format(finalTotal));
                rv.setAdapter(new RecyclerView.Adapter<DebtVH>() {
                    @Override public DebtVH onCreateViewHolder(android.view.ViewGroup p, int vt) {
                        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_debt, p, false);
                        return new DebtVH(v);
                    }
                    @Override public void onBindViewHolder(DebtVH h, int pos) {
                        Invoice inv = debts.get(pos);
                        h.tvInvNumber.setText(inv.getInvoiceNumber());
                        h.tvDate.setText(sdf.format(new Date(inv.getDate())));
                        h.tvTotal.setText(CurrencyHelper.format(inv.getTotal()));
                        h.tvPaid.setText("مدفوع: " + CurrencyHelper.format(inv.getPaidAmount()));
                        h.tvRemaining.setText("متبقي: " + CurrencyHelper.format(inv.getRemainingAmount()));
                        String statusText = inv.getType() == Invoice.TYPE_SALE ? "عميل" : "مورد";
                        h.tvStatus.setText(statusText);
                    }
                    @Override public int getItemCount() { return debts.size(); }
                });
            });
        });
    }

    static class DebtVH extends RecyclerView.ViewHolder {
        TextView tvInvNumber, tvDate, tvTotal, tvPaid, tvRemaining, tvStatus;
        DebtVH(View v) {
            super(v);
            tvInvNumber = v.findViewById(R.id.tv_debt_invoice_number);
            tvDate = v.findViewById(R.id.tv_debt_date);
            tvTotal = v.findViewById(R.id.tv_debt_total);
            tvPaid = v.findViewById(R.id.tv_debt_paid);
            tvRemaining = v.findViewById(R.id.tv_debt_remaining);
            tvStatus = v.findViewById(R.id.tv_debt_status);
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
