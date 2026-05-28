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
import com.posmix.mixtuvgag.adapters.CategoriesAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Category;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoriesManagementActivity extends AppCompatActivity {
    private RecyclerView rvCategories;
    private CategoriesAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_management);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("إدارة المجموعات");
        }
        
        db = AppDatabase.getInstance(this);
        rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new CategoriesAdapter(new CategoriesAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Category category) {
                showCategoryDialog(category);
            }
            @Override
            public void onDelete(Category category) {
                new AlertDialog.Builder(CategoriesManagementActivity.this)
                    .setTitle("تأكيد الحذف")
                    .setMessage("هل أنت متأكد من حذف المجموعة '" + category.getName() + "'؟\nملاحظة: لن يتم حذف المنتجات المرتبطة بها.")
                    .setPositiveButton("حذف", (d, w) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.categoryDao().delete(category);
                            runOnUiThread(() -> loadCategories());
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            }
        });
        rvCategories.setAdapter(adapter);
        
        loadCategories();
        
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_category);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showCategoryDialog(null));
        }
    }
    
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Category> categories = db.categoryDao().getAllSync();
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.submitList(categories);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "خطأ في تحميل البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void showCategoryDialog(Category existingCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        
        EditText etName = view.findViewById(R.id.et_category_name);
        EditText etDescription = view.findViewById(R.id.et_category_description);
        
        boolean isEdit = existingCategory != null;
        if (isEdit) {
            etName.setText(existingCategory.getName());
            etDescription.setText(existingCategory.getDescription() != null ? existingCategory.getDescription() : "");
            builder.setTitle("تعديل مجموعة");
        } else {
            builder.setTitle("إضافة مجموعة جديدة");
        }
        
        builder.setView(view)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال اسم المجموعة", Toast.LENGTH_SHORT).show();
                    return;
                }
                String description = etDescription.getText().toString().trim();
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        if (isEdit) {
                            existingCategory.setName(name);
                            existingCategory.setDescription(description);
                            db.categoryDao().update(existingCategory);
                        } else {
                            Category newCategory = new Category();
                            newCategory.setName(name);
                            newCategory.setDescription(description);
                            db.categoryDao().insert(newCategory);
                        }
                        runOnUiThread(() -> {
                            loadCategories();
                            Toast.makeText(CategoriesManagementActivity.this, isEdit ? "تم التحديث" : "تمت الإضافة", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CategoriesManagementActivity.this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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