package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.fragments.SalesFragment;
import com.posmix.mixtuvgag.utils.SessionManager;

import android.view.View;
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.syncProgressBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        sessionManager = new SessionManager(this);

        // تحميل الـ Fragment الافتراضي (المبيعات)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SalesFragment())
                    .commit();
        }

        // إعداد قائمة التنقل السفلية
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_sales) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SalesFragment())
                            .commit();
                    toolbar.setTitle("المبيعات");
                    return true;
                } else if (itemId == R.id.navigation_inventory) {
                    startActivity(new Intent(MainActivity.this, InventoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_customers) {
                    startActivity(new Intent(MainActivity.this, CustomersActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_reports) {
                    startActivity(new Intent(MainActivity.this, ReportsActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_more) {
                    showMoreOptionsDialog();
                    return true;
                }
                return false;
            }
        });
    }

    private void showMoreOptionsDialog() {
        // عرض حوار (AlertDialog) يحتوي على خيارات "المزيد"
        String[] options = {
            "الموردون", "المشتريات", "الصندوق", "المصاريف", "الإعدادات", "مزامنة", "تسجيل الخروج"
        };

        new AlertDialog.Builder(this)
                .setTitle("خيارات إضافية")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // الموردون
                            startActivity(new Intent(MainActivity.this, SuppliersActivity.class));
                            break;
                        case 1: // المشتريات
                            startActivity(new Intent(MainActivity.this, PurchasesActivity.class));
                            break;
                        case 2: // الصندوق
                            startActivity(new Intent(MainActivity.this, CashFundActivity.class));
                            break;
                        case 3: // المصاريف
                            startActivity(new Intent(MainActivity.this, ExpensesActivity.class));
                            break;
                        case 4: // الإعدادات
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            break;
                        case 5: // مزامنة
                            Toast.makeText(MainActivity.this, "جاري المزامنة...", Toast.LENGTH_SHORT).show();
                            // يمكنك إضافة كود المزامنة هنا لاحقاً
                            break;
                        case 6: // تسجيل الخروج
                            sessionManager.logout();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                            break;
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        // إذا كان الـ Fragment الحالي هو SalesFragment، أغلق التطبيق، وإلا ارجع للخلف
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SalesFragment) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SalesFragment())
                    .commit();
            toolbar.setTitle("المبيعات");
            bottomNavigationView.setSelectedItemId(R.id.navigation_sales);
        }
    }
}
