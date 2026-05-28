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
import com.posmix.mixtuvgag.adapters.UnitsAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Unit;
import java.util.List;
import java.util.concurrent.Executors;

public class UnitsFragment extends Fragment {
    private RecyclerView rvUnits;
    private UnitsAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        rvUnits = view.findViewById(R.id.rv_units);
        rvUnits.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new UnitsAdapter(new UnitsAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Unit unit) {
                showUnitDialog(unit);
            }
            @Override
            public void onDelete(Unit unit) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.unitDao().delete(unit);
                    requireActivity().runOnUiThread(() -> {
                        loadUnits();
                        Toast.makeText(getContext(), "تم الحذف", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        rvUnits.setAdapter(adapter);
        
        loadUnits();
        
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_unit);
        fabAdd.setOnClickListener(v -> showUnitDialog(null));
    }
    
    private void loadUnits() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Unit> units = db.unitDao().getAllSync();
            requireActivity().runOnUiThread(() -> adapter.submitList(units));
        });
    }
    
    private void showUnitDialog(Unit existingUnit) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_unit, null);
        
        EditText etName = dialogView.findViewById(R.id.et_unit_name);
        
        boolean isEdit = existingUnit != null;
        if (isEdit) {
            etName.setText(existingUnit.getName());
            builder.setTitle("تعديل وحدة");
        } else {
            builder.setTitle("إضافة وحدة جديدة");
        }
        
        builder.setView(dialogView)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "الرجاء إدخال اسم الوحدة", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    if (isEdit) {
                        existingUnit.setName(name);
                        db.unitDao().update(existingUnit);
                    } else {
                        Unit newUnit = new Unit();
                        newUnit.setName(name);
                        db.unitDao().insert(newUnit);
                    }
                    requireActivity().runOnUiThread(() -> {
                        loadUnits();
                        Toast.makeText(getContext(), isEdit ? "تم التحديث" : "تمت الإضافة", Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
