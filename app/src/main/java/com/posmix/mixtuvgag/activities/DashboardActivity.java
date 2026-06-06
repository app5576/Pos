package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.LowStockAdapter;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.viewmodels.DashboardViewModel;

public class DashboardActivity extends AppCompatActivity {
    private DashboardViewModel vm;
    private TextView tvTodaySales, tvTodayInvoices, tvMonthSales, tvPendingDebts;
    private TextView tvTotalProducts, tvLowStock, tvTotalCustomers, tvShiftStatus;
    private RecyclerView rvLowStock;
    private LowStockAdapter lowStockAdapter;
    private MaterialCardView cardShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        tvTodaySales = findViewById(R.id.tv_today_sales);
        tvTodayInvoices = findViewById(R.id.tv_today_invoices);
        tvMonthSales = findViewById(R.id.tv_month_sales);
        tvPendingDebts = findViewById(R.id.tv_pending_debts);
        tvTotalProducts = findViewById(R.id.tv_total_products);
        tvLowStock = findViewById(R.id.tv_low_stock);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);
        tvShiftStatus = findViewById(R.id.tv_shift_status);
        cardShift = findViewById(R.id.card_shift);
        rvLowStock = findViewById(R.id.rv_low_stock);

        rvLowStock.setLayoutManager(new LinearLayoutManager(this));
        lowStockAdapter = new LowStockAdapter();
        rvLowStock.setAdapter(lowStockAdapter);

        vm.getDashboardData().observe(this, data -> {
            if (data == null) return;
            tvTodaySales.setText(CurrencyHelper.format(data.todaySales));
            tvTodayInvoices.setText(String.valueOf(data.todayInvoiceCount));
            tvMonthSales.setText(CurrencyHelper.format(data.monthSales));
            tvPendingDebts.setText(CurrencyHelper.format(data.pendingDebts));
            tvTotalProducts.setText(String.valueOf(data.totalProducts));
            tvLowStock.setText(String.valueOf(data.lowStockProducts));
            tvTotalCustomers.setText(String.valueOf(data.totalCustomers));
            if (data.openShift != null) {
                tvShiftStatus.setText("وردية مفتوحة - " + (data.openShift.getEmployeeName() != null ? data.openShift.getEmployeeName() : ""));
                cardShift.setCardBackgroundColor(getColor(android.R.color.holo_green_light));
            } else {
                tvShiftStatus.setText("لا توجد وردية مفتوحة");
                cardShift.setCardBackgroundColor(getColor(android.R.color.holo_orange_light));
            }
            if (data.lowStockList != null) lowStockAdapter.submitList(data.lowStockList);
        });

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.load();
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_go_sales).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btn_go_inventory).setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        findViewById(R.id.btn_go_customers).setOnClickListener(v -> startActivity(new Intent(this, CustomersActivity.class)));
        findViewById(R.id.btn_go_reports).setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        findViewById(R.id.btn_go_expenses).setOnClickListener(v -> startActivity(new Intent(this, ExpensesActivity.class)));
        findViewById(R.id.btn_go_purchases).setOnClickListener(v -> startActivity(new Intent(this, PurchasesActivity.class)));
        findViewById(R.id.btn_go_shifts).setOnClickListener(v -> startActivity(new Intent(this, ShiftManagementActivity.class)));
        findViewById(R.id.btn_go_promotions).setOnClickListener(v -> startActivity(new Intent(this, PromotionsActivity.class)));
        findViewById(R.id.btn_go_returns).setOnClickListener(v -> startActivity(new Intent(this, ReturnInvoiceActivity.class)));
        findViewById(R.id.btn_go_employees).setOnClickListener(v -> startActivity(new Intent(this, EmployeesActivity.class)));
        cardShift.setOnClickListener(v -> startActivity(new Intent(this, ShiftManagementActivity.class)));
    }
}
