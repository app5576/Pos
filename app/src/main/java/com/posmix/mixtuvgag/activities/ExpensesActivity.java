package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.ExpensesAdapter;
import com.posmix.mixtuvgag.models.Expense;
import com.posmix.mixtuvgag.viewmodels.ExpensesViewModel;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
public class ExpensesActivity extends AppCompatActivity {

    private ExpensesViewModel vm;
    private ExpensesAdapter adapter;

    private static final String[] CATEGORIES = {
        "إيجار", "رواتب", "كهرباء وماء", "هاتف وإنترنت",
        "صيانة", "مواصلات", "مستلزمات مكتبية", "تسويق وإعلان", "أخرى"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("المصاريف");
        }

        RecyclerView rv = findViewById(R.id.rv_expenses);
        FloatingActionButton fab = findViewById(R.id.fab_add_expense);

        vm = new ViewModelProvider(this).get(ExpensesViewModel.class);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpensesAdapter(expense -> confirmDeleteExpense(expense));
        rv.setAdapter(adapter);

        vm.getExpenses().observe(this, expenses -> adapter.submitList(expenses));

        fab.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showAddExpenseDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_expense_category);
        EditText etAmount = view.findViewById(R.id.et_expense_amount);
        EditText etNotes  = view.findViewById(R.id.et_expense_notes);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        new AlertDialog.Builder(this)
            .setTitle("إضافة مصروف")
            .setView(view)
            .setPositiveButton("إضافة", (d, w) -> {
                String amountStr = etAmount.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Expense e = new Expense();
                    e.setCategory(spinnerCategory.getSelectedItem().toString());
                    e.setAmount(Double.parseDouble(amountStr));
                    e.setDate(System.currentTimeMillis());
                    e.setNotes(etNotes.getText().toString().trim());
                    vm.insert(e);
                    Toast.makeText(this, "تم إضافة المصروف", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, "مبلغ غير صحيح", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void confirmDeleteExpense(Expense e) {
        new AlertDialog.Builder(this)
            .setTitle("حذف المصروف")
            .setMessage("هل أنت متأكد من حذف هذا المصروف؟")
            .setPositiveButton("حذف", (d, w) -> {
                vm.delete(e);
                Toast.makeText(this, "تم حذف المصروف", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
