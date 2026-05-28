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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.*;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.utils.InvoicePdfHelper;
import com.posmix.mixtuvgag.utils.PrintHelper;
import com.posmix.mixtuvgag.utils.EnhancedPrintHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class ReportsActivity extends AppCompatActivity {

    private Calendar calFrom = Calendar.getInstance();
    private Calendar calTo = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ar"));

    private TextInputEditText etFromDate;
    private TextInputEditText etToDate;
    private MaterialButton btnApplyFilter;
    private MaterialButton btnPrintReport;
    private ImageButton btnRefresh;

    private AppDatabase db;
    private static final int MARGIN_SIDE = 40;
    private static final int MARGIN_TOP = 40;
    private static final int MARGIN_BOTTOM = 50;

    // أزرار/صفوف التقارير
    private LinearLayout btnReportSales, btnReportCashSalesInvoice, btnReportDroppedSalesInvoice;
    private LinearLayout btnReportTotalSalesByCategory, btnReportTotalTaxByCategory;
    private LinearLayout btnReportCustomerAccountStatement, btnReportCustomerInvoices, btnReportCustomerPaymentsMovement;
    private LinearLayout btnReportPurchasesInvoiceCash, btnReportSupplierAccountStatement;
    private LinearLayout btnReportSupplierInvoices, btnReportSupplierPaymentsMovement;
    private LinearLayout btnReportStoreInventory, btnReportFundMovement;
    private LinearLayout btnReportExpenses, btnReportExpensesByCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("التقارير");
            }
        }

        calFrom.set(Calendar.DAY_OF_MONTH, 1);

        initViews();
        setupClickListeners();
        updateDateDisplay();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        etFromDate = findViewById(R.id.et_from_date);
        etToDate = findViewById(R.id.et_to_date);
        btnApplyFilter = findViewById(R.id.btn_apply_filter);
        btnPrintReport = findViewById(R.id.btn_print_report);
        btnRefresh = findViewById(R.id.btn_refresh);

        btnReportSales = findViewById(R.id.btn_report_sales);
        btnReportCashSalesInvoice = findViewById(R.id.btn_report_cash_sales_invoice);
        btnReportDroppedSalesInvoice = findViewById(R.id.btn_report_dropped_sales_invoice);
        btnReportTotalSalesByCategory = findViewById(R.id.btn_report_total_sales_by_category);
        btnReportTotalTaxByCategory = findViewById(R.id.btn_report_total_tax_by_category);

        btnReportCustomerAccountStatement = findViewById(R.id.btn_report_customer_account_statement);
        btnReportCustomerInvoices = findViewById(R.id.btn_report_customer_invoices);
        btnReportCustomerPaymentsMovement = findViewById(R.id.btn_report_customer_payments_movement);

        btnReportPurchasesInvoiceCash = findViewById(R.id.btn_report_purchases_invoice_cash);
        btnReportSupplierAccountStatement = findViewById(R.id.btn_report_supplier_account_statement);
        btnReportSupplierInvoices = findViewById(R.id.btn_report_supplier_invoices);
        btnReportSupplierPaymentsMovement = findViewById(R.id.btn_report_supplier_payments_movement);

        btnReportStoreInventory = findViewById(R.id.btn_report_store_inventory);
        btnReportFundMovement = findViewById(R.id.btn_report_fund_movement);
        btnReportExpenses = findViewById(R.id.btn_report_expenses);
        btnReportExpensesByCategory = findViewById(R.id.btn_report_expenses_by_category);
    }

    private void setupClickListeners() {

        // فلتر التاريخ
        etFromDate.setOnClickListener(v -> showFromDatePicker());
        etToDate.setOnClickListener(v -> showToDatePicker());
        btnApplyFilter.setOnClickListener(v -> {
            long from = getStartOfDay(calFrom);
            long to = getEndOfDay(calTo);
            if (from > to) {
                Toast.makeText(this, "تاريخ البداية يجب أن يكون قبل تاريخ النهاية", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "تم تطبيق الفلترة بنجاح", Toast.LENGTH_SHORT).show();
        });

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                calFrom = Calendar.getInstance();
                calFrom.set(Calendar.DAY_OF_MONTH, 1);
                calTo = Calendar.getInstance();
                updateDateDisplay();
                Toast.makeText(this, "تم تحديث التقارير", Toast.LENGTH_SHORT).show();
            });
        }

        btnPrintReport.setOnClickListener(v ->
                Toast.makeText(this, "اختر تقريراً محدداً للطباعة.", Toast.LENGTH_SHORT).show());

        // تقارير المبيعات
        btnReportSales.setOnClickListener(v -> openReportDetail("sales_report", "تقرير المبيعات الشامل"));
        btnReportCashSalesInvoice.setOnClickListener(v -> openReportDetail("cash_sales_invoices_list", "تقرير فواتير المبيعات النقدية"));
        btnReportDroppedSalesInvoice.setOnClickListener(v -> openReportDetail("credit_sales_invoices", "تقرير فواتير المبيعات الآجلة"));
        btnReportTotalSalesByCategory.setOnClickListener(v -> generateReportPdf("sales_by_category", "تقرير إجمالي المبيعات حسب الفئة"));
        btnReportTotalTaxByCategory.setOnClickListener(v -> generateReportPdf("tax_by_category", "تقرير إجمالي الضرائب حسب الفئة"));

        // تقارير العملاء
        btnReportCustomerAccountStatement.setOnClickListener(v -> generateReportPdf("customer_account_statement", "كشف حساب العملاء"));
        btnReportCustomerInvoices.setOnClickListener(v -> generateReportPdf("customer_invoices", "تقرير فواتير العملاء"));
        btnReportCustomerPaymentsMovement.setOnClickListener(v -> generateReportPdf("customer_payments_movement", "تقرير حركة مدفوعات العملاء"));

        // تقارير المشتريات والموردين
        btnReportPurchasesInvoiceCash.setOnClickListener(v -> generateReportPdf("purchases_invoices", "تقرير فواتير المشتريات"));
        btnReportSupplierAccountStatement.setOnClickListener(v -> generateReportPdf("supplier_account_statement", "كشف حساب الموردين"));
        btnReportSupplierInvoices.setOnClickListener(v -> generateReportPdf("supplier_invoices", "تقرير فواتير الموردين"));
        btnReportSupplierPaymentsMovement.setOnClickListener(v -> generateReportPdf("supplier_payments_movement", "تقرير حركة مدفوعات الموردين"));

        // تقارير المخزون والخزينة
        btnReportStoreInventory.setOnClickListener(v -> openReportDetail("store_inventory", "تقرير مخزون المتجر"));
        btnReportFundMovement.setOnClickListener(v -> openReportDetail("fund_movement", "تقرير حركة الصندوق"));
        btnReportExpenses.setOnClickListener(v -> openReportDetail("expenses_report", "تقرير المصروفات"));
        btnReportExpensesByCategory.setOnClickListener(v -> generateReportPdf("expenses_by_category", "تقرير المصروفات حسب الفئة"));
    }

    private void showFromDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calFrom.set(year, month, day);
            updateDateDisplay();
        }, calFrom.get(Calendar.YEAR), calFrom.get(Calendar.MONTH), calFrom.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showToDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calTo.set(year, month, day);
            updateDateDisplay();
        }, calTo.get(Calendar.YEAR), calTo.get(Calendar.MONTH), calTo.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        if (etFromDate != null) etFromDate.setText(sdf.format(calFrom.getTime()));
        if (etToDate != null) etToDate.setText(sdf.format(calTo.getTime()));
    }

    private void showCashSalesInvoicesWithOptions() {
        long from = getStartOfDay(calFrom);
        long to = getEndOfDay(calTo);

        if (from > to) {
            Toast.makeText(this, "تاريخ البداية يجب أن يكون قبل تاريخ النهاية", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Invoice> invoices = db.invoiceDao().getByTypeBetweenSync(Invoice.TYPE_SALE, from, to);

            List<Invoice> cashInvoices = new ArrayList<>();
            for (Invoice inv : invoices) {
                if (inv.getPaymentStatus() == Invoice.STATUS_CASH || inv.getPaymentStatus() == Invoice.STATUS_CARD) {
                    cashInvoices.add(inv);
                }
            }

            if (cashInvoices.isEmpty()) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                        .setTitle("فواتير المبيعات النقدية")
                        .setMessage("لا توجد فواتير نقدية في هذه الفترة")
                        .setPositiveButton("موافق", null)
                        .show();
                });
                return;
            }

            String[] invoiceItems = new String[cashInvoices.size()];
            for (int i = 0; i < cashInvoices.size(); i++) {
                Invoice inv = cashInvoices.get(i);
                String customerName = "عميل نقدي";
                if (inv.getCustomerId() != null) {
                    Customer c = db.customerDao().getByIdSync(inv.getCustomerId());
                    if (c != null) customerName = c.getName();
                }
                invoiceItems[i] = inv.getInvoiceNumber() + " | " +
                    sdf.format(new Date(inv.getDate())) + " | " +
                    CurrencyHelper.format(inv.getTotal()) + " | " + customerName;
            }

            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                    .setTitle("اختر فاتورة")
                    .setItems(invoiceItems, (dialog, which) -> {
                        Invoice selectedInvoice = cashInvoices.get(which);
                        showInvoiceOptionsDialog(selectedInvoice);
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            });
        });
    }

    private void showInvoiceOptionsDialog(Invoice invoice) {
        String[] options = {"📝 تعديل الفاتورة", "🖨️ طباعة الفاتورة", "📄 تصدير PDF", "📋 عرض التفاصيل"};

        new AlertDialog.Builder(this)
            .setTitle("خيارات الفاتورة: " + invoice.getInvoiceNumber())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: openEditInvoiceActivity(invoice); break;
                    case 1: printInvoice(invoice); break;
                    case 2: exportInvoiceToPdf(invoice); break;
                    case 3: showInvoiceDetails(invoice); break;
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void openEditInvoiceActivity(Invoice invoice) {
        Intent intent = new Intent(this, EditInvoiceActivity.class);
        intent.putExtra("invoice_id", invoice.getId());
        intent.putExtra("invoice_number", invoice.getInvoiceNumber());
        intent.putExtra("invoice_type", invoice.getType());
        intent.putExtra("invoice_total", invoice.getTotal());
        intent.putExtra("invoice_discount", invoice.getDiscount());
        intent.putExtra("invoice_paid", invoice.getPaidAmount());
        intent.putExtra("invoice_customer_id", invoice.getCustomerId() != null ? invoice.getCustomerId() : -1);
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
                Log.e("ReportsActivity", "Error exporting PDF", e);
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
            details.append("التاريخ: ").append(sdf.format(new Date(invoice.getDate()))).append("\n");

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
                           .append(" | ").append((int) item.getQuantity())
                           .append(" × ").append(String.format("%.2f", item.getUnitPrice()))
                           .append(" = ").append(String.format("%.2f", item.getTotalPrice())).append("\n");
                }
            }

            details.append("\n--- الملخص ---\n");
            details.append("المجموع: ").append(String.format("%.2f", invoice.getSubtotal())).append("\n");
            details.append("الخصم: ").append(String.format("%.2f", invoice.getDiscount())).append("\n");
            details.append("الإجمالي: ").append(String.format("%.2f", invoice.getTotal())).append("\n");
            details.append("المدفوع: ").append(String.format("%.2f", invoice.getPaidAmount())).append("\n");
            details.append("المتبقي: ").append(String.format("%.2f", invoice.getRemainingAmount())).append("\n");

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

    // ============ دوال مساعدة ============

    private void openReportDetail(String reportType, String reportTitle) {
        Intent intent = new Intent(this, ReportsDetailActivity.class);
        intent.putExtra(ReportsDetailActivity.EXTRA_REPORT_TYPE, reportType);
        intent.putExtra(ReportsDetailActivity.EXTRA_FROM_DATE, getStartOfDay(calFrom));
        intent.putExtra(ReportsDetailActivity.EXTRA_TO_DATE, getEndOfDay(calTo));
        intent.putExtra(ReportsDetailActivity.EXTRA_REPORT_TITLE, reportTitle);
        startActivity(intent);
    }

    private void sharePdf(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(
            this,
            getApplicationContext().getPackageName() + ".fileprovider",
            pdfFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "مشاركة التقرير"));
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

    // ============ دوال إنشاء التقارير ============

    private void generateReportPdf(String reportType, String reportTitle) {
        long from = getStartOfDay(calFrom);
        long to = getEndOfDay(calTo);

        if (from > to) {
            Toast.makeText(this, "تاريخ البداية يجب أن يكون قبل تاريخ النهاية", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "جاري إنشاء " + reportTitle + "...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);
            paint.setColor(getResources().getColor(R.color.text_primary));

            int y = MARGIN_TOP;
            int x = MARGIN_SIDE;
            int lineHeight = 18;

            y = drawPageHeader(canvas, reportTitle, from, to, x, y, paint, pageInfo.getPageWidth());
            y += lineHeight;

            try {
                switch (reportType) {
                    case "sales_by_category":
                        y = generateSalesByCategoryReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "tax_by_category":
                        y = generateTaxByCategoryReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "customer_account_statement":
                        y = generateCustomerAccountStatement(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "customer_invoices":
                        y = generateCustomerInvoicesReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "customer_payments_movement":
                        y = generateCustomerPaymentsMovementReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "purchases_invoices":
                        y = generateInvoicesListReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to, Invoice.TYPE_PURCHASE, -1, "فواتير المشتريات");
                        break;
                    case "supplier_account_statement":
                        y = generateSupplierAccountStatement(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "supplier_invoices":
                        y = generateSupplierInvoicesReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "supplier_payments_movement":
                        y = generateSupplierPaymentsMovementReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                    case "expenses_by_category":
                        y = generateExpensesByCategoryReport(document, pageInfo, page, canvas, x, y, paint, lineHeight, from, to);
                        break;
                }

                document.finishPage(page);

                String fileName = reportType + "_" + System.currentTimeMillis() + ".pdf";
                File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadsDir == null) {
                    runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "خطأ: لا يمكن الوصول إلى مجلد التنزيلات.", Toast.LENGTH_SHORT).show());
                    return;
                }
                File file = new File(downloadsDir, fileName);

                document.writeTo(new FileOutputStream(file));
                runOnUiThread(() -> {
                    Toast.makeText(ReportsActivity.this, "تم إنشاء التقرير: " + fileName, Toast.LENGTH_LONG).show();
                    sharePdf(file);
                });

            } catch (IOException e) {
                Log.e("PDF_GEN", "Error creating PDF", e);
                runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "خطأ في إنشاء ملف PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                document.close();
            }
        });
    }

    private int drawPageHeader(Canvas canvas, String title, long fromDate, long toDate, int x, int y, Paint paint, int pageWidth) {
        paint.setTextSize(18);
        canvas.drawText(title, x + (pageWidth - 2 * x - paint.measureText(title)) / 2, y, paint);
        y += paint.getTextSize() + 10;

        paint.setTextSize(10);
        String dateRangeText = String.format("الفترة: %s - %s", sdf.format(new Date(fromDate)), sdf.format(new Date(toDate)));
        canvas.drawText(dateRangeText, x, y, paint);
        y += paint.getTextSize() + 5;

        canvas.drawLine(x, y, pageWidth - x, y, paint);
        y += 10;
        return y;
    }

    private int generateSalesByCategoryReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Invoice> salesInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);
        Map<String, Double> salesByCategory = new HashMap<>();

        for (Invoice invoice : salesInvoices) {
            if (invoice.getDate() >= from && invoice.getDate() <= to) {
                List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
                for (InvoiceItem item : items) {
                    Product product = db.productDao().getById(item.getProductId());
                    if (product != null) {
                        String categoryName = product.getCategoryName() != null ? product.getCategoryName() : "غير مصنف";
                        salesByCategory.put(categoryName, salesByCategory.getOrDefault(categoryName, 0.0) + item.getTotalPrice());
                    }
                }
            }
        }

        if (salesByCategory.isEmpty()) {
            canvas.drawText("لا توجد مبيعات حسب الفئة لهذه الفترة.", x, y, paint);
            return y + lineHeight;
        }

        String[] headers = {"الفئة", "إجمالي المبيعات"};
        int[] widths = {250, 150};
        y = drawTableHeader(canvas, headers, x, y, paint, widths);
        drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
        y += 5;

        for (Map.Entry<String, Double> entry : salesByCategory.entrySet()) {
            if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                document.finishPage(currentPage);
                currentPage = document.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                y = drawPageHeader(canvas, "تقرير إجمالي المبيعات حسب الفئة (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                y = drawTableHeader(canvas, headers, x, y, paint, widths);
                drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                y += 5;
            }
            String[] rowData = {entry.getKey(), CurrencyHelper.format(entry.getValue())};
            y = drawTableRow(canvas, rowData, x, y, paint, lineHeight, widths);
        }
        return y;
    }

    private int generateTaxByCategoryReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Invoice> salesInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);
        Map<String, Double> taxByCategory = new HashMap<>();

        for (Invoice invoice : salesInvoices) {
            if (invoice.getDate() >= from && invoice.getDate() <= to) {
                List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
                for (InvoiceItem item : items) {
                    Product product = db.productDao().getById(item.getProductId());
                    if (product != null) {
                        String categoryName = product.getCategoryName() != null ? product.getCategoryName() : "غير مصنف";
                        double itemTax = item.getTotalPrice() * (item.getTaxPercentage() / (100 + item.getTaxPercentage()));
                        taxByCategory.put(categoryName, taxByCategory.getOrDefault(categoryName, 0.0) + itemTax);
                    }
                }
            }
        }

        if (taxByCategory.isEmpty()) {
            canvas.drawText("لا توجد ضرائب حسب الفئة لهذه الفترة.", x, y, paint);
            return y + lineHeight;
        }

        String[] headers = {"الفئة", "إجمالي الضريبة"};
        int[] widths = {250, 150};
        y = drawTableHeader(canvas, headers, x, y, paint, widths);
        drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
        y += 5;

        for (Map.Entry<String, Double> entry : taxByCategory.entrySet()) {
            if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                document.finishPage(currentPage);
                currentPage = document.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                y = drawPageHeader(canvas, "تقرير إجمالي الضرائب حسب الفئة (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                y = drawTableHeader(canvas, headers, x, y, paint, widths);
                drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                y += 5;
            }
            String[] rowData = {entry.getKey(), CurrencyHelper.format(entry.getValue())};
            y = drawTableRow(canvas, rowData, x, y, paint, lineHeight, widths);
        }
        return y;
    }

    private int generateInvoicesListReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to, int invoiceType, int paymentStatus, String title) {
        paint.setTextSize(10);
        List<Invoice> invoices = db.invoiceDao().getByTypeBetweenSync(invoiceType, from, to);

        List<Invoice> filteredInvoices = new ArrayList<>();
        if (invoices != null) {
            for (Invoice inv : invoices) {
                if (paymentStatus == -1 || inv.getPaymentStatus() == paymentStatus) {
                    filteredInvoices.add(inv);
                }
            }
        }

        if (filteredInvoices.isEmpty()) {
            canvas.drawText("لا توجد فواتير لعرضها في هذه الفترة.", x, y, paint);
            return y + lineHeight;
        }

        String[] headers = {"رقم الفاتورة", "التاريخ", "العميل/المورد", "الإجمالي", "المدفوع", "المتبقي", "الحالة"};
        int[] widths = {100, 100, 120, 70, 70, 70, 60};

        y = drawTableHeader(canvas, headers, x, y, paint, widths);
        drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
        y += 5;

        for (Invoice invoice : filteredInvoices) {
            if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                document.finishPage(currentPage);
                currentPage = document.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                y = drawPageHeader(canvas, title + " (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                y = drawTableHeader(canvas, headers, x, y, paint, widths);
                drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                y += 5;
            }

            String customerOrSupplierName = "N/A";
            if (invoice.getCustomerId() != null) {
                Customer customer = db.customerDao().getByIdSync(invoice.getCustomerId());
                if (customer != null) customerOrSupplierName = customer.getName();
            } else if (invoice.getSupplierId() != null) {
                Supplier supplier = db.supplierDao().getByIdSync(invoice.getSupplierId());
                if (supplier != null) customerOrSupplierName = supplier.getName();
            }

            String status = "";
            if (invoice.getPaymentStatus() == Invoice.STATUS_CASH) status = "نقدي";
            else if (invoice.getPaymentStatus() == Invoice.STATUS_CREDIT) status = "آجل";
            else if (invoice.getPaymentStatus() == Invoice.STATUS_PARTIAL) status = "جزئي";
            else if (invoice.getPaymentStatus() == Invoice.STATUS_CARD) status = "بطاقة";

            String[] rowData = {
                invoice.getInvoiceNumber(),
                sdf.format(new Date(invoice.getDate())),
                customerOrSupplierName,
                CurrencyHelper.format(invoice.getTotal()),
                CurrencyHelper.format(invoice.getPaidAmount()),
                CurrencyHelper.format(invoice.getRemainingAmount()),
                status
            };
            y = drawTableRow(canvas, rowData, x, y, paint, lineHeight, widths);
        }
        return y;
    }

    private int generateCustomerAccountStatement(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Customer> customers = db.customerDao().getAllSync();
        List<Invoice> allSalesInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);

        if (customers.isEmpty()) {
            canvas.drawText("لا يوجد عملاء لعرض كشوف حساباتهم.", x, y, paint);
            return y + lineHeight;
        }

        for (Customer customer : customers) {
            if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                document.finishPage(currentPage);
                currentPage = document.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                y = drawPageHeader(canvas, "كشف حساب العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
            }

            canvas.drawText("العميل: " + customer.getName(), x, y, paint);
            y += lineHeight;
            canvas.drawText("الرصيد الحالي: " + CurrencyHelper.format(customer.getCurrentBalance()), x, y, paint);
            y += lineHeight;

            List<Invoice> customerInvoices = new ArrayList<>();
            for (Invoice invoice : allSalesInvoices) {
                if (invoice.getCustomerId() != null && invoice.getCustomerId() == customer.getId() &&
                    invoice.getDate() >= from && invoice.getDate() <= to) {
                    customerInvoices.add(invoice);
                }
            }

            if (!customerInvoices.isEmpty()) {
                String[] headers = {"رقم الفاتورة", "التاريخ", "الإجمالي", "المدفوع", "المتبقي"};
                int[] widths = {100, 100, 70, 70, 70};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : customerInvoices) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "كشف حساب العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("العميل: " + customer.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }
                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getTotal()),
                        CurrencyHelper.format(invoice.getPaidAmount()),
                        CurrencyHelper.format(invoice.getRemainingAmount())
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
            }
            y += lineHeight * 2;
            drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
            y += lineHeight;
        }
        return y;
    }

    private int generateCustomerInvoicesReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Customer> customers = db.customerDao().getAllSync();
        List<Invoice> allSalesInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);

        for (Customer customer : customers) {
            List<Invoice> customerInvoices = new ArrayList<>();
            for (Invoice invoice : allSalesInvoices) {
                if (invoice.getCustomerId() != null && invoice.getCustomerId() == customer.getId() &&
                    invoice.getDate() >= from && invoice.getDate() <= to) {
                    customerInvoices.add(invoice);
                }
            }

            if (!customerInvoices.isEmpty()) {
                if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                    document.finishPage(currentPage);
                    currentPage = document.startPage(pageInfo);
                    canvas = currentPage.getCanvas();
                    y = drawPageHeader(canvas, "تقرير فواتير العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                }

                canvas.drawText("العميل: " + customer.getName(), x, y, paint);
                y += lineHeight;

                String[] headers = {"رقم الفاتورة", "التاريخ", "الإجمالي", "المدفوع", "المتبقي", "الحالة"};
                int[] widths = {100, 100, 70, 70, 70, 60};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : customerInvoices) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "تقرير فواتير العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("العميل: " + customer.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }

                    String status = "";
                    if (invoice.getPaymentStatus() == Invoice.STATUS_CASH) status = "نقدي";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_CREDIT) status = "آجل";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_PARTIAL) status = "جزئي";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_CARD) status = "بطاقة";

                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getTotal()),
                        CurrencyHelper.format(invoice.getPaidAmount()),
                        CurrencyHelper.format(invoice.getRemainingAmount()),
                        status
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
                drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                y += lineHeight;
            }
        }
        return y;
    }

    private int generateCustomerPaymentsMovementReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Customer> customers = db.customerDao().getAllSync();
        List<Invoice> allSalesInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);

        for (Customer customer : customers) {
            List<Invoice> customerPayments = new ArrayList<>();
            for (Invoice invoice : allSalesInvoices) {
                if (invoice.getCustomerId() != null && invoice.getCustomerId() == customer.getId() &&
                    invoice.getPaidAmount() > 0 && invoice.getDate() >= from && invoice.getDate() <= to) {
                    customerPayments.add(invoice);
                }
            }

            if (!customerPayments.isEmpty()) {
                if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                    document.finishPage(currentPage);
                    currentPage = document.startPage(pageInfo);
                    canvas = currentPage.getCanvas();
                    y = drawPageHeader(canvas, "تقرير حركة مدفوعات العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                }

                canvas.drawText("العميل: " + customer.getName(), x, y, paint);
                y += lineHeight;

                String[] headers = {"رقم الفاتورة", "التاريخ", "المبلغ المدفوع"};
                int[] widths = {150, 150, 100};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : customerPayments) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "تقرير حركة مدفوعات العملاء (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("العميل: " + customer.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }
                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getPaidAmount())
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
                y += lineHeight;
            }
        }
        return y;
    }

    private int generateSupplierAccountStatement(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Supplier> suppliers = db.supplierDao().getAllSync();
        List<Invoice> allPurchaseInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_PURCHASE);

        for (Supplier supplier : suppliers) {
            if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                document.finishPage(currentPage);
                currentPage = document.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                y = drawPageHeader(canvas, "كشف حساب الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
            }

            canvas.drawText("المورد: " + supplier.getName(), x, y, paint);
            y += lineHeight;
            canvas.drawText("الرصيد الحالي: " + CurrencyHelper.format(supplier.getCurrentBalance()), x, y, paint);
            y += lineHeight;

            List<Invoice> supplierInvoices = new ArrayList<>();
            for (Invoice invoice : allPurchaseInvoices) {
                if (invoice.getSupplierId() != null && invoice.getSupplierId() == supplier.getId() &&
                    invoice.getDate() >= from && invoice.getDate() <= to) {
                    supplierInvoices.add(invoice);
                }
            }

            if (!supplierInvoices.isEmpty()) {
                String[] headers = {"رقم الفاتورة", "التاريخ", "الإجمالي", "المدفوع", "المتبقي"};
                int[] widths = {100, 100, 70, 70, 70};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : supplierInvoices) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "كشف حساب الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("المورد: " + supplier.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }
                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getTotal()),
                        CurrencyHelper.format(invoice.getPaidAmount()),
                        CurrencyHelper.format(invoice.getRemainingAmount())
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
            }
            y += lineHeight * 2;
            drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
            y += lineHeight;
        }
        return y;
    }

    private int generateSupplierInvoicesReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Supplier> suppliers = db.supplierDao().getAllSync();
        List<Invoice> allPurchaseInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_PURCHASE);

        for (Supplier supplier : suppliers) {
            List<Invoice> supplierInvoices = new ArrayList<>();
            for (Invoice invoice : allPurchaseInvoices) {
                if (invoice.getSupplierId() != null && invoice.getSupplierId() == supplier.getId() &&
                    invoice.getDate() >= from && invoice.getDate() <= to) {
                    supplierInvoices.add(invoice);
                }
            }

            if (!supplierInvoices.isEmpty()) {
                if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                    document.finishPage(currentPage);
                    currentPage = document.startPage(pageInfo);
                    canvas = currentPage.getCanvas();
                    y = drawPageHeader(canvas, "تقرير فواتير الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                }

                canvas.drawText("المورد: " + supplier.getName(), x, y, paint);
                y += lineHeight;

                String[] headers = {"رقم الفاتورة", "التاريخ", "الإجمالي", "المدفوع", "المتبقي", "الحالة"};
                int[] widths = {100, 100, 70, 70, 70, 60};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : supplierInvoices) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "تقرير فواتير الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("المورد: " + supplier.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }

                    String status = "";
                    if (invoice.getPaymentStatus() == Invoice.STATUS_CASH) status = "نقدي";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_CREDIT) status = "آجل";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_PARTIAL) status = "جزئي";
                    else if (invoice.getPaymentStatus() == Invoice.STATUS_CARD) status = "بطاقة";

                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getTotal()),
                        CurrencyHelper.format(invoice.getPaidAmount()),
                        CurrencyHelper.format(invoice.getRemainingAmount()),
                        status
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
                drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                y += lineHeight;
            }
        }
        return y;
    }

    private int generateSupplierPaymentsMovementReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        List<Supplier> suppliers = db.supplierDao().getAllSync();
        List<Invoice> allPurchaseInvoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_PURCHASE);

        for (Supplier supplier : suppliers) {
            List<Invoice> supplierPayments = new ArrayList<>();
            for (Invoice invoice : allPurchaseInvoices) {
                if (invoice.getSupplierId() != null && invoice.getSupplierId() == supplier.getId() &&
                    invoice.getPaidAmount() > 0 && invoice.getDate() >= from && invoice.getDate() <= to) {
                    supplierPayments.add(invoice);
                }
            }

            if (!supplierPayments.isEmpty()) {
                if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM - 50) {
                    document.finishPage(currentPage);
                    currentPage = document.startPage(pageInfo);
                    canvas = currentPage.getCanvas();
                    y = drawPageHeader(canvas, "تقرير حركة مدفوعات الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                }

                canvas.drawText("المورد: " + supplier.getName(), x, y, paint);
                y += lineHeight;

                String[] headers = {"رقم الفاتورة", "التاريخ", "المبلغ المدفوع"};
                int[] widths = {150, 150, 100};

                y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                y += 5;

                for (Invoice invoice : supplierPayments) {
                    if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                        document.finishPage(currentPage);
                        currentPage = document.startPage(pageInfo);
                        canvas = currentPage.getCanvas();
                        y = drawPageHeader(canvas, "تقرير حركة مدفوعات الموردين (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                        canvas.drawText("المورد: " + supplier.getName() + " (تابع)", x, y, paint);
                        y += lineHeight;
                        y = drawTableHeader(canvas, headers, x + 20, y, paint, widths);
                        drawHorizontalLine(canvas, x + 20, y, pageInfo.getPageWidth() - x, paint);
                        y += 5;
                    }
                    String[] rowData = {
                        invoice.getInvoiceNumber(),
                        sdf.format(new Date(invoice.getDate())),
                        CurrencyHelper.format(invoice.getPaidAmount())
                    };
                    y = drawTableRow(canvas, rowData, x + 20, y, paint, lineHeight, widths);
                }
                y += lineHeight;
            }
        }
        return y;
    }

    private int generateExpensesByCategoryReport(PdfDocument document, PdfDocument.PageInfo pageInfo, PdfDocument.Page currentPage, Canvas canvas, int x, int y, Paint paint, int lineHeight, long from, long to) {
        paint.setTextSize(10);
        try {
            List<Expense> expenses = db.expenseDao().getAllSync();
            Map<String, Double> expensesByCategory = new HashMap<>();

            if (expenses != null) {
                for (Expense expense : expenses) {
                    if (expense.getDate() >= from && expense.getDate() <= to) {
                        String category = expense.getCategory() != null ? expense.getCategory() : "غير مصنف";
                        expensesByCategory.put(category, expensesByCategory.getOrDefault(category, 0.0) + expense.getAmount());
                    }
                }
            }

            if (expensesByCategory.isEmpty()) {
                canvas.drawText("لا توجد مصروفات حسب الفئة لهذه الفترة.", x, y, paint);
                return y + lineHeight;
            }

            String[] headers = {"الفئة", "إجمالي المصروفات"};
            int[] widths = {250, 150};
            y = drawTableHeader(canvas, headers, x, y, paint, widths);
            drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
            y += 5;

            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                if (y > pageInfo.getPageHeight() - MARGIN_BOTTOM) {
                    document.finishPage(currentPage);
                    currentPage = document.startPage(pageInfo);
                    canvas = currentPage.getCanvas();
                    y = drawPageHeader(canvas, "تقرير المصروفات حسب الفئة (تابع)", from, to, x, MARGIN_TOP, paint, pageInfo.getPageWidth());
                    y = drawTableHeader(canvas, headers, x, y, paint, widths);
                    drawHorizontalLine(canvas, x, y, pageInfo.getPageWidth() - x, paint);
                    y += 5;
                }
                String[] rowData = {entry.getKey(), CurrencyHelper.format(entry.getValue())};
                y = drawTableRow(canvas, rowData, x, y, paint, lineHeight, widths);
            }
        } catch (Exception e) {
            canvas.drawText("خطأ في تحميل بيانات المصروفات.", x, y, paint);
            y += lineHeight;
        }
        return y;
    }

    // ============ دوال مساعدة لرسم الجداول ============

    private int drawTableHeader(Canvas canvas, String[] headers, int x, int y, Paint paint, int[] widths) {
        paint.setFakeBoldText(true);
        int currentX = x;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], currentX, y, paint);
            currentX += widths[i];
        }
        y += (int) paint.getTextSize() + 5;
        paint.setFakeBoldText(false);
        return y;
    }

    private int drawTableRow(Canvas canvas, String[] rowData, int x, int y, Paint paint, int lineHeight, int[] widths) {
        int currentX = x;
        for (int i = 0; i < rowData.length; i++) {
            canvas.drawText(rowData[i] != null ? rowData[i] : "", currentX, y, paint);
            currentX += widths[i];
        }
        return y + lineHeight;
    }

    private void drawHorizontalLine(Canvas canvas, int x1, int y, int x2, Paint paint) {
        float savedStrokeWidth = paint.getStrokeWidth();
        paint.setStrokeWidth(0.5f);
        canvas.drawLine(x1, y, x2, y, paint);
        paint.setStrokeWidth(savedStrokeWidth);
    }
}
