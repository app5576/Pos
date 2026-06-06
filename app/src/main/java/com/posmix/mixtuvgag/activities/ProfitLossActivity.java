package com.posmix.mixtuvgag.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.database.DatabaseHelper;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class ProfitLossActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private Calendar calFrom = Calendar.getInstance(), calTo = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ar"));
    private TextView etFromDate, etToDate;
    private TextView tvTotalSales, tvTotalPurchases, tvGrossProfit, tvTotalExpenses;
    private TextView tvTotalReturns, tvNetProfit, tvGrossMargin, tvNetMargin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_loss);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("قائمة الأرباح والخسائر");
        }
        db = new DatabaseHelper(this);
        calFrom.set(Calendar.DAY_OF_MONTH, 1);

        etFromDate = findViewById(R.id.tv_pl_from_date);
        etToDate = findViewById(R.id.tv_pl_to_date);
        tvTotalSales = findViewById(R.id.tv_pl_total_sales);
        tvTotalPurchases = findViewById(R.id.tv_pl_total_purchases);
        tvGrossProfit = findViewById(R.id.tv_pl_gross_profit);
        tvTotalExpenses = findViewById(R.id.tv_pl_total_expenses);
        tvTotalReturns = findViewById(R.id.tv_pl_total_returns);
        tvNetProfit = findViewById(R.id.tv_pl_net_profit);
        tvGrossMargin = findViewById(R.id.tv_pl_gross_margin);
        tvNetMargin = findViewById(R.id.tv_pl_net_margin);

        etFromDate.setText(sdf.format(calFrom.getTime()));
        etToDate.setText(sdf.format(calTo.getTime()));

        etFromDate.setOnClickListener(v -> new DatePickerDialog(this,
            (dp, y, m, d) -> { calFrom.set(y, m, d); etFromDate.setText(sdf.format(calFrom.getTime())); loadData(); },
            calFrom.get(Calendar.YEAR), calFrom.get(Calendar.MONTH), calFrom.get(Calendar.DAY_OF_MONTH)).show());

        etToDate.setOnClickListener(v -> new DatePickerDialog(this,
            (dp, y, m, d) -> { calTo.set(y, m, d); etToDate.setText(sdf.format(calTo.getTime())); loadData(); },
            calTo.get(Calendar.YEAR), calTo.get(Calendar.MONTH), calTo.get(Calendar.DAY_OF_MONTH)).show());

        MaterialButton btnRefresh = findViewById(R.id.btn_pl_refresh);
        btnRefresh.setOnClickListener(v -> loadData());
        loadData();
    }

    private void loadData() {
        calFrom.set(Calendar.HOUR_OF_DAY, 0); calFrom.set(Calendar.MINUTE, 0); calFrom.set(Calendar.SECOND, 0);
        calTo.set(Calendar.HOUR_OF_DAY, 23); calTo.set(Calendar.MINUTE, 59); calTo.set(Calendar.SECOND, 59);
        long from = calFrom.getTimeInMillis(), to = calTo.getTimeInMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            double sales = db.getInvoiceTotalByDateRange(from, to, 1);
            double purchases = db.getInvoiceTotalByDateRange(from, to, 2);
            double expenses = db.getExpensesTotalByDateRange(from, to);
            double returns = db.getReturnInvoiceDao().getTotalReturnsByDateRange(from, to);
            double grossProfit = sales - purchases;
            double netProfit = grossProfit - expenses - returns;
            double grossMargin = sales > 0 ? (grossProfit / sales) * 100 : 0;
            double netMargin = sales > 0 ? (netProfit / sales) * 100 : 0;
            runOnUiThread(() -> {
                tvTotalSales.setText(CurrencyHelper.format(sales));
                tvTotalPurchases.setText(CurrencyHelper.format(purchases));
                tvGrossProfit.setText(CurrencyHelper.format(grossProfit));
                tvTotalExpenses.setText(CurrencyHelper.format(expenses));
                tvTotalReturns.setText(CurrencyHelper.format(returns));
                tvNetProfit.setText(CurrencyHelper.format(netProfit));
                tvGrossMargin.setText(String.format(Locale.getDefault(), "%.1f%%", grossMargin));
                tvNetMargin.setText(String.format(Locale.getDefault(), "%.1f%%", netMargin));
                if (netProfit >= 0) tvNetProfit.setTextColor(getColor(android.R.color.holo_green_dark));
                else tvNetProfit.setTextColor(getColor(android.R.color.holo_red_dark));
            });
        });
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
