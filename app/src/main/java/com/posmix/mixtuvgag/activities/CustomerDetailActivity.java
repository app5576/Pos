package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.CustomerHistoryAdapter; // Changed to CustomerHistoryAdapter
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.CashTransaction;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
import com.posmix.mixtuvgag.viewmodels.CustomerDetailViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import com.posmix.mixtuvgag.databinding.ActivityCustomerDetailBinding;
import com.posmix.mixtuvgag.utils.EnhancedPrintHelper; // Added for printing

public class CustomerDetailActivity extends AppCompatActivity {

    private ActivityCustomerDetailBinding binding;
    private CustomerDetailViewModel viewModel;
    private int customerId;
    private Customer currentCustomer;
    private CustomerHistoryAdapter customerHistoryAdapter; // Changed to CustomerHistoryAdapter

    private static final Locale ENGLISH_LOCALE = Locale.US;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", ENGLISH_LOCALE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("تفاصيل العميل");
        }

        customerId = getIntent().getIntExtra(CustomersActivity.EXTRA_CUSTOMER_ID, -1);
        if (customerId == -1) {
            Toast.makeText(this, "خطأ: لم يتم تحديد العميل.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CustomerDetailViewModel.class);

        setupCustomerDetails();
        setupCustomerHistoryRecyclerView(); // Renamed and modified
        setupEditCustomerButton();
        setupPdfReportButton();
        setupPaymentButton();

        viewModel.loadCustomer(customerId);
        viewModel.loadOutstandingInvoices(customerId); // Still load outstanding for summary card
        viewModel.loadCustomerHistory(customerId); // Load combined history
    }

    private void setupCustomerDetails() {
        viewModel.getCustomer().observe(this, customer -> {
            if (customer != null) {
                currentCustomer = customer;
                binding.tvDetailCustomerName.setText(customer.getName());
                binding.tvDetailCustomerPhone.setText(getString(R.string.customer_detail_phone_label) + " " + formatNumberToEnglishDigits(customer.getPhone() != null ? customer.getPhone() : getString(R.string.not_specified)));
                binding.tvDetailCustomerEmail.setText(getString(R.string.customer_detail_email_label) + " " + formatNumberToEnglishDigits(customer.getEmail() != null ? customer.getEmail() : getString(R.string.not_specified)));
                binding.tvDetailCustomerAddress.setText(getString(R.string.customer_detail_address_label) + " " + formatNumberToEnglishDigits(customer.getAddress() != null ? customer.getAddress() : getString(R.string.not_specified)));
                
                String balanceText = String.format(ENGLISH_LOCALE, "%s %.2f %s", getString(R.string.customer_detail_balance_label), customer.getCurrentBalance(), CurrencyHelper.getSymbol());
                binding.tvDetailCustomerBalance.setText(balanceText);

                if (customer.getCurrentBalance() > 0) {
                    binding.tvDetailCustomerBalance.setTextColor(getColor(android.R.color.holo_red_dark));
                } else {
                    binding.tvDetailCustomerBalance.setTextColor(getColor(R.color.primary));
                }

                // Update summary card and total credit display
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(this);
                    List<Invoice> outstandingInvoicesList = db.invoiceDao().getCreditInvoicesByCustomerSync(customer.getId());
                    double totalCredit = db.invoiceDao().getTotalCreditByCustomer(customer.getId());

                    runOnUiThread(() -> {
                        binding.tvSummaryCount.setText(getString(R.string.customer_detail_unpaid_invoices_count, outstandingInvoicesList.size()));
                        binding.tvSummaryTotal.setText(String.format(ENGLISH_LOCALE, "%.2f %s", totalCredit, CurrencyHelper.getSymbol()));
                        
                        binding.tvTotalCredit.setText(String.format(ENGLISH_LOCALE, "%s: %.2f %s", 
                            getString(R.string.customer_detail_total_credit_label), totalCredit, CurrencyHelper.getSymbol()));
                    });
                });
            }
        });
    }

    private void setupCustomerHistoryRecyclerView() {
        binding.rvCustomerInvoices.setLayoutManager(new LinearLayoutManager(this));
        customerHistoryAdapter = new CustomerHistoryAdapter(new CustomerHistoryAdapter.OnHistoryItemClickListener() {
            @Override
            public void onInvoiceClick(Invoice invoice) {
                showInvoiceOptionsDialog(invoice);
            }

            @Override
            public void onCashTransactionClick(CashTransaction transaction) {
                showCashTransactionOptionsDialog(transaction);
            }
        });
        binding.rvCustomerInvoices.setAdapter(customerHistoryAdapter);

        viewModel.getCustomerHistory().observe(this, history -> {
            if (history != null) {
                customerHistoryAdapter.submitList(history);
                if (history.isEmpty()) {
                    binding.tvEmptyInvoices.setVisibility(View.VISIBLE);
                    binding.rvCustomerInvoices.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyInvoices.setVisibility(View.GONE);
                    binding.rvCustomerInvoices.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupEditCustomerButton() {
        binding.btnEditCustomerDetail.setOnClickListener(v -> {
            if (currentCustomer != null) {
                showEditCustomerDialog(currentCustomer);
            }
        });
    }

    private void setupPdfReportButton() {
        binding.btnGeneratePdfReport.setOnClickListener(v -> {
            if (currentCustomer != null) {
                generateCustomerPdfReport(currentCustomer);
            }
        });
    }

    private void setupPaymentButton() {
        if (binding.btnMakePayment != null) {
            binding.btnMakePayment.setOnClickListener(v -> {
                if (currentCustomer != null) {
                    showPaymentDialogForCustomer(); // Overall customer payment
                }
            });
        }
    }
    
    // Dialog for overall customer payment (not tied to a specific invoice)
    private void showPaymentDialogForCustomer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        EditText etAmount = view.findViewById(R.id.et_payment_amount);
        
        // Optionally display current balance in the dialog
        TextView tvCurrentCredit = view.findViewById(R.id.tv_current_credit);
        if (tvCurrentCredit != null) {
            tvCurrentCredit.setVisibility(View.VISIBLE);
            tvCurrentCredit.setText(String.format(ENGLISH_LOCALE, "إجمالي الدين الحالي: %.2f %s", currentCustomer.getCurrentBalance(), CurrencyHelper.getSymbol()));
        }

        builder.setTitle(getString(R.string.customer_detail_make_payment) + " - " + currentCustomer.getName())
                .setView(view)
                .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
                    String str = etAmount.getText().toString().trim();
                    if (str.isEmpty()) {
                        Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(str);
                    if (amount <= 0) {
                         Toast.makeText(this, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show();
                         return;
                    }

                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        
                        // Update customer balance directly for overall payment
                        currentCustomer.setCurrentBalance(currentCustomer.getCurrentBalance() - amount);
                        db.customerDao().update(currentCustomer);
                        
                        // Record as a general cash transaction
                        CashTransaction trans = new CashTransaction();
                        trans.setType(CashTransaction.TYPE_IN);
                        trans.setAmount(amount);
                        trans.setDate(System.currentTimeMillis());
                        trans.setDescription(getString(R.string.customer_detail_make_payment) + " من العميل: " + currentCustomer.getName());
                        trans.setReferenceId(currentCustomer.getId());
                        trans.setReferenceType("CUSTOMER_PAYMENT");
                        db.cashTransactionDao().insert(trans);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "تم التسديد بنجاح", Toast.LENGTH_SHORT).show();
                            // Reload both customer details and outstanding invoices to refresh UI
                            viewModel.loadCustomer(customerId);
                            viewModel.loadOutstandingInvoices(customerId);
                            viewModel.loadCustomerHistory(customerId); // Refresh combined history
                        });
                    });
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showInvoiceOptionsDialog(Invoice invoice) {
        String[] options = {"تسديد هذه الفاتورة", "تعديل الفاتورة", "طباعة الفاتورة", "عرض التفاصيل", "حذف الفاتورة"};
        
        new AlertDialog.Builder(this)
            .setTitle("خيارات الفاتورة: " + invoice.getInvoiceNumber())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // تسديد هذه الفاتورة
                        showPaySpecificInvoiceDialog(invoice);
                        break;
                    case 1: // تعديل الفاتورة
                        openEditInvoiceActivity(invoice);
                        break;
                    case 2: // طباعة الفاتورة
                        printInvoice(invoice);
                        break;
                    case 3: // عرض التفاصيل
                        showInvoiceDetails(invoice);
                        break;
                    case 4: // حذف الفاتورة
                        showDeleteInvoiceConfirmation(invoice);
                        break;
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void showCashTransactionOptionsDialog(CashTransaction transaction) {
        String[] options = {"تعديل السند", "طباعة السند", "حذف السند"};
        
        new AlertDialog.Builder(this)
            .setTitle("خيارات السند: " + transaction.getDescription())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // تعديل السند
                        showEditPaymentDialog(transaction);
                        break;
                    case 1: // طباعة السند
                        printPaymentReceipt(transaction);
                        break;
                    case 2: // حذف السند
                        confirmDeletePayment(transaction);
                        break;
                }
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void showPaySpecificInvoiceDialog(Invoice invoice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        EditText etAmount = view.findViewById(R.id.et_payment_amount);
        TextView tvPaymentInfo = view.findViewById(R.id.tv_payment_info);
        
        tvPaymentInfo.setText(String.format(ENGLISH_LOCALE, "المبلغ المتبقي على فاتورة رقم %s: %.2f %s", 
            invoice.getInvoiceNumber(), invoice.getRemainingAmount(), CurrencyHelper.getSymbol()));
        etAmount.setText(String.format(ENGLISH_LOCALE, "%.2f", invoice.getRemainingAmount()));

        builder.setTitle(getString(R.string.customer_detail_make_payment))
            .setView(view)
            .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
                String str = etAmount.getText().toString().trim();
                if (str.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                    return;
                }
                double paymentAmount = Double.parseDouble(str);
                
                if (paymentAmount <= 0) {
                     Toast.makeText(this, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show();
                     return;
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(this);
                    
                    // Record payment on the specific invoice
                    db.invoiceDao().recordPayment(invoice.getId(), paymentAmount);

                    // Update customer's total balance
                    if (currentCustomer != null) {
                        currentCustomer.setCurrentBalance(currentCustomer.getCurrentBalance() - paymentAmount);
                        db.customerDao().update(currentCustomer);
                    }

                    // Record cash transaction
                    CashTransaction trans = new CashTransaction();
                    trans.setType(CashTransaction.TYPE_IN);
                    trans.setAmount(paymentAmount);
                    trans.setDate(System.currentTimeMillis());
                    trans.setDescription(String.format(ENGLISH_LOCALE, "تسديد جزئي/كلي لفاتورة رقم %s من العميل %s", invoice.getInvoiceNumber(), currentCustomer.getName()));
                    trans.setReferenceId(invoice.getId()); // Reference invoice ID
                    trans.setReferenceType("INVOICE_PAYMENT");
                    db.cashTransactionDao().insert(trans);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "تم تسديد الفاتورة بنجاح", Toast.LENGTH_SHORT).show();
                        viewModel.loadCustomer(customerId);
                        viewModel.loadOutstandingInvoices(customerId);
                        viewModel.loadCustomerHistory(customerId); // Refresh combined history
                    });
                });
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void showEditPaymentDialog(CashTransaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cash_transaction, null);
        EditText etAmount = view.findViewById(R.id.et_transaction_amount);
        EditText etDesc = view.findViewById(R.id.et_transaction_desc);

        etAmount.setText(String.format(ENGLISH_LOCALE, "%.2f", transaction.getAmount()));
        etDesc.setText(transaction.getDescription());

        builder.setTitle("تعديل سند الدفعة")
            .setView(view)
            .setPositiveButton(getString(R.string.btn_save), (dialog, which) -> {
                String amountStr = etAmount.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    double oldAmount = transaction.getAmount();
                    double newAmount = Double.parseDouble(amountStr);

                    transaction.setAmount(newAmount);
                    transaction.setDescription(desc);
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        db.cashTransactionDao().update(transaction);
                        
                        // Adjust customer balance
                        if (currentCustomer != null) {
                            double balanceDiff = newAmount - oldAmount;
                            if (transaction.getType() == CashTransaction.TYPE_IN) { // Payment IN, so reduces customer debt
                                currentCustomer.setCurrentBalance(currentCustomer.getCurrentBalance() - balanceDiff);
                            } else { // Payment OUT, should not be here for customer payments
                                // This case should ideally not happen for customer payment 'TYPE_IN'
                            }
                            db.customerDao().update(currentCustomer);
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(this, "تم تعديل السند بنجاح", Toast.LENGTH_SHORT).show();
                            viewModel.loadCustomer(customerId);
                            viewModel.loadOutstandingInvoices(customerId);
                            viewModel.loadCustomerHistory(customerId);
                        });
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "مبلغ غير صحيح", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void confirmDeletePayment(CashTransaction transaction) {
        new AlertDialog.Builder(this)
            .setTitle("حذف سند الدفعة")
            .setMessage("هل أنت متأكد من حذف هذا السند؟ سيتم عكس تأثيره على رصيد العميل.")
            .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(this);
                    
                    // Revert customer balance
                    if (currentCustomer != null) {
                        if (transaction.getType() == CashTransaction.TYPE_IN) { // Payment IN, so add back to customer debt
                            currentCustomer.setCurrentBalance(currentCustomer.getCurrentBalance() + transaction.getAmount());
                        } else { // Payment OUT, should not be here for customer payments
                            // This case should ideally not happen for customer payment 'TYPE_IN'
                        }
                        db.customerDao().update(currentCustomer);
                    }
                    
                    db.cashTransactionDao().delete(transaction);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "تم حذف السند بنجاح", Toast.LENGTH_SHORT).show();
                        viewModel.loadCustomer(customerId);
                        viewModel.loadOutstandingInvoices(customerId);
                        viewModel.loadCustomerHistory(customerId);
                    });
                });
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void printPaymentReceipt(CashTransaction transaction) {
        // Simple text receipt for payment transaction
        String receiptText = buildPaymentReceiptText(transaction, currentCustomer.getName(), "MicroPOS");
        
        Intent intent = new Intent(this, PrintActivity.class);
        intent.putExtra(PrintActivity.EXTRA_INVOICE_TEXT, receiptText);
        startActivity(intent);
    }
    
    private String buildPaymentReceiptText(CashTransaction transaction, String customerName, String storeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("        ").append(storeName).append("\n");
        sb.append("================================\n");
        sb.append("سند قبض\n");
        sb.append("--------------------------------\n");
        sb.append("الرقم: ").append(transaction.getId()).append("\n");
        sb.append("التاريخ: ").append(dateFormat.format(new Date(transaction.getDate()))).append("\n");
        sb.append("العميل: ").append(customerName).append("\n");
        sb.append("الوصف: ").append(transaction.getDescription()).append("\n");
        sb.append("--------------------------------\n");
        sb.append("المبلغ: ").append(String.format(Locale.US, "%.2f %s", transaction.getAmount(), CurrencyHelper.getSymbol())).append("\n");
        sb.append("================================\n");
        sb.append("       شكراً لتعاملكم معنا\n");
        sb.append("================================\n");
        return sb.toString();
    }


    private void openEditInvoiceActivity(Invoice invoice) {
        Intent intent = new Intent(this, EditInvoiceActivity.class);
        intent.putExtra("invoice_id", invoice.getId());
        startActivity(intent);
    }
    
    private void printInvoice(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<InvoiceItem> items = AppDatabase.getInstance(this)
                .invoiceDao().getItemsForInvoiceSync(invoice.getId());
            
            String customerName = "عميل نقدي";
            if (invoice.getCustomerId() != null) {
                Customer customer = AppDatabase.getInstance(this).customerDao().getByIdSync(invoice.getCustomerId());
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

    private void showDeleteInvoiceConfirmation(Invoice invoice) {
        new AlertDialog.Builder(this)
            .setTitle("حذف الفاتورة")
            .setMessage("هل أنت متأكد من حذف الفاتورة رقم " + invoice.getInvoiceNumber() + "؟ هذا الإجراء لا يمكن التراجع عنه.")
            .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> deleteInvoice(invoice))
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void deleteInvoice(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            try {
                db.getWritableDatabase().beginTransaction();
                
                // Revert stock for all items in the invoice
                List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(invoice.getId());
                if (items != null) {
                    for (InvoiceItem item : items) {
                        db.productDao().increaseStock(item.getProductId(), (int) item.getQuantity());
                    }
                }
                
                // Adjust customer balance if it was a credit sale
                if (currentCustomer != null) {
                    if (invoice.getRemainingAmount() > 0 || invoice.getPaymentStatus() == Invoice.STATUS_CREDIT) {
                        currentCustomer.setCurrentBalance(currentCustomer.getCurrentBalance() - invoice.getRemainingAmount());
                        db.customerDao().update(currentCustomer);
                    }
                }
                
                // Delete all invoice items first
                db.getWritableDatabase().delete("invoice_items", "invoice_id=?", new String[]{String.valueOf(invoice.getId())});
                // Then delete the invoice
                db.invoiceDao().delete(invoice);
                
                db.getWritableDatabase().setTransactionSuccessful();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "تم حذف الفاتورة بنجاح", Toast.LENGTH_SHORT).show();
                    viewModel.loadCustomer(customerId);
                    viewModel.loadOutstandingInvoices(customerId);
                    viewModel.loadCustomerHistory(customerId); // Refresh combined history
                });
            } catch (Exception e) {
                Log.e("CustomerDetailActivity", "Error deleting invoice", e);
                runOnUiThread(() -> Toast.makeText(this, "خطأ في حذف الفاتورة: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                db.getWritableDatabase().endTransaction();
            }
        });
    }

    private void showInvoiceDetails(Invoice invoice) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<InvoiceItem> items = AppDatabase.getInstance(this)
                .invoiceDao().getItemsForInvoiceSync(invoice.getId());
            
            StringBuilder details = new StringBuilder();
            details.append("📋 تفاصيل الفاتورة\n\n");
            details.append("رقم الفاتورة: ").append(formatNumberToEnglishDigits(invoice.getInvoiceNumber())).append("\n");
            details.append("التاريخ: ").append(dateFormat.format(new Date(invoice.getDate()))).append("\n");
            
            String customerName = "عميل نقدي";
            if (invoice.getCustomerId() != null) {
                Customer customer = AppDatabase.getInstance(this).customerDao().getByIdSync(invoice.getCustomerId());
                if (customer != null) customerName = customer.getName();
            }
            details.append("العميل: ").append(customerName).append("\n");
            details.append("\n--- الأصناف ---\n");
            
            if (items != null) {
                for (InvoiceItem item : items) {
                    details.append("• ").append(item.getProductName())
                           .append(" | ").append(String.format(Locale.US, "%d", (int)item.getQuantity()))
                           .append(" × ").append(String.format(Locale.US, "%.2f", item.getUnitPrice()))
                           .append(" = ").append(String.format(Locale.US, "%.2f", item.getTotalPrice())).append("\n");
                }
            }
            
            details.append("\n--- الملخص ---\n");
            details.append("المجموع: ").append(String.format(Locale.US, "%.2f", invoice.getSubtotal())).append("\n");
            details.append("الخصم: ").append(String.format(Locale.US, "%.2f", invoice.getDiscount())).append("\n");
            details.append("الإجمالي: ").append(String.format(Locale.US, "%.2f", invoice.getTotal())).append("\n");
            details.append("المدفوع: ").append(String.format(Locale.US, "%.2f", invoice.getPaidAmount())).append("\n");
            details.append("المتبقي: ").append(String.format(Locale.US, "%.2f", invoice.getRemainingAmount())).append("\n");
            
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


    private void showEditCustomerDialog(Customer customer) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_customer, null);
        EditText etName = view.findViewById(R.id.et_customer_name);
        EditText etPhone = view.findViewById(R.id.et_customer_phone);
        EditText etAddress = view.findViewById(R.id.et_customer_address);
        EditText etEmail = view.findViewById(R.id.et_customer_email);

        etName.setText(customer.getName());
        etPhone.setText(customer.getPhone());
        etAddress.setText(customer.getAddress());
        etEmail.setText(customer.getEmail());

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.customer_detail_edit_customer))
            .setView(view)
            .setPositiveButton(getString(R.string.btn_save), (d, w) -> {
                customer.setName(etName.getText().toString().trim());
                customer.setPhone(etPhone.getText().toString().trim());
                customer.setAddress(etAddress.getText().toString().trim());
                customer.setEmail(etEmail.getText().toString().trim());
                viewModel.updateCustomer(customer);
                Toast.makeText(this, getString(R.string.success_message), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void generateCustomerPdfReport(Customer customer) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setTextSize(12);
                paint.setColor(android.graphics.Color.BLACK);

                int y = 40;
                int x = 40;

                paint.setTextSize(18);
                paint.setFakeBoldText(true);
                canvas.drawText(getString(R.string.report_customer_statement_of_account), x, y, paint);
                y += 30;

                paint.setTextSize(12);
                paint.setFakeBoldText(false);
                canvas.drawText(getString(R.string.label_name) + ": " + customer.getName(), x, y, paint);
                y += 20;
                canvas.drawText(getString(R.string.customer_detail_phone_label) + " " + (customer.getPhone() != null ? formatNumberToEnglishDigits(customer.getPhone()) : getString(R.string.not_specified)), x, y, paint);
                y += 20;
                
                String balanceText = String.format(ENGLISH_LOCALE, "%s %.2f %s", getString(R.string.customer_detail_balance_label), customer.getCurrentBalance(), CurrencyHelper.getSymbol());
                canvas.drawText(balanceText, x, y, paint);
                y += 30;

                List<Invoice> invoices = AppDatabase.getInstance(this)
                    .invoiceDao().getAllInvoicesByCustomerSync(customer.getId());
                if (invoices != null && !invoices.isEmpty()) {
                    canvas.drawText(getString(R.string.customer_detail_outstanding_invoices_title), x, y, paint);
                    y += 20;
                    for (Invoice inv : invoices) {
                        if (y > 800) {
                            document.finishPage(page);
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = 40;
                        }
                        String invoiceLine = formatNumberToEnglishDigits(inv.getInvoiceNumber()) + " - " +
                            String.format(ENGLISH_LOCALE, "%.2f %s", inv.getTotal(), CurrencyHelper.getSymbol()) + " - " +
                            String.format(ENGLISH_LOCALE, "%s: %.2f %s", getString(R.string.invoice_remaining), inv.getRemainingAmount(), CurrencyHelper.getSymbol());
                        canvas.drawText(invoiceLine, x + 20, y, paint);
                        y += 20;
                    }
                }

                document.finishPage(page);

                String fileName = "customer_" + customer.getId() + "_" + System.currentTimeMillis() + ".pdf";
                File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadsDir == null) {
                    runOnUiThread(() -> Toast.makeText(this, "خطأ في الوصول لمجلد التنزيلات", Toast.LENGTH_SHORT).show());
                    return;
                }
                File file = new File(downloadsDir, fileName);
                document.writeTo(new FileOutputStream(file));
                document.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.success_message) + ": " + fileName, Toast.LENGTH_LONG).show();
                    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("application/pdf");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(share, getString(R.string.action_share_pdf_text)));
                });

            } catch (Exception e) {
                Log.e("CustomerDetail", "Error generating PDF", e);
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_message) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String formatNumberToEnglishDigits(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '٠' && c <= '٩') { // Arabic-Indic digits
                builder.append((char) (c - '٠' + '0')); // Convert to Western Arabic digits
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities (e.g., EditInvoiceActivity)
        viewModel.loadCustomer(customerId);
        viewModel.loadOutstandingInvoices(customerId);
        viewModel.loadCustomerHistory(customerId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}