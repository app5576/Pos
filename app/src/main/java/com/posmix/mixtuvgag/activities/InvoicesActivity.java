package com.posmix.mixtuvgag.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.InvoicesAdapter;
import com.posmix.mixtuvgag.databinding.ActivityInvoicesBinding;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.*;
import com.posmix.mixtuvgag.viewmodels.InvoicesViewModel;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.util.List;
import java.util.concurrent.Executors;

public class InvoicesActivity extends AppCompatActivity {
    private ActivityInvoicesBinding binding;
    private InvoicesViewModel viewModel;
    private InvoicesAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = AppDatabase.getInstance(this);

        setSupportActionBar(binding.toolbar);
        viewModel = new ViewModelProvider(this).get(InvoicesViewModel.class);
        binding.rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoicesAdapter(invoice -> showInvoiceItems(invoice));
        binding.rvInvoices.setAdapter(adapter);

        loadAllInvoices();
    }

    private void loadAllInvoices() {
        viewModel.getAllInvoices().observe(this, invoices -> adapter.submitList(invoices));
    }

    private void showInvoiceItems(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
            runOnUiThread(() -> {
                if (items == null || items.isEmpty()) return;
                String[] itemNames = new String[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    itemNames[i] = items.get(i).getProductName() + " - " + CurrencyHelper.format(items.get(i).getTotalPrice());
                }
                new AlertDialog.Builder(this)
                    .setTitle("رقم: " + invoice.getInvoiceNumber())
                    .setItems(itemNames, (dialog, which) -> showInvoiceItemDetail(items.get(which)))
                    .setPositiveButton("إغلاق", null)
                    .show();
            });
        });
    }

    private void showInvoiceItemDetail(InvoiceItem item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Product product = db.productDao().getById(item.getProductId());
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_invoice_item_detail, null);
                
                TextView tvName = view.findViewById(R.id.tv_item_product_name);
                TextView tvTotalItem = view.findViewById(R.id.tv_item_total_price);
                TextView tvProfit = view.findViewById(R.id.tv_item_expected_profit);
                EditText etQty = view.findViewById(R.id.et_item_quantity);
                EditText etPrice = view.findViewById(R.id.et_item_sell_price);
                Button btnDelete = view.findViewById(R.id.btn_remove_item);

                tvName.setText(item.getProductName());
                double buyPrice = (product != null) ? product.getBuyPrice() : 0;
                etQty.setText(String.valueOf((int)item.getQuantity()));
                etPrice.setText(String.format("%.2f", item.getUnitPrice()));

                TextWatcher watcher = new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                        try {
                            double q = Double.parseDouble(etQty.getText().toString());
                            double p = Double.parseDouble(etPrice.getText().toString());
                            tvTotalItem.setText(String.format("%.2f ر.س", q * p));
                            double pr = (p - buyPrice) * q;
                            tvProfit.setText(String.format("%.2f", pr));
                            tvProfit.setTextColor(pr >= 0 ? Color.parseColor("#2E7D32") : Color.RED);
                        } catch (Exception e) {}
                    }
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override public void afterTextChanged(Editable s) {}
                };
                etQty.addTextChangedListener(watcher);
                etPrice.addTextChangedListener(watcher);
                etQty.setText(etQty.getText());

                AlertDialog dialog = builder.setView(view)
                    .setPositiveButton("تحديث", (d, w) -> {
                        item.setQuantity(Double.parseDouble(etQty.getText().toString()));
                        item.setUnitPrice(Double.parseDouble(etPrice.getText().toString()));
                        item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.invoiceDao().updateItem(item);
                            runOnUiThread(() -> loadAllInvoices());
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .create();

                btnDelete.setOnClickListener(v -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.invoiceDao().deleteItem(item);
                        runOnUiThread(() -> { loadAllInvoices(); dialog.dismiss(); });
                    });
                });
                dialog.show();
            });
        });
    }
}