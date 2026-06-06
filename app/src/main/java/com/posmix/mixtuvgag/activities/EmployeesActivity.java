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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.EmployeesAdapter;
import com.posmix.mixtuvgag.models.Employee;
import com.posmix.mixtuvgag.viewmodels.EmployeesViewModel;
import java.security.MessageDigest;

public class EmployeesActivity extends AppCompatActivity implements EmployeesAdapter.Listener {
    private EmployeesViewModel vm;
    private EmployeesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("إدارة الموظفين");
        }
        vm = new ViewModelProvider(this).get(EmployeesViewModel.class);
        RecyclerView rv = findViewById(R.id.rv_employees);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeesAdapter(this);
        rv.setAdapter(adapter);
        vm.getEmployees().observe(this, list -> adapter.submitList(list));
        FloatingActionButton fab = findViewById(R.id.fab_add_employee);
        fab.setOnClickListener(v -> showDialog(null));
    }

    @Override public void onEdit(Employee e) { showDialog(e); }
    @Override public void onDelete(Employee e) {
        new AlertDialog.Builder(this).setTitle("حذف موظف")
            .setMessage("هل تريد حذف " + e.getName() + "؟")
            .setPositiveButton("حذف", (d, w) -> vm.delete(e.getId()))
            .setNegativeButton("إلغاء", null).show();
    }

    private void showDialog(Employee existing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_employee, null);
        EditText etName = v.findViewById(R.id.et_emp_name);
        EditText etUsername = v.findViewById(R.id.et_emp_username);
        EditText etPassword = v.findViewById(R.id.et_emp_password);
        EditText etPhone = v.findViewById(R.id.et_emp_phone);
        EditText etSalary = v.findViewById(R.id.et_emp_salary);
        Spinner spRole = v.findViewById(R.id.sp_emp_role);
        CheckBox cbActive = v.findViewById(R.id.cb_emp_active);

        String[] roles = {"مدير عام", "كاشير", "محاسب", "مشرف"};
        spRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));

        if (existing != null) {
            etName.setText(existing.getName());
            etUsername.setText(existing.getUsername());
            etPhone.setText(existing.getPhone());
            etSalary.setText(String.valueOf(existing.getSalary()));
            spRole.setSelection(existing.getRole() - 1);
            cbActive.setChecked(existing.isActive());
            etPassword.setHint("اتركه فارغاً للإبقاء على كلمة المرور");
        }

        new AlertDialog.Builder(this)
            .setTitle(existing == null ? "إضافة موظف" : "تعديل موظف")
            .setView(v)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = etName.getText().toString().trim();
                String uname = etUsername.getText().toString().trim();
                if (name.isEmpty() || uname.isEmpty()) { Toast.makeText(this, "الاسم واسم المستخدم إلزامي", Toast.LENGTH_SHORT).show(); return; }
                Employee emp = existing != null ? existing : new Employee();
                emp.setName(name);
                emp.setUsername(uname);
                emp.setPhone(etPhone.getText().toString().trim());
                emp.setRole(spRole.getSelectedItemPosition() + 1);
                emp.setActive(cbActive.isChecked());
                try { emp.setSalary(Double.parseDouble(etSalary.getText().toString())); } catch (Exception ex) { emp.setSalary(0); }
                String pwd = etPassword.getText().toString();
                if (!pwd.isEmpty()) emp.setPasswordHash(hashPassword(pwd));
                else if (existing == null) emp.setPasswordHash(hashPassword("1234"));
                vm.save(emp);
            })
            .setNegativeButton("إلغاء", null).show();
    }

    private String hashPassword(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pwd.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return pwd; }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
