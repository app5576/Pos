package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.ProductsAdapter;
import com.posmix.mixtuvgag.models.CartItem;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.viewmodels.PurchasesViewModel;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

public class PurchasesActivity extends AppCompatActivity {

    private PurchasesViewModel vm;
    private ProductsAdapter productsAdapter;
    private List<CartItem> cart = new ArrayList<>();
    private TextView tvCartTotal, tvCartCount;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("المشتريات");
        }

        rv_setup();
        vm_setup();
        
        findViewById(R.id.btn_confirm_purchase).setOnClickListener(v -> showCheckoutDialog());
    }

    private void rv_setup() {
        RecyclerView rvProducts = findViewById(R.id.rv_purchase_products);
        tvCartTotal = findViewById(R.id.tv_purchase_total);
        tvCartCount = findViewById(R.id.tv_purchase_count);
        etSearch = findViewById(R.id.et_search_purchase);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productsAdapter = new ProductsAdapter(new ProductsAdapter.ProductClickListener() {
            @Override public void onProductClick(Product p) { addToCart(p); }
            @Override public void onDeleteClick(Product p) { removeFromCart(p); }
            @Override public void onEditSellPriceClick(Product product) {}
        });
        rvProducts.setAdapter(productsAdapter);
    }

    private void vm_setup() {
        vm = new ViewModelProvider(this).get(PurchasesViewModel.class);
        vm.getProducts().observe(this, products -> productsAdapter.submitList(products));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (q.isEmpty()) vm.getProducts().observe(PurchasesActivity.this, list -> productsAdapter.submitList(list));
                else vm.searchProducts(q).observe(PurchasesActivity.this, list -> productsAdapter.submitList(list));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void addToCart(Product p) {
        for (CartItem item : cart) {
            if (item.getProductId() == p.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                updateCartUI();
                return;
            }
        }
        cart.add(new CartItem(p.getId(), p.getName(), p.getBuyPrice(), p.getTaxPercentage(), 1));
        updateCartUI();
    }

    private void removeFromCart(Product p) {
        cart.removeIf(item -> item.getProductId() == p.getId());
        updateCartUI();
    }

    private void updateCartUI() {
        double total = 0;
        for (CartItem item : cart) total += item.getFinalTotal();
        tvCartTotal.setText(String.format("الإجمالي: %.2f ر.س", total));
        tvCartCount.setText(String.format("الأصناف: %d", cart.size()));
    }

    private void showCheckoutDialog() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "الفاتورة فارغة!", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null);
        
        // ربط العناصر الجديدة (التي تم تغيير أسمائها في التحديث السابق)
        TextView tvTotalDisplay = view.findViewById(R.id.tv_total_display);
        RadioGroup rgPayment = view.findViewById(R.id.rg_payment_type);
        EditText etPaid = view.findViewById(R.id.et_paid_amount);
        EditText etDiscount = view.findViewById(R.id.et_discount);
        
        // إخفاء حقل العميل في المشتريات لأنه مخصص للموردين (اختياري)
        if (view.findViewById(R.id.actv_customer) != null) {
            view.findViewById(R.id.actv_customer).setVisibility(View.GONE);
        }

        double subtotal = 0;
        for (CartItem item : cart) subtotal += item.getFinalTotal();
        final double finalSubtotal = subtotal;
        
        tvTotalDisplay.setText(String.format("إجمالي الشراء: %.2f ر.س", subtotal));
        etPaid.setText(String.format("%.2f", subtotal));

        new AlertDialog.Builder(this)
            .setTitle("تأكيد فاتورة شراء")
            .setView(view)
            .setPositiveButton("حفظ", (d, w) -> savePurchaseInvoice(rgPayment, etPaid, etDiscount, finalSubtotal))
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void savePurchaseInvoice(RadioGroup rgPayment, EditText etPaid, EditText etDiscount, double subtotal) {
        int status;
        int id = rgPayment.getCheckedRadioButtonId();
        if (id == R.id.rb_cash) status = Invoice.STATUS_CASH;
        else if (id == R.id.rb_card) status = Invoice.STATUS_CARD;
        else status = Invoice.STATUS_CREDIT;

        double disc = 0, paid = 0;
        try { disc = Double.parseDouble(etDiscount.getText().toString()); } catch (Exception e) {}
        try { paid = Double.parseDouble(etPaid.getText().toString()); } catch (Exception e) {}

        Invoice inv = new Invoice();
        inv.setType(Invoice.TYPE_PURCHASE);
        inv.setDate(System.currentTimeMillis());
        inv.setInvoiceNumber("PUR-" + System.currentTimeMillis() / 1000);
        inv.setSubtotal(subtotal);
        inv.setDiscount(disc);
        inv.setTotal(subtotal - disc);
        inv.setPaidAmount(paid);
        inv.setRemainingAmount(inv.getTotal() - paid);
        inv.setPaymentStatus(status);

        List<InvoiceItem> items = new ArrayList<>();
        for (CartItem ci : cart) {
            InvoiceItem ii = new InvoiceItem();
            ii.setProductId(ci.getProductId());
            ii.setProductName(ci.getProductName());
            ii.setQuantity(ci.getQuantity());
            ii.setUnitPrice(ci.getUnitPrice());
            ii.setTotalPrice(ci.getFinalTotal());
            items.add(ii);
        }

        vm.savePurchaseInvoice(inv, items, () -> runOnUiThread(() -> {
            Toast.makeText(this, "تم حفظ المشتريات ✓", Toast.LENGTH_SHORT).show();
            cart.clear();
            updateCartUI();
        }));
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
