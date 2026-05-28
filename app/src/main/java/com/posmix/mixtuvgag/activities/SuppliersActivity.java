package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.SuppliersAdapter;
import com.posmix.mixtuvgag.models.Supplier;
import com.posmix.mixtuvgag.viewmodels.SuppliersViewModel;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
public class SuppliersActivity extends AppCompatActivity {

    private SuppliersViewModel vm;
    private SuppliersAdapter adapter;
    private RecyclerView rvSuppliers;
    private EditText etSearch;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("الموردون");
        }

        rvSuppliers = findViewById(R.id.rv_suppliers);
        etSearch = findViewById(R.id.et_search_suppliers);
        fabAdd = findViewById(R.id.fab_add_supplier);

        vm = new ViewModelProvider(this).get(SuppliersViewModel.class);

        rvSuppliers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SuppliersAdapter(new SuppliersAdapter.SupplierClickListener() {
            @Override public void onEdit(Supplier s) { showSupplierDialog(s); }
            @Override public void onDelete(Supplier s) { confirmDelete(s); }
        });
        rvSuppliers.setAdapter(adapter);

        vm.getSuppliers().observe(this, suppliers -> adapter.submitList(suppliers));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    vm.getSuppliers().observe(SuppliersActivity.this, list -> adapter.submitList(list));
                } else {
                    vm.search(q).observe(SuppliersActivity.this, list -> adapter.submitList(list));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> showSupplierDialog(null));
    }

    private void showSupplierDialog(Supplier existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_supplier, null);
        EditText etName    = view.findViewById(R.id.et_supplier_name);
        EditText etPhone   = view.findViewById(R.id.et_supplier_phone);
        EditText etAddress = view.findViewById(R.id.et_supplier_address);
        EditText etBalance = view.findViewById(R.id.et_supplier_balance);

        boolean isEdit = existing != null;
        if (isEdit) {
            etName.setText(existing.getName());
            etPhone.setText(existing.getPhone());
            etAddress.setText(existing.getAddress());
            etBalance.setText(String.valueOf(existing.getCurrentBalance()));
        }

        new AlertDialog.Builder(this)
            .setTitle(isEdit ? "تعديل مورد" : "إضافة مورد جديد")
            .setView(view)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (d, w) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال اسم المورد", Toast.LENGTH_SHORT).show();
                    return;
                }
                Supplier s = isEdit ? existing : new Supplier();
                s.setName(name);
                s.setPhone(etPhone.getText().toString().trim());
                s.setAddress(etAddress.getText().toString().trim());
                try {
                    s.setCurrentBalance(Double.parseDouble(etBalance.getText().toString()));
                } catch (NumberFormatException e) { s.setCurrentBalance(0); }
                if (isEdit) { vm.update(s); Toast.makeText(this, "تم تحديث المورد", Toast.LENGTH_SHORT).show(); }
                else { vm.insert(s); Toast.makeText(this, "تم إضافة المورد", Toast.LENGTH_SHORT).show(); }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void confirmDelete(Supplier s) {
        new AlertDialog.Builder(this)
            .setTitle("حذف المورد")
            .setMessage("هل أنت متأكد من حذف المورد: " + s.getName() + "؟")
            .setPositiveButton("حذف", (d, w) -> { vm.delete(s); Toast.makeText(this, "تم حذف المورد", Toast.LENGTH_SHORT).show(); })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
