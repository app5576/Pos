package com.posmix.mixtuvgag.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.InvoicesAdapter; // Using InvoicesAdapter
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.*;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.utils.InvoicePdfHelper;
import com.posmix.mixtuvgag.utils.EnhancedPrintHelper; // Using EnhancedPrintHelper for printing

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class ReportsDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_TYPE = "report_type";
    public static final String EXTRA_FROM_DATE = "from_date";
    public static final String EXTRA_TO_DATE = "to_date";
    public static final String EXTRA_REPORT_TITLE = "report_title";

    private String reportType;
    private String reportTitle;
    private RecyclerView rvData;
    private InvoicesAdapter invoicesAdapter; // Changed to InvoicesAdapter
    private TextView tvTitle;
    private AppDatabase db;
    
    // For date filtering
    private Button btnDateFilter;
    private TextView tvDateRangeDisplay;
    private Calendar currentSelectedDate = Calendar.getInstance();
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US); // Use US locale for digits

    private List<Invoice> displayedInvoices = new ArrayList<>(); // To hold the actual invoice objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("تفاصيل التقرير");
        }

        reportType = getIntent().getStringExtra(EXTRA_REPORT_TYPE);
        // We will ignore initial from/toDate from intent for "cash_sales_invoices_list"
        // and default to today, allowing user to filter.
        reportTitle = getIntent().getStringExtra(EXTRA_REPORT_TITLE);
        
        if (reportTitle == null) reportTitle = getDefaultTitle();

        db = AppDatabase.getInstance(this);

        tvTitle = findViewById(R.id.tv_report_title);
        tvTitle.setText(reportTitle);

        rvData = findViewById(R.id.rv_report_data);
        rvData.setLayoutManager(new LinearLayoutManager(this));
        rvData.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize adapter with click listener
        invoicesAdapter = new InvoicesAdapter(this::showInvoiceOptionsDialog);
        rvData.setAdapter(invoicesAdapter);
        
        // Date filter UI setup
        btnDateFilter = findViewById(R.id.btn_date_filter);
        tvDateRangeDisplay = findViewById(R.id.tv_date_range_display);
        
        // Set default filter to today's date
        currentSelectedDate.setTimeInMillis(System.currentTimeMillis());
        updateDateDisplay();
        
        btnDateFilter.setOnClickListener(v -> showDatePicker());

        // Load report data based on report type and initial date
        loadReportData();
    }

    private String getDefaultTitle() {
        switch (reportType) {
            case "sales_report": return "تقرير المبيعات";
            case "cash_sales_invoices_list": return "تقرير فواتير المبيعات النقدية";
            case "credit_sales_invoices": return "فواتير المبيعات الآجلة";
            case "sales_by_category": return "المبيعات حسب الفئة";
            case "purchases_invoices": return "فواتير المشتريات";
            case "store_inventory": return "مخزون المتجر";
            case "fund_movement": return "حركة الصندوق";
            case "expenses_report": return "المصروفات";
            default: return "تقرير";
        }
    }

    private void updateDateDisplay() {
        tvDateRangeDisplay.setText("تاريخ العرض: " + displayDateFormat.format(currentSelectedDate.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, monthOfYear, dayOfMonth) -> {
                currentSelectedDate.set(year, monthOfYear, dayOfMonth);
                updateDateDisplay();
                loadReportData(); // Reload data for the new date
            },
            currentSelectedDate.get(Calendar.YEAR),
            currentSelectedDate.get(Calendar.MONTH),
            currentSelectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadReportData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            displayedInvoices.clear();
            List<Invoice> fetchedInvoices = new ArrayList<>();
            
            long from = getStartOfDay(currentSelectedDate);
            long to = getEndOfDay(currentSelectedDate);

            if ("cash_sales_invoices_list".equals(reportType)) {
                List<Invoice> invoices = db.invoiceDao().getByTypeBetweenSync(Invoice.TYPE_SALE, from, to);
                if (invoices != null) {
                    for (Invoice inv : invoices) {
                        if (inv.getPaymentStatus() == Invoice.STATUS_CASH || inv.getPaymentStatus() == Invoice.STATUS_CARD) {
                            fetchedInvoices.add(inv);
                        }
                    }
                }
                
                runOnUiThread(() -> {
                    if (fetchedInvoices.isEmpty()) {
                        Toast.makeText(this, "لا توجد فواتير مبيعات نقدية في هذا التاريخ.", Toast.LENGTH_SHORT).show();
                        rvData.setVisibility(View.GONE);
                    } else {
                        rvData.setVisibility(View.VISIBLE);
                    }
                    displayedInvoices.addAll(fetchedInvoices);
                    invoicesAdapter.submitList(displayedInvoices);
                });
            } else {
                // Keep the old behavior for other report types if they exist
                // For now, only 'cash_sales_invoices_list' is handled this way.
                runOnUiThread(() -> {
                    Toast.makeText(this, "هذا التقرير لا يدعم عرض القائمة حالياً.", Toast.LENGTH_SHORT).show();
                    // Optionally hide RecyclerView and show a message for unsupported types
                    rvData.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share_pdf) {
            generateAndSharePdf();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateAndSharePdf() {
        try {
            int pageWidth = 595;
            int pageHeight = 842;
            int margin = 40;
            int y = margin + 30;
            int lineHeight = 18;
            
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(10);
            paint.setColor(getResources().getColor(R.color.text_primary));

            // Report title
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText(reportTitle, margin, y, paint);
            y += lineHeight * 2;
            
            // Date range in PDF
            paint.setTextSize(10);
            String dateRangeText = "تاريخ العرض: " + displayDateFormat.format(currentSelectedDate.getTime());
            canvas.drawText(dateRangeText, margin, y, paint);
            y += lineHeight * 2;


            // Data
            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            
            // Table Headers for Invoices
            if (!displayedInvoices.isEmpty() && "cash_sales_invoices_list".equals(reportType)) {
                String[] headers = {"رقم الفاتورة", "التاريخ", "العميل", "الإجمالي", "المدفوع", "المتبقي", "الحالة"};
                int[] widths = {80, 80, 100, 70, 70, 70, 60}; // Adjust widths as needed
                y = drawTableHeader(canvas, headers, margin, y, paint, widths);
                drawHorizontalLine(canvas, margin, y, pageWidth - margin, paint);
                y += 5;

                for (Invoice inv : displayedInvoices) {
                    if (y > pageHeight - margin) {
                        document.finishPage(page);
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = margin + 10;
                        canvas.drawText(reportTitle + " (تابع)", margin, y, paint);
                        y += lineHeight * 2;
                        y = drawTableHeader(canvas, headers, margin, y, paint, widths);
                        drawHorizontalLine(canvas, margin, y, pageWidth - margin, paint);
                        y += 5;
                    }
                    
                    String customerName = "عميل نقدي";
                    if (inv.getCustomerId() != null) {
                        Customer c = db.customerDao().getByIdSync(inv.getCustomerId());
                        if (c != null) customerName = c.getName();
                    }
                    String status;
                    if (inv.getPaymentStatus() == Invoice.STATUS_CASH) status = "نقدي";
                    else if (inv.getPaymentStatus() == Invoice.STATUS_CARD) status = "بطاقة";
                    else status = "N/A"; // Should not happen with this filter

                    String[] rowData = {
                        inv.getInvoiceNumber(),
                        displayDateFormat.format(new Date(inv.getDate())),
                        customerName,
                        CurrencyHelper.format(inv.getTotal()),
                        CurrencyHelper.format(inv.getPaidAmount()),
                        CurrencyHelper.format(inv.getRemainingAmount()),
                        status
                    };
                    y = drawTableRow(canvas, rowData, margin, y, paint, lineHeight, widths);
                }
            } else {
                // Fallback for other report types if they are not converted to List<Invoice>
                canvas.drawText("لا توجد بيانات متاحة لإنشاء PDF لهذا التقرير.", margin, y, paint);
            }

            document.finishPage(page);

            String fileName = "report_" + reportType + "_" + System.currentTimeMillis() + ".pdf";
            File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir == null) {
                runOnUiThread(() -> Toast.makeText(this, "خطأ في الوصول لمجلد التنزيلات", Toast.LENGTH_SHORT).show());
                return;
            }
            File file = new File(downloadsDir, fileName);
            
            document.writeTo(new FileOutputStream(file));
            document.close();

            sharePdf(file);

        } catch (Exception e) {
            Log.e("PDF_ERROR", "Error generating PDF", e);
            Toast.makeText(this, "خطأ في إنشاء PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdf(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "مشاركة التقرير"));
    }

    // Invoice options dialog (copied from ReportsActivity)
    private void showInvoiceOptionsDialog(Invoice invoice) {
        String[] options = {"📝 تعديل الفاتورة", "🖨️ طباعة الفاتورة", "📄 تصدير PDF", "📋 عرض التفاصيل"};
        
        new AlertDialog.Builder(this)
            .setTitle("خيارات الفاتورة: " + invoice.getInvoiceNumber())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openEditInvoiceActivity(invoice);
                        break;
                    case 1:
                        printInvoice(invoice);
                        break;
                    case 2:
                        exportInvoiceToPdf(invoice);
                        break;
                    case 3:
                        showInvoiceDetails(invoice);
                        break;
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    // Helper methods for invoice actions (copied from ReportsActivity)
    private void openEditInvoiceActivity(Invoice invoice) {
        Intent intent = new Intent(this, EditInvoiceActivity.class);
        intent.putExtra("invoice_id", invoice.getId());
        // Pass other invoice data if needed for EditInvoiceActivity initial setup
        startActivity(intent);
    }
    
    private void printInvoice(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
            
            String customerName = "عميل نقدي";
            if (invoice.getCustomerId() != null) {
                Customer customer = db.customerDao().getByIdSync(invoice.getCustomerId());
                if (customer != null) customerName = customer.getName();
            }
            
            String invoiceText = EnhancedPrintHelper.buildInvoiceText(
                invoice, items, customerName, "MicroPOS", 
                EnhancedPrintHelper.PrinterSize.SIZE_58MM);
            
            runOnUiThread(() -> {
                Intent intent = new Intent(this, PrintActivity.class);
                intent.putExtra(PrintActivity.EXTRA_INVOICE_ID, invoice.getId());
                intent.putExtra(PrintActivity.EXTRA_INVOICE_TEXT, invoiceText);
                startActivity(intent);
            });
        });
    }
    
    private void exportInvoiceToPdf(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
                
                if (items == null) items = new ArrayList<>();
                
                File pdfFile = InvoicePdfHelper.generateThermalPdf(this, invoice, items);
                
                runOnUiThread(() -> {
                    if (pdfFile != null && pdfFile.exists()) {
                        Toast.makeText(this, "تم إنشاء PDF: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
                        
                        Uri pdfUri = FileProvider.getUriForFile(this, 
                            getApplicationContext().getPackageName() + ".fileprovider", pdfFile);
                        
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/pdf");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "مشاركة الفاتورة"));
                    } else {
                        Toast.makeText(this, "خطأ في إنشاء ملف PDF", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("ReportsDetailActivity", "Error exporting PDF", e);
                runOnUiThread(() -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void showInvoiceDetails(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
            
            StringBuilder details = new StringBuilder();
            details.append("📋 تفاصيل الفاتورة\n\n");
            details.append("رقم الفاتورة: ").append(invoice.getInvoiceNumber()).append("\n");
            details.append("التاريخ: ").append(displayDateFormat.format(new Date(invoice.getDate()))).append("\n"); // Use displayDateFormat
            
            String customerName = "عميل نقدي";
            if (invoice.getCustomerId() != null) {
                Customer customer = db.customerDao().getByIdSync(invoice.getCustomerId());
                if (customer != null) customerName = customer.getName();
            }
            details.append("العميل: ").append(customerName).append("\n");
            details.append("\n--- الأصناف ---\n");
            
            if (items != null) {
                for (InvoiceItem item : items) {
                    details.append("• ").append(item.getProductName())
                           .append(" | ").append(String.format(Locale.US, "%d", (int)item.getQuantity())) // English digits
                           .append(" × ").append(String.format(Locale.US, "%.2f", item.getUnitPrice())) // English digits
                           .append(" = ").append(String.format(Locale.US, "%.2f", item.getTotalPrice())).append("\n"); // English digits
                }
            }
            
            details.append("\n--- الملخص ---\n");
            details.append("المجموع: ").append(String.format(Locale.US, "%.2f", invoice.getSubtotal())).append("\n"); // English digits
            details.append("الخصم: ").append(String.format(Locale.US, "%.2f", invoice.getDiscount())).append("\n"); // English digits
            details.append("الإجمالي: ").append(String.format(Locale.US, "%.2f", invoice.getTotal())).append("\n"); // English digits
            details.append("المدفوع: ").append(String.format(Locale.US, "%.2f", invoice.getPaidAmount())).append("\n"); // English digits
            details.append("المتبقي: ").append(String.format(Locale.US, "%.2f", invoice.getRemainingAmount())).append("\n"); // English digits
            
            String status;
            switch (invoice.getPaymentStatus()) {
                case Invoice.STATUS_CASH: status = "نقدي"; break;
                case Invoice.STATUS_CREDIT: status = "آجل"; break;
                case Invoice.STATUS_PARTIAL: status = "جزئي"; break;
                case Invoice.STATUS_CARD: status = "بطاقة"; break;
                default: status = "غير معروف";
            }
            details.append("الحالة: ").append(status).append("\n");
            
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                    .setTitle("تفاصيل الفاتورة")
                    .setMessage(details.toString())
                    .setPositiveButton("موافق", null)
                    .setNegativeButton("تعديل", (d, w) -> openEditInvoiceActivity(invoice))
                    .show();
            });
        });
    }

    private long getStartOfDay(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
    
    private long getEndOfDay(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }
    
    private int drawTableHeader(Canvas canvas, String[] headers, int startX, int startY, Paint paint, int[] widths) {
        int currentX = startX;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], currentX, startY, paint);
            currentX += widths[i];
        }
        return startY + (int) paint.getTextSize() + 5;
    }

    private int drawTableRow(Canvas canvas, String[] rowData, int startX, int startY, Paint paint, int lineHeight, int[] widths) {
        int currentX = startX;
        for (int i = 0; i < rowData.length; i++) {
            canvas.drawText(rowData[i], currentX, startY, paint);
            currentX += widths[i];
        }
        return startY + lineHeight;
    }

    private void drawHorizontalLine(Canvas canvas, int startX, int startY, int endX, Paint paint) {
        canvas.drawLine(startX, startY, endX, startY, paint);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}