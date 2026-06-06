package com.posmix.mixtuvgag.activities;

import android.app.DatePickerDialog;
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
import com.posmix.mixtuvgag.adapters.PromotionsAdapter;
import com.posmix.mixtuvgag.models.Promotion;
import com.posmix.mixtuvgag.viewmodels.PromotionsViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PromotionsActivity extends AppCompatActivity implements PromotionsAdapter.Listener {
    private PromotionsViewModel vm;
    private PromotionsAdapter adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ar"));
    private Calendar calStart = Calendar.getInstance();
    private Calendar calEnd = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("العروض والخصومات");
        }
        vm = new ViewModelProvider(this).get(PromotionsViewModel.class);
        RecyclerView rv = findViewById(R.id.rv_promotions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PromotionsAdapter(this);
        rv.setAdapter(adapter);
        vm.getPromotions().observe(this, list -> adapter.submitList(list));
        FloatingActionButton fab = findViewById(R.id.fab_add_promotion);
        fab.setOnClickListener(v -> showDialog(null));
    }

    @Override public void onEdit(Promotion p) { showDialog(p); }
    @Override public void onDelete(Promotion p) {
        new AlertDialog.Builder(this).setTitle("حذف العرض")
            .setMessage("هل تريد حذف " + p.getName() + "؟")
            .setPositiveButton("حذف", (d, w) -> vm.delete(p.getId()))
            .setNegativeButton("إلغاء", null).show();
    }

    private void showDialog(Promotion existing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_promotion, null);
        EditText etName = v.findViewById(R.id.et_promo_name);
        EditText etCode = v.findViewById(R.id.et_promo_code);
        EditText etValue = v.findViewById(R.id.et_promo_value);
        EditText etMinAmount = v.findViewById(R.id.et_promo_min_amount);
        EditText etBuyQty = v.findViewById(R.id.et_promo_buy_qty);
        EditText etGetQty = v.findViewById(R.id.et_promo_get_qty);
        Spinner spType = v.findViewById(R.id.sp_promo_type);
        Button btnStartDate = v.findViewById(R.id.btn_promo_start_date);
        Button btnEndDate = v.findViewById(R.id.btn_promo_end_date);
        CheckBox cbActive = v.findViewById(R.id.cb_promo_active);

        String[] types = {"خصم نسبة مئوية", "خصم مبلغ ثابت", "اشتري X واحصل على Y"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        if (existing != null) {
            etName.setText(existing.getName());
            etCode.setText(existing.getCode());
            etValue.setText(String.valueOf(existing.getDiscountValue()));
            etMinAmount.setText(String.valueOf(existing.getMinAmount()));
            etBuyQty.setText(String.valueOf(existing.getBuyQty()));
            etGetQty.setText(String.valueOf(existing.getGetQty()));
            spType.setSelection(existing.getType() - 1);
            cbActive.setChecked(existing.isActive());
            calStart.setTimeInMillis(existing.getStartDate() > 0 ? existing.getStartDate() : System.currentTimeMillis());
            calEnd.setTimeInMillis(existing.getEndDate() > 0 ? existing.getEndDate() : System.currentTimeMillis());
        }
        btnStartDate.setText(sdf.format(calStart.getTime()));
        btnEndDate.setText(sdf.format(calEnd.getTime()));

        btnStartDate.setOnClickListener(btn -> new DatePickerDialog(this,
            (dp, y, m, d2) -> { calStart.set(y, m, d2); btnStartDate.setText(sdf.format(calStart.getTime())); },
            calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH)).show());

        btnEndDate.setOnClickListener(btn -> new DatePickerDialog(this,
            (dp, y, m, d2) -> { calEnd.set(y, m, d2); btnEndDate.setText(sdf.format(calEnd.getTime())); },
            calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH)).show());

        new AlertDialog.Builder(this)
            .setTitle(existing == null ? "إضافة عرض" : "تعديل عرض")
            .setView(v)
            .setPositiveButton("حفظ", (d2, w) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { Toast.makeText(this, "اسم العرض إلزامي", Toast.LENGTH_SHORT).show(); return; }
                Promotion p = existing != null ? existing : new Promotion();
                p.setName(name);
                p.setCode(etCode.getText().toString().trim());
                p.setType(spType.getSelectedItemPosition() + 1);
                p.setActive(cbActive.isChecked());
                p.setStartDate(calStart.getTimeInMillis());
                p.setEndDate(calEnd.getTimeInMillis());
                try { p.setDiscountValue(Double.parseDouble(etValue.getText().toString())); } catch (Exception ex) {}
                try { p.setMinAmount(Double.parseDouble(etMinAmount.getText().toString())); } catch (Exception ex) {}
                try { p.setBuyQty(Integer.parseInt(etBuyQty.getText().toString())); } catch (Exception ex) {}
                try { p.setGetQty(Integer.parseInt(etGetQty.getText().toString())); } catch (Exception ex) {}
                vm.save(p);
            }).setNegativeButton("إلغاء", null).show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
