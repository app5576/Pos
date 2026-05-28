package com.posmix.mixtuvgag.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.CategoriesAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Category;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoriesFragment extends Fragment {
    private RecyclerView rvCategories;
    private CategoriesAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new CategoriesAdapter(new CategoriesAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Category category) {
                showCategoryDialog(category);
            }
            @Override
            public void onDelete(Category category) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    // التحقق من وجود منتجات مرتبطة
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "لا يمكن الحذف لوجود منتجات مرتبطة", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        rvCategories.setAdapter(adapter);
        
        loadCategories();
        
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_category);
        fabAdd.setOnClickListener(v -> showCategoryDialog(null));
    }
    
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = db.categoryDao().getAllSync();
            requireActivity().runOnUiThread(() -> adapter.submitList(categories));
        });
    }
    
    private void showCategoryDialog(Category existingCategory) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category, null);
        
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        EditText etDescription = dialogView.findViewById(R.id.et_category_description);
        
        boolean isEdit = existingCategory != null;
        if (isEdit) {
            etName.setText(existingCategory.getName());
            etDescription.setText(existingCategory.getDescription());
            builder.setTitle("تعديل مجموعة");
        } else {
            builder.setTitle("إضافة مجموعة جديدة");
        }
        
        builder.setView(dialogView)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "الرجاء إدخال اسم المجموعة", Toast.LENGTH_SHORT).show();
                    return;
                }
                String description = etDescription.getText().toString().trim();
                
                Executors.newSingleThreadExecutor().execute(() -> {
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
                    requireActivity().runOnUiThread(() -> {
                        loadCategories();
                        Toast.makeText(getContext(), isEdit ? "تم التحديث" : "تمت الإضافة", Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
