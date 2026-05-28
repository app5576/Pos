package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.utils.PrintHelper;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
public class PrintActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = "invoice_id";
    public static final String EXTRA_INVOICE_TEXT = "invoice_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("معاينة الفاتورة");
        }

        TextView tvInvoice = findViewById(R.id.tv_invoice_preview);
        Button btnShare   = findViewById(R.id.btn_share_invoice);
        Button btnPrint   = findViewById(R.id.btn_print_invoice);

        String invoiceText = getIntent().getStringExtra(EXTRA_INVOICE_TEXT);
        if (invoiceText == null || invoiceText.isEmpty()) {
            invoiceText = PrintHelper.buildSampleInvoice();
        }
        tvInvoice.setText(invoiceText);

        final String finalText = invoiceText;

        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, finalText);
            share.putExtra(Intent.EXTRA_SUBJECT, "فاتورة MicroPOS");
            startActivity(Intent.createChooser(share, "مشاركة الفاتورة عبر"));
        });

        btnPrint.setOnClickListener(v -> {
            // تفعيل طباعة عبر Bluetooth أو Wi-Fi في الإصدار القادم
            android.widget.Toast.makeText(this,
                "الطباعة المباشرة ستكون متاحة قريباً", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}

