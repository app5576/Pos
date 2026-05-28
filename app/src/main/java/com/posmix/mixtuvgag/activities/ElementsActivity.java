package com.posmix.mixtuvgag.activities;  

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.utils.SessionManager;
import com.posmix.mixtuvgag.utils.FullSyncManager;

public class ElementsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SessionManager sessionManager;
    private LinearLayout cardSales, cardPurchases, cardCustomers, cardSuppliers;
    private LinearLayout cardInventory, cardExpenses, cardCashbox, cardReports, cardSettings;
    private LinearLayout syncLayout;
    private ProgressBar syncProgress;
    private ImageView syncIcon;
    private TextView syncStatus;
    private FullSyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elements);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) { navigateToLogin(); return; }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeViews();
        setupClickListeners();

        syncLayout = findViewById(R.id.sync_layout);
        syncProgress = findViewById(R.id.sync_progress);
        syncIcon = findViewById(R.id.sync_icon);
        syncStatus = findViewById(R.id.sync_status);
        Button btnSyncNow = findViewById(R.id.btn_sync_now);

        syncManager = new FullSyncManager(this);
        syncStatus.setText(syncManager.getLastSyncTime());
        btnSyncNow.setOnClickListener(v -> startSyncOperation());
    }

    private void initializeViews() {
        cardSales = findViewById(R.id.card_sales);
        cardPurchases = findViewById(R.id.card_purchases);
        cardCustomers = findViewById(R.id.card_customers);
        cardSuppliers = findViewById(R.id.card_suppliers);
        cardInventory = findViewById(R.id.card_inventory);
        cardExpenses = findViewById(R.id.card_expenses);
        cardCashbox = findViewById(R.id.card_cashbox);
        cardReports = findViewById(R.id.card_reports);
        cardSettings = findViewById(R.id.card_settings);
    }

    private void setupClickListeners() {
        cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesActivity.class)));
        cardInventory.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        cardCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomersActivity.class)));
        cardSuppliers.setOnClickListener(v -> startActivity(new Intent(this, SuppliersActivity.class)));
        cardPurchases.setOnClickListener(v -> startActivity(new Intent(this, PurchasesActivity.class)));
        cardExpenses.setOnClickListener(v -> startActivity(new Intent(this, ExpensesActivity.class)));
        cardCashbox.setOnClickListener(v -> startActivity(new Intent(this, CashFundActivity.class)));
        cardReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void startSyncOperation() {
        setUiEnabled(false);
        syncManager.syncAllData(new FullSyncManager.SyncCallback() {
            @Override
            public void onSyncStart() {
                runOnUiThread(() -> syncStatus.setText("جاري البدء..."));
            }

            @Override
            public void onSyncProgress(String message) {
                runOnUiThread(() -> syncStatus.setText(message));
            }

            @Override
            public void onSyncComplete(String message) {
                runOnUiThread(() -> {
                    syncStatus.setText("تم التزامن");
                    Toast.makeText(ElementsActivity.this, message, Toast.LENGTH_SHORT).show();
                    setUiEnabled(true);
                });
            }

            @Override
            public void onSyncError(String error) {
                runOnUiThread(() -> {
                    syncStatus.setText("خطأ");
                    Toast.makeText(ElementsActivity.this, "فشل: " + error, Toast.LENGTH_LONG).show();
                    setUiEnabled(true);
                });
            }
        });
    }

    private void setUiEnabled(boolean enabled) {
        syncProgress.setVisibility(enabled ? View.GONE : View.VISIBLE);
        syncIcon.setVisibility(enabled ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_sync_now).setEnabled(enabled);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.main_menu, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) { sessionManager.logout(); navigateToLogin(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
