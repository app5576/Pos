package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.ReturnInvoicesAdapter;
import com.posmix.mixtuvgag.database.DatabaseHelper;
import com.posmix.mixtuvgag.models.*;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.viewmodels.ReturnInvoiceViewModel;
import java.util.*;
import java.util.concurrent.Executors;

public class ReturnInvoiceActivity extends AppCompatActivity implements ReturnInvoicesAdapter.Listener {
    private ReturnInvoiceViewModel vm;
    private ReturnInvoicesAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_invoice);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("فواتير المرتجعات");
        }
        db = new DatabaseHelper(this);
        vm = new ViewModelProvider(this).get(ReturnInvoiceViewModel.class);
        RecyclerView rv = findViewById(R.id.rv_return_invoices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReturnInvoicesAdapter(this);
        rv.setAdapter(adapter);
        vm.getReturns().observe(this, list -> adapter.submitList(list));

        FloatingActionButton fab = findViewById(R.id.fab_add_return);
        fab.setOnClickListener(v -> showNewReturnDialog());
    }

    @Override
    public void onItemClick(ReturnInvoice r) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ReturnInvoiceItem> items = db.getReturnInvoiceDao().getItemsByReturnId(r.getId());
            runOnUiThread(() -> showReturnDetailsDialog(r, items));
        });
    }

    private void showNewReturnDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_return_item, null);
        EditText etInvoiceNumber = v.findViewById(R.id.et_original_invoice_number);
        RadioGroup rgType = v.findViewById(R.id.rg_return_type);
        EditText etNotes = v.findViewById(R.id.et_return_notes);
        MaterialButton btnSearch = v.findViewById(R.id.btn_search_invoice);
        LinearLayout llInvoiceItems = v.findViewById(R.id.ll_invoice_items);
        final Invoice[] foundInvoice = {null};

        btnSearch.setOnClickListener(btn -> {
            String num = etInvoiceNumber.getText().toString().trim();
            if (num.isEmpty()) { Toast.makeText(this, "أدخل رقم الفاتورة", Toast.LENGTH_SHORT).show(); return; }
            Executors.newSingleThreadExecutor().execute(() -> {
                Invoice inv = db.getInvoiceByNumber(num);
                runOnUiThread(() -> {
                    if (inv == null) {
                        Toast.makeText(this, "الفاتورة غير موجودة", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    foundInvoice[0] = inv;
                    llInvoiceItems.removeAllViews();
                    if (inv.getItems() != null) {
                        for (InvoiceItem item : inv.getItems()) {
                            TextView tv = new TextView(this);
                            tv.setText("• " + item.getProductName() + " - الكمية: " + item.getQuantity() + " - السعر: " + CurrencyHelper.format(item.getUnitPrice()));
                            tv.setPadding(16, 8, 16, 8);
                            llInvoiceItems.addView(tv);
                        }
                    }
                    Toast.makeText(this, "تم العثور على الفاتورة ✓", Toast.LENGTH_SHORT).show();
                });
            });
        });

        new AlertDialog.Builder(this).setTitle("فاتورة مرتجع جديدة").setView(v)
            .setPositiveButton("إنشاء المرتجع", (d, w) -> {
                if (foundInvoice[0] == null) { Toast.makeText(this, "يجب البحث عن فاتورة أولاً", Toast.LENGTH_SHORT).show(); return; }
                Invoice orig = foundInvoice[0];
                ReturnInvoice ret = new ReturnInvoice();
                int type = rgType.getCheckedRadioButtonId() == R.id.rb_sale_return ?
                    ReturnInvoice.TYPE_SALE_RETURN : ReturnInvoice.TYPE_PURCHASE_RETURN;
                ret.setType(type);
                ret.setOriginalInvoiceId(orig.getId());
                ret.setDate(System.currentTimeMillis());
                ret.setNotes(etNotes.getText().toString().trim());
                if (orig.getCustomerId() != null) ret.setCustomerId(orig.getCustomerId());
                if (orig.getSupplierId() != null) ret.setSupplierId(orig.getSupplierId());
                ret.setSubtotal(orig.getSubtotal());
                ret.setTaxAmount(orig.getTaxAmount());
                ret.setDiscount(orig.getDiscount());
                ret.setTotal(orig.getTotal());

                Executors.newSingleThreadExecutor().execute(() -> {
                    String returnNum = "RET-" + System.currentTimeMillis();
                    ret.setReturnNumber(returnNum);
                    long retId = db.getReturnInvoiceDao().insertReturnInvoice(ret);
                    if (orig.getItems() != null) {
                        for (InvoiceItem item : orig.getItems()) {
                            ReturnInvoiceItem ri = new ReturnInvoiceItem();
                            ri.setReturnInvoiceId((int) retId);
                            ri.setProductId(item.getProductId());
                            ri.setOriginalItemId(item.getId());
                            ri.setQuantity((int) item.getQuantity());
                            ri.setUnitPrice(item.getUnitPrice());
                            ri.setTaxPercentage(item.getTaxPercentage());
                            ri.setTaxAmount(item.getTaxAmount());
                            ri.setTotal(item.getTotal());
                            db.getReturnInvoiceDao().insertItem(ri);
                            // إعادة الكمية للمخزون إذا كان مرتجع مبيعات
                            if (type == ReturnInvoice.TYPE_SALE_RETURN) {
                                db.restoreStockForReturn(item.getProductId(), (int) item.getQuantity());
                            }
                        }
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "تم إنشاء المرتجع " + returnNum, Toast.LENGTH_LONG).show();
                        vm.load();
                    });
                });
            }).setNegativeButton("إلغاء", null).show();
    }

    private void showReturnDetailsDialog(ReturnInvoice r, List<ReturnInvoiceItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("رقم المرتجع: ").append(r.getReturnNumber()).append("\n");
        sb.append("نوع: ").append(r.getType() == ReturnInvoice.TYPE_SALE_RETURN ? "مرتجع مبيعات" : "مرتجع مشتريات").append("\n");
        sb.append("الإجمالي: ").append(CurrencyHelper.format(r.getTotal())).append("\n\nالأصناف:\n");
        for (ReturnInvoiceItem item : items) {
            sb.append("• ").append(item.getProductName()).append(" x").append(item.getQuantity())
              .append(" = ").append(CurrencyHelper.format(item.getTotal())).append("\n");
        }
        new AlertDialog.Builder(this).setTitle("تفاصيل المرتجع")
            .setMessage(sb.toString()).setPositiveButton("إغلاق", null).show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
