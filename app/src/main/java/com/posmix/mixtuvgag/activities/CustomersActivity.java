package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.CustomersAdapter;
import com.posmix.mixtuvgag.databinding.ActivityCustomersBinding;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.viewmodels.CustomersViewModel;

import androidx.annotation.NonNull;
import java.util.Locale; // Import Locale
public class CustomersActivity extends AppCompatActivity {
    private ActivityCustomersBinding binding;
    private CustomersViewModel vm;
    private CustomersAdapter adapter;

    public static final String EXTRA_CUSTOMER_ID = "customer_id";
    
    // تنسيق الأرقام باللغة الإنجليزية فقط
    private static final Locale ENGLISH_LOCALE = Locale.US;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("العملاء");
        }
        
        vm = new ViewModelProvider(this).get(CustomersViewModel.class);
        
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomersAdapter(new CustomersAdapter.CustomerActionsListener() {
            @Override
            public void onCustomerClick(Customer customer) {
                // Open CustomerDetailActivity when a customer is clicked
                Intent intent = new Intent(CustomersActivity.this, CustomerDetailActivity.class);
                intent.putExtra(EXTRA_CUSTOMER_ID, customer.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(Customer customer) {
                showCustomerDialog(customer);
            }

            @Override
            public void onDelete(Customer customer) {
                confirmDeleteCustomer(customer);
            }
        });
        binding.rvCustomers.setAdapter(adapter);
        
        vm.getCustomers().observe(this, customers -> {
            adapter.submitList(customers);
        });
        
        binding.fabAdd.setOnClickListener(v -> showAddCustomerDialog());
    }
    
    private void showAddCustomerDialog() {
        showCustomerDialog(null);
    }
    
    private void showCustomerDialog(Customer existingCustomer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_customer, null);
        
        EditText etName = view.findViewById(R.id.et_customer_name);
        EditText etPhone = view.findViewById(R.id.et_customer_phone);
        EditText etAddress = view.findViewById(R.id.et_customer_address);
        EditText etEmail = view.findViewById(R.id.et_customer_email);
        
        boolean isEdit = existingCustomer != null;
        if (isEdit) {
            etName.setText(existingCustomer.getName());
            etPhone.setText(formatNumberToEnglishDigits(existingCustomer.getPhone())); // Ensure English digits
            etAddress.setText(existingCustomer.getAddress());
            etEmail.setText(existingCustomer.getEmail());
            builder.setTitle("تعديل عميل");
        } else {
            builder.setTitle("إضافة عميل جديد");
        }
        
        builder.setView(view)
                .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    
                    if (name.isEmpty()) {
                        Toast.makeText(this, "الرجاء إدخال اسم العميل", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Customer customer;
                    if (isEdit) {
                        customer = existingCustomer;
                        customer.setName(name);
                        customer.setPhone(phone);
                        customer.setAddress(address);
                        customer.setEmail(email);
                        vm.update(customer);
                        Toast.makeText(this, "تم تحديث العميل", Toast.LENGTH_SHORT).show();
                    } else {
                        customer = new Customer();
                        customer.setName(name);
                        customer.setPhone(phone);
                        customer.setAddress(address);
                        customer.setEmail(email);
                        vm.insert(customer);
                        Toast.makeText(this, "تم إضافة العميل", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmDeleteCustomer(Customer customer) {
        new AlertDialog.Builder(this)
            .setTitle("حذف العميل")
            .setMessage("هل أنت متأكد من حذف العميل '" + customer.getName() + "'؟ هذا الإجراء لا يمكن التراجع عنه.")
            .setPositiveButton("حذف", (dialog, which) -> {
                vm.delete(customer);
                Toast.makeText(this, "تم حذف العميل", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    // Helper method to convert Arabic-Indic digits to Western Arabic digits
    private String formatNumberToEnglishDigits(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '٠' && c <= '٩') { // Arabic-Indic digits
                builder.append((char) (c - '٠' + '0')); // Convert to Western Arabic digits
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}