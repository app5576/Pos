package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.ShiftsAdapter;
import com.posmix.mixtuvgag.models.Shift;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.viewmodels.EmployeesViewModel;
import com.posmix.mixtuvgag.viewmodels.ShiftViewModel;
import com.posmix.mixtuvgag.models.Employee;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShiftManagementActivity extends AppCompatActivity {
    private ShiftViewModel vm;
    private EmployeesViewModel empVm;
    private ShiftsAdapter adapter;
    private MaterialButton btnOpenShift, btnCloseShift;
    private TextView tvCurrentShiftInfo;
    private Shift currentShift;
    private List<Employee> employees = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_management);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("إدارة الورديات");
        }
        vm = new ViewModelProvider(this).get(ShiftViewModel.class);
        empVm = new ViewModelProvider(this).get(EmployeesViewModel.class);

        btnOpenShift = findViewById(R.id.btn_open_shift);
        btnCloseShift = findViewById(R.id.btn_close_shift);
        tvCurrentShiftInfo = findViewById(R.id.tv_current_shift_info);
        RecyclerView rv = findViewById(R.id.rv_shifts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShiftsAdapter();
        rv.setAdapter(adapter);

        vm.getShifts().observe(this, list -> adapter.submitList(list));
        vm.getCurrentShift().observe(this, shift -> {
            currentShift = shift;
            updateUI(shift);
        });
        empVm.getEmployees().observe(this, list -> employees = list);

        btnOpenShift.setOnClickListener(v -> showOpenShiftDialog());
        btnCloseShift.setOnClickListener(v -> showCloseShiftDialog());
    }

    private void updateUI(Shift shift) {
        if (shift != null) {
            tvCurrentShiftInfo.setText("وردية مفتوحة منذ: " + sdf.format(new Date(shift.getOpenTime())) +
                "\nموظف: " + (shift.getEmployeeName() != null ? shift.getEmployeeName() : "-") +
                "\nمبلغ الفتح: " + CurrencyHelper.format(shift.getOpeningCash()));
            btnOpenShift.setEnabled(false);
            btnCloseShift.setEnabled(true);
        } else {
            tvCurrentShiftInfo.setText("لا توجد وردية مفتوحة حالياً");
            btnOpenShift.setEnabled(true);
            btnCloseShift.setEnabled(false);
        }
    }

    private void showOpenShiftDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_shift, null);
        Spinner spEmployee = v.findViewById(R.id.sp_shift_employee);
        EditText etOpeningCash = v.findViewById(R.id.et_opening_cash);
        EditText etNotes = v.findViewById(R.id.et_shift_notes);
        TextView tvTitle = v.findViewById(R.id.tv_shift_dialog_title);
        if (tvTitle != null) tvTitle.setText("فتح وردية جديدة");
        etOpeningCash.setVisibility(View.VISIBLE);

        List<String> empNames = new ArrayList<>();
        empNames.add("-- اختر موظف --");
        for (Employee e : employees) empNames.add(e.getName());
        spEmployee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, empNames));

        new AlertDialog.Builder(this).setTitle("فتح وردية").setView(v)
            .setPositiveButton("فتح", (d, w) -> {
                Shift s = new Shift();
                int empIdx = spEmployee.getSelectedItemPosition();
                if (empIdx > 0 && empIdx <= employees.size()) {
                    s.setEmployeeId(employees.get(empIdx - 1).getId());
                }
                try { s.setOpeningCash(Double.parseDouble(etOpeningCash.getText().toString())); } catch (Exception ex) { s.setOpeningCash(0); }
                s.setNotes(etNotes.getText().toString().trim());
                vm.openShift(s);
            }).setNegativeButton("إلغاء", null).show();
    }

    private void showCloseShiftDialog() {
        if (currentShift == null) return;
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_shift, null);
        EditText etClosingCash = v.findViewById(R.id.et_opening_cash);
        EditText etNotes = v.findViewById(R.id.et_shift_notes);
        TextView tvTitle = v.findViewById(R.id.tv_shift_dialog_title);
        if (tvTitle != null) tvTitle.setText("إغلاق الوردية");
        if (etClosingCash != null) etClosingCash.setHint("مبلغ إغلاق الصندوق");
        Spinner spEmployee = v.findViewById(R.id.sp_shift_employee);
        if (spEmployee != null) spEmployee.setVisibility(View.GONE);

        new AlertDialog.Builder(this).setTitle("إغلاق الوردية").setView(v)
            .setPositiveButton("إغلاق", (d, w) -> {
                try { currentShift.setClosingCash(Double.parseDouble(etClosingCash.getText().toString())); } catch (Exception ex) { currentShift.setClosingCash(0); }
                currentShift.setNotes(etNotes.getText().toString().trim());
                vm.closeShift(currentShift);
            }).setNegativeButton("إلغاء", null).show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
