package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.InvoiceItemsAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.*;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class EditInvoiceActivity extends AppCompatActivity {

    private int invoiceId;
    private Invoice currentInvoice;
    private List<InvoiceItem> items = new ArrayList<>();
    private AppDatabase db;
    private TextView tvInvoiceNumber, tvInvoiceDate, tvSubtotal, tvDiscount, tvTotal, tvPaid, tvRemaining;
    private EditText etDiscount, etPaidAmount;
    private RecyclerView rvItems;
    private InvoiceItemsAdapter itemsAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
    private boolean isModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_invoice);
        
        db = AppDatabase.getInstance(this);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("تعديل الفاتورة");
        }
        
        invoiceId = getIntent().getIntExtra("invoice_id", -1);
        if (invoiceId == -1) {
            Toast.makeText(this, "خطأ: لم يتم تحديد الفاتورة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadInvoiceData();
    }
    
    private void initViews() {
        tvInvoiceNumber = findViewById(R.id.tv_edit_invoice_number);
        tvInvoiceDate = findViewById(R.id.tv_edit_invoice_date);
        tvSubtotal = findViewById(R.id.tv_edit_subtotal);
        tvDiscount = findViewById(R.id.tv_edit_discount_label);
        tvTotal = findViewById(R.id.tv_edit_total);
        tvPaid = findViewById(R.id.tv_edit_remaining);
        tvRemaining = findViewById(R.id.tv_edit_remaining);
        
        etDiscount = findViewById(R.id.et_edit_discount);
        etPaidAmount = findViewById(R.id.et_edit_paid_amount);
        
        rvItems = findViewById(R.id.rv_edit_invoice_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapter = new InvoiceItemsAdapter(item -> showEditItemDialog(item));
        rvItems.setAdapter(itemsAdapter);
        
        // أزرار
        Button btnSave = findViewById(R.id.btn_save_invoice);
        Button btnAddItem = findViewById(R.id.btn_add_item);
        Button btnDeleteInvoice = findViewById(R.id.btn_delete_invoice);
        
        btnSave.setOnClickListener(v -> saveInvoiceChanges());
        btnAddItem.setOnClickListener(v -> showAddItemDialog());
        btnDeleteInvoice.setOnClickListener(v -> confirmDeleteInvoice());
        
        // مراقبة التغييرات
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                isModified = true;
                updateCalculations();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        
        etDiscount.addTextChangedListener(changeWatcher);
        etPaidAmount.addTextChangedListener(changeWatcher);
    }
    
    private void loadInvoiceData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentInvoice = db.invoiceDao().getInvoiceByIdSync(invoiceId);
            items = db.invoiceDao().getItemsForInvoiceSync(invoiceId);
            
            if (currentInvoice == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "خطأ: الفاتورة غير موجودة", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            
            runOnUiThread(() -> {
                displayInvoiceData();
            });
        });
    }
    
    private void displayInvoiceData() {
        tvInvoiceNumber.setText("رقم الفاتورة: " + currentInvoice.getInvoiceNumber());
        tvInvoiceDate.setText("التاريخ: " + sdf.format(new Date(currentInvoice.getDate())));
        
        etDiscount.setText(String.format(Locale.US, "%.2f", currentInvoice.getDiscount()));
        etPaidAmount.setText(String.format(Locale.US, "%.2f", currentInvoice.getPaidAmount()));
        
        itemsAdapter.submitList(items);
        updateCalculations();
    }
    
    private void updateCalculations() {
        double subtotal = 0;
        for (InvoiceItem item : items) {
            subtotal += item.getTotalPrice();
        }
        
        double discount = 0;
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (NumberFormatException e) {}
        
        double paid = 0;
        try {
            paid = Double.parseDouble(etPaidAmount.getText().toString());
        } catch (NumberFormatException e) {}
        
        double total = subtotal - discount;
        double remaining = total - paid;
        
        tvSubtotal.setText(CurrencyHelper.format(subtotal));
        tvTotal.setText(CurrencyHelper.format(total));
        tvPaid.setText(CurrencyHelper.format(paid));
        tvRemaining.setText(CurrencyHelper.format(remaining));
    }
    
    private void showEditItemDialog(InvoiceItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_invoice_item_detail, null);
        
        TextView tvName = view.findViewById(R.id.tv_item_product_name);
        TextView tvTotalItem = view.findViewById(R.id.tv_item_total_price);
        EditText etQty = view.findViewById(R.id.et_item_quantity);
        EditText etPrice = view.findViewById(R.id.et_item_sell_price);
        Button btnDelete = view.findViewById(R.id.btn_remove_item);
        
        tvName.setText(item.getProductName());
        etQty.setText(String.format(Locale.US, "%d", (int)item.getQuantity()));
        etPrice.setText(String.format(Locale.US, "%.2f", item.getUnitPrice()));
        tvTotalItem.setText(String.format(Locale.US, "%.2f ر.س", item.getTotalPrice()));
        
        TextWatcher watcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                try {
                    double q = Double.parseDouble(etQty.getText().toString());
                    double p = Double.parseDouble(etPrice.getText().toString());
                    tvTotalItem.setText(String.format(Locale.US, "%.2f ر.س", q * p));
                } catch (Exception e) {}
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        etQty.addTextChangedListener(watcher);
        etPrice.addTextChangedListener(watcher);
        
        AlertDialog dialog = builder.setView(view)
            .setPositiveButton("تحديث", (d, w) -> {
                try {
                    double qty = Double.parseDouble(etQty.getText().toString());
                    double price = Double.parseDouble(etPrice.getText().toString());
                    item.setQuantity(qty);
                    item.setUnitPrice(price);
                    item.setTotalPrice(qty * price);
                    
                    isModified = true;
                    itemsAdapter.notifyDataSetChanged();
                    updateCalculations();
                    Toast.makeText(this, "تم تحديث الصنف", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "قيم غير صالحة", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .create();
        
        btnDelete.setOnClickListener(v -> {
            items.remove(item);
            isModified = true;
            itemsAdapter.submitList(items);
            updateCalculations();
            dialog.dismiss();
            Toast.makeText(this, "تم حذف الصنف", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show();
    }
    
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        
        EditText etProductName = view.findViewById(R.id.et_new_item_name);
        EditText etQuantity = view.findViewById(R.id.et_new_item_qty);
        EditText etPrice = view.findViewById(R.id.et_new_item_price);
        
        builder.setTitle("إضافة صنف جديد")
            .setView(view)
            .setPositiveButton("إضافة", (d, w) -> {
                String name = etProductName.getText().toString().trim();
                String qtyStr = etQuantity.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                
                if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    double qty = Double.parseDouble(qtyStr);
                    double price = Double.parseDouble(priceStr);
                    
                    InvoiceItem newItem = new InvoiceItem();
                    newItem.setInvoiceId(invoiceId);
                    newItem.setProductName(name);
                    newItem.setQuantity(qty);
                    newItem.setUnitPrice(price);
                    newItem.setTotalPrice(qty * price);
                    
                    items.add(newItem);
                    isModified = true;
                    itemsAdapter.submitList(items);
                    updateCalculations();
                    Toast.makeText(this, "تم إضافة الصنف", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "قيم غير صالحة", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void saveInvoiceChanges() {
        if (!isModified) {
            Toast.makeText(this, "لا توجد تغييرات للحفظ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("حفظ التغييرات")
            .setMessage("هل أنت متأكد من حفظ التغييرات على الفاتورة؟")
            .setPositiveButton("حفظ", (d, w) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        double discount = 0;
                        double paid = 0;
                        try {
                            discount = Double.parseDouble(etDiscount.getText().toString());
                            paid = Double.parseDouble(etPaidAmount.getText().toString());
                        } catch (NumberFormatException e) {}
                        
                        double subtotal = 0;
                        for (InvoiceItem item : items) {
                            subtotal += item.getTotalPrice();
                        }
                        
                        currentInvoice.setSubtotal(subtotal);
                        currentInvoice.setDiscount(discount);
                        currentInvoice.setTotal(subtotal - discount);
                        currentInvoice.setPaidAmount(paid);
                        currentInvoice.setRemainingAmount(currentInvoice.getTotal() - paid);
                        
                        if (currentInvoice.getRemainingAmount() <= 0) {
                            currentInvoice.setPaymentStatus(Invoice.STATUS_CASH);
                        } else if (paid > 0) {
                            currentInvoice.setPaymentStatus(Invoice.STATUS_PARTIAL);
                        } else {
                            currentInvoice.setPaymentStatus(Invoice.STATUS_CREDIT);
                        }
                        
                        db.getWritableDatabase().beginTransaction();
                        try {
                            db.invoiceDao().update(currentInvoice);
                            
                            // حذف العناصر القديمة وإضافة الجديدة
                            List<InvoiceItem> oldItems = db.invoiceDao().getItemsForInvoiceSync(invoiceId);
                            for (InvoiceItem oldItem : oldItems) {
                                db.invoiceDao().deleteItem(oldItem);
                            }
                            
                            for (InvoiceItem item : items) {
                                item.setInvoiceId(invoiceId);
                                db.invoiceDao().insertItem(item);
                            }
                            
                            db.getWritableDatabase().setTransactionSuccessful();
                            
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ تم حفظ التغييرات بنجاح", Toast.LENGTH_SHORT).show();
                                isModified = false;
                                finish();
                            });
                        } finally {
                            db.getWritableDatabase().endTransaction();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> 
                            Toast.makeText(this, "❌ خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void confirmDeleteInvoice() {
        new AlertDialog.Builder(this)
            .setTitle("حذف الفاتورة")
            .setMessage("هل أنت متأكد من حذف هذه الفاتورة؟ هذا الإجراء لا يمكن التراجع عنه.")
            .setPositiveButton("حذف", (d, w) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        // استعادة المخزون
                        for (InvoiceItem item : items) {
                            db.productDao().increaseStock(item.getProductId(), (int) item.getQuantity());
                        }
                        
                        db.invoiceDao().delete(currentInvoice);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "تم حذف الفاتورة", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> 
                            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        if (isModified) {
            new AlertDialog.Builder(this)
                .setTitle("تغييرات غير محفوظة")
                .setMessage("هناك تغييرات لم يتم حفظها. هل تريد المغادرة؟")
                .setPositiveButton("مغادرة", (d, w) -> finish())
                .setNegativeButton("البقاء", null)
                .show();
            return true;
        }
        finish();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                .setTitle("تغييرات غير محفوظة")
                .setMessage("هناك تغييرات لم يتم حفظها. هل تريد المغادرة؟")
                .setPositiveButton("مغادرة", (d, w) -> super.onBackPressed())
                .setNegativeButton("البقاء", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}