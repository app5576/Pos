package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.UnitsAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Unit;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
public class UnitsManagementActivity extends AppCompatActivity {
    private RecyclerView rvUnits;
    private UnitsAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units_management);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("إدارة وحدات العبوة");
        }
        
        db = AppDatabase.getInstance(this);
        rvUnits = findViewById(R.id.rv_units);
        rvUnits.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new UnitsAdapter(new UnitsAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Unit unit) {
                showUnitDialog(unit);
            }
            @Override
            public void onDelete(Unit unit) {
                new AlertDialog.Builder(UnitsManagementActivity.this)
                    .setTitle("تأكيد الحذف")
                    .setMessage("هل أنت متأكد من حذف الوحدة '" + unit.getName() + "'؟")
                    .setPositiveButton("حذف", (d, w) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.unitDao().delete(unit);
                            runOnUiThread(() -> loadUnits());
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            }
        });
        rvUnits.setAdapter(adapter);
        
        loadUnits();
        
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_unit);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showUnitDialog(null));
        }
    }
    
    private void loadUnits() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Unit> units = db.unitDao().getAllSync();
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.submitList(units);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "خطأ في تحميل البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void showUnitDialog(Unit existingUnit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_unit, null);
        
        EditText etName = view.findViewById(R.id.et_unit_name);
        
        boolean isEdit = existingUnit != null;
        if (isEdit) {
            etName.setText(existingUnit.getName());
            builder.setTitle("تعديل وحدة");
        } else {
            builder.setTitle("إضافة وحدة جديدة");
        }
        
        builder.setView(view)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال اسم الوحدة", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        if (isEdit) {
                            existingUnit.setName(name);
                            db.unitDao().update(existingUnit);
                        } else {
                            Unit newUnit = new Unit();
                            newUnit.setName(name);
                            db.unitDao().insert(newUnit);
                        }
                        runOnUiThread(() -> {
                            loadUnits();
                            Toast.makeText(UnitsManagementActivity.this, isEdit ? "تم التحديث" : "تمت الإضافة", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(UnitsManagementActivity.this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
