package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.CashTransactionsAdapter;
import com.posmix.mixtuvgag.databinding.ActivityCashFundBinding;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.CashTransaction;
import com.posmix.mixtuvgag.viewmodels.CashFundViewModel;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
public class CashFundActivity extends AppCompatActivity {

    private ActivityCashFundBinding binding;
    private CashFundViewModel vm;
    private TextView tvBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCashFundBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("الصندوق");
        }

        vm = new ViewModelProvider(this).get(CashFundViewModel.class);

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        CashTransactionsAdapter adapter = new CashTransactionsAdapter();
        binding.rvTransactions.setAdapter(adapter);

        vm.getTransactions().observe(this, list -> {
            adapter.submitList(list);
            refreshBalance();
        });

        // زر الإيداع
        binding.btnDeposit.setOnClickListener(v -> showTransactionDialog(CashTransaction.TYPE_IN));

        // زر السحب (إن وجد في الـ layout)
        View btnWithdraw = binding.getRoot().findViewById(R.id.btn_withdraw);
        if (btnWithdraw != null)
            btnWithdraw.setOnClickListener(v -> showTransactionDialog(CashTransaction.TYPE_OUT));
    }

    private void showTransactionDialog(int type) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cash_transaction, null);
        EditText etAmount = view.findViewById(R.id.et_transaction_amount);
        EditText etDesc   = view.findViewById(R.id.et_transaction_desc);

        String title = (type == CashTransaction.TYPE_IN) ? "إيداع في الصندوق" : "سحب من الصندوق";

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton("تأكيد", (d, w) -> {
                String amountStr = etAmount.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    CashTransaction t = new CashTransaction();
                    t.setType(type);
                    t.setAmount(Double.parseDouble(amountStr));
                    t.setDescription(etDesc.getText().toString().trim().isEmpty()
                        ? (type == CashTransaction.TYPE_IN ? "إيداع" : "سحب")
                        : etDesc.getText().toString().trim());
                    t.setDate(System.currentTimeMillis());
                    vm.insert(t);
                    Toast.makeText(this, "تمت العملية بنجاح", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "مبلغ غير صحيح", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void refreshBalance() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            double balance = db.cashTransactionDao().getCurrentBalance();
            runOnUiThread(() -> {
                View tvBal = binding.getRoot().findViewById(R.id.tv_cash_balance);
                if (tvBal instanceof TextView)
                    ((TextView) tvBal).setText(String.format("رصيد الصندوق: %.2f ر.س", balance));
            });
        });
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
