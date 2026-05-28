package com.posmix.mixtuvgag.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.CartHorizontalAdapter;
import com.posmix.mixtuvgag.adapters.SearchResultAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.CartItem;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.utils.InvoicePdfHelper;
import com.posmix.mixtuvgag.viewmodels.SalesViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalesFragment extends Fragment {

    private SalesViewModel viewModel;
    private CartHorizontalAdapter cartAdapter;
    private SearchResultAdapter searchAdapter;
    private List<CartItem> cart = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private List<Customer> allCustomers = new ArrayList<>();
    private EditText etSearch;
    private TextView tvTotal;
    private Button btnCheckout;
    private String currencySymbol = "ر.س";
    private AppDatabase database;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = AppDatabase.getInstance(requireContext());
        
        // تهيئة CurrencyHelper إذا لم يتم تهيئته
        try {
            CurrencyHelper.init(requireContext());
            currencySymbol = CurrencyHelper.getSymbol();
        } catch (Exception e) {
            currencySymbol = "ر.س";
        }
        
        etSearch = view.findViewById(R.id.etSearch);
        tvTotal = view.findViewById(R.id.tvCartTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        
        setupRecyclerViews(view);
        setupObservers();
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        searchAdapter.setOnProductClickListener(product -> {
            addItemToCart(product);
            etSearch.setText("");
            view.findViewById(R.id.cardSearchResults).setVisibility(View.GONE);
        });
        
        cartAdapter.setOnCartItemClickListener(item -> showEditCartItemDialog(item));
        btnCheckout.setOnClickListener(v -> showCheckoutDialog());
        
        updateUI();
    }
    
    private void setupRecyclerViews(View view) {
        androidx.recyclerview.widget.RecyclerView rvCart = view.findViewById(R.id.rvCartHorizontal);
        cartAdapter = new CartHorizontalAdapter();
        rvCart.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        rvCart.setAdapter(cartAdapter);
        
        androidx.recyclerview.widget.RecyclerView rvResults = view.findViewById(R.id.rvSearchResults);
        searchAdapter = new SearchResultAdapter();
        rvResults.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rvResults.setAdapter(searchAdapter);
    }
    
    private void setupObservers() {
        viewModel = new ViewModelProvider(this).get(SalesViewModel.class);
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProducts = products;
            }
        });
        viewModel.getCustomers().observe(getViewLifecycleOwner(), customers -> {
            if (customers != null) {
                allCustomers = customers;
            }
        });
    }
    
    private void filterSearch(String query) {
        View card = getView().findViewById(R.id.cardSearchResults);
        if (query.isEmpty()) {
            card.setVisibility(View.GONE);
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(query.toLowerCase()) ||
                (p.getBarcode() != null && p.getBarcode().contains(query))) {
                filtered.add(p);
            }
        }
        searchAdapter.submitList(filtered);
        card.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    private void addItemToCart(Product product) {
        for (CartItem item : cart) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                updateUI();
                return;
            }
        }
        cart.add(new CartItem(product.getId(), product.getName(), product.getSellPrice(), product.getTaxPercentage(), 1));
        updateUI();
    }
    
    private void updateUI() {
        cartAdapter.submitList(new ArrayList<>(cart));
        double total = 0;
        for (CartItem item : cart) {
            total += item.getFinalTotal();
        }
        tvTotal.setText(CurrencyHelper.format(total));
        View emptyView = getView().findViewById(R.id.tvEmptyCart);
        if (emptyView != null) {
            emptyView.setVisibility(cart.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    
    // ======================== دالة عرض مربع حوار تعديل العنصر مع دعم الملاحظات ========================
    
    private void showEditCartItemDialog(CartItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_cart_item, null);
        
        TextView tvProductName = view.findViewById(R.id.tv_dialog_edit_product_name);
        TextView tvBuyPrice = view.findViewById(R.id.tv_dialog_edit_buy_price);
        TextView tvExpectedProfit = view.findViewById(R.id.tv_dialog_edit_expected_profit);
        EditText etQuantity = view.findViewById(R.id.et_dialog_edit_quantity);
        EditText etSellPrice = view.findViewById(R.id.et_dialog_edit_sell_price);
        EditText etNotes = view.findViewById(R.id.et_dialog_edit_notes_input);
        TextView tvTotalPrice = view.findViewById(R.id.tv_dialog_edit_total_price);
        Button btnRemove = view.findViewById(R.id.btn_remove_from_cart);
        Button btnCancel = view.findViewById(R.id.btn_cancel_edit);
        Button btnSave = view.findViewById(R.id.btn_save_edit);
        
        tvProductName.setText(item.getProductName());
        
        // جلب سعر التكلفة من قاعدة البيانات
        executor.execute(() -> {
            Product product = database.productDao().getById(item.getProductId());
            double buyPrice = product != null ? product.getBuyPrice() : 0;
            double expectedProfit = item.getUnitPrice() - buyPrice;
            requireActivity().runOnUiThread(() -> {
                tvBuyPrice.setText(CurrencyHelper.format(buyPrice));
                tvExpectedProfit.setText(CurrencyHelper.format(expectedProfit));
                tvExpectedProfit.setTextColor(expectedProfit >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#E53935"));
            });
        });
        
        etQuantity.setText(String.valueOf(item.getQuantity()));
        etSellPrice.setText(String.valueOf(item.getUnitPrice()));
        if (item.getNotes() != null) {
            etNotes.setText(item.getNotes());
        }
        tvTotalPrice.setText(CurrencyHelper.format(item.getFinalTotal()));
        
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    double qty = Double.parseDouble(etQuantity.getText().toString());
                    double price = Double.parseDouble(etSellPrice.getText().toString());
                    tvTotalPrice.setText(CurrencyHelper.format(qty * price));
                    
                    // تحديث الربح المتوقع
                    double buyPrice = 0;
                    try {
                        buyPrice = Double.parseDouble(tvBuyPrice.getText().toString().replaceAll("[^0-9.-]", ""));
                    } catch (Exception ex) {}
                    double expectedProfit = price - buyPrice;
                    tvExpectedProfit.setText(CurrencyHelper.format(expectedProfit));
                    tvExpectedProfit.setTextColor(expectedProfit >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#E53935"));
                } catch (NumberFormatException e) {}
            }
        };
        etQuantity.addTextChangedListener(watcher);
        etSellPrice.addTextChangedListener(watcher);
        
        AlertDialog dialog = builder.setView(view).create();
        
        btnRemove.setOnClickListener(v -> {
            cart.remove(item);
            cartAdapter.submitList(new ArrayList<>(cart));
            updateUI();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            try {
                int newQty = Integer.parseInt(etQuantity.getText().toString());
                double newPrice = Double.parseDouble(etSellPrice.getText().toString());
                String notes = etNotes.getText().toString().trim();
                
                item.setQuantity(newQty);
                item.setUnitPrice(newPrice);
                item.setNotes(notes);
                
                cartAdapter.notifyDataSetChanged();
                updateUI();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "يرجى إدخال قيم صحيحة", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    // ======================== دالة عرض مربع حوار تأكيد البيع ========================
    
    private void showCheckoutDialog() {
        if (cart.isEmpty()) {
            Toast.makeText(getContext(), "السلة فارغة! أضف منتجات أولاً", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_checkout, null);
        
        TextView tvTotalDisplay = view.findViewById(R.id.tv_total_display);
        TextView tvBalanceDisplay = view.findViewById(R.id.tv_balance_display);
        EditText etDiscount = view.findViewById(R.id.et_discount);
        EditText etPaid = view.findViewById(R.id.et_paid_amount);
        AutoCompleteTextView actvCustomer = view.findViewById(R.id.actv_customer);
        RadioGroup rgPayment = view.findViewById(R.id.rg_payment_type);
        EditText etNotes = view.findViewById(R.id.et_notes);
        
        double subtotal = 0;
        for (CartItem item : cart) {
            subtotal += item.getFinalTotal();
        }
        final double finalSubtotal = subtotal;
        
        tvTotalDisplay.setText(CurrencyHelper.format(finalSubtotal));
        etPaid.setText(String.format(Locale.US, "%.2f", finalSubtotal));
        
        // إعداد قائمة العملاء
        List<String> customerNames = new ArrayList<>();
        customerNames.add("بدون عميل");
        for (Customer c : allCustomers) {
            customerNames.add(c.getName());
        }
        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, customerNames);
        actvCustomer.setAdapter(customerAdapter);
        
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    double discount = Double.parseDouble(etDiscount.getText().toString().isEmpty() ? "0" : etDiscount.getText().toString());
                    double paid = Double.parseDouble(etPaid.getText().toString().isEmpty() ? "0" : etPaid.getText().toString());
                    double remaining = finalSubtotal - discount - paid;
                    tvBalanceDisplay.setText(CurrencyHelper.format(remaining));
                } catch (NumberFormatException e) {}
            }
        };
        etDiscount.addTextChangedListener(watcher);
        etPaid.addTextChangedListener(watcher);
        
        builder.setTitle("تأكيد البيع")
                .setView(view)
                .setPositiveButton("تأكيد", (dialog, which) -> {
                    double discount = 0, paid = 0;
                    try {
                        discount = Double.parseDouble(etDiscount.getText().toString().isEmpty() ? "0" : etDiscount.getText().toString());
                        paid = Double.parseDouble(etPaid.getText().toString().isEmpty() ? "0" : etPaid.getText().toString());
                    } catch (NumberFormatException e) {}
                    
                    int paymentStatus;
                    int checkedId = rgPayment.getCheckedRadioButtonId();
                    if (checkedId == R.id.rb_cash) {
                        paymentStatus = Invoice.STATUS_CASH;
                    } else if (checkedId == R.id.rb_card) {
                        paymentStatus = Invoice.STATUS_CARD;
                    } else {
                        paymentStatus = Invoice.STATUS_CREDIT;
                    }
                    
                    String customerName = actvCustomer.getText().toString();
                    Integer customerId = null;
                    if (!customerName.isEmpty() && !customerName.equals("بدون عميل")) {
                        for (Customer c : allCustomers) {
                            if (c.getName().equals(customerName)) {
                                customerId = c.getId();
                                break;
                            }
                        }
                    }
                    
                    String notes = etNotes.getText().toString().trim();
                    saveInvoice(finalSubtotal, discount, paid, paymentStatus, customerId, notes);
                })
                .setNegativeButton("إلغاء", null);
        
        builder.create().show();
    }
    
    private void saveInvoice(double subtotal, double discount, double paid, int paymentStatus, Integer customerId, String notes) {
        executor.execute(() -> {
            try {
                String invoiceNumber = "INV-" + System.currentTimeMillis();
                
                Invoice invoice = new Invoice();
                invoice.setInvoiceNumber(invoiceNumber);
                invoice.setType(Invoice.TYPE_SALE);
                invoice.setCustomerId(customerId);
                invoice.setDate(System.currentTimeMillis());
                invoice.setSubtotal(subtotal);
                invoice.setDiscount(discount);
                invoice.setTaxAmount(0);
                invoice.setTotal(subtotal - discount);
                invoice.setPaidAmount(paid);
                invoice.setRemainingAmount((subtotal - discount) - paid);
                invoice.setPaymentStatus(paymentStatus);
                invoice.setNotes(notes);
                invoice.setPrinted(false);
                
                long invoiceId = database.invoiceDao().insert(invoice);
                
                for (CartItem cartItem : cart) {
                    InvoiceItem item = new InvoiceItem();
                    item.setInvoiceId((int) invoiceId);
                    item.setProductId(cartItem.getProductId());
                    item.setProductName(cartItem.getProductName());
                    item.setQuantity(cartItem.getQuantity());
                    item.setUnitPrice(cartItem.getUnitPrice());
                    item.setTotalPrice(cartItem.getFinalTotal());
                    item.setNotes(cartItem.getNotes());  // حفظ الملاحظات
                    database.invoiceDao().insertItem(item);
                    
                    // تحديث المخزون
                    database.productDao().decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
                }
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "تم حفظ الفاتورة بنجاح", Toast.LENGTH_SHORT).show();
                    cart.clear();
                    updateUI();
                });
                
            } catch (Exception e) {
                Log.e("SalesFragment", "خطأ في حفظ الفاتورة", e);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}