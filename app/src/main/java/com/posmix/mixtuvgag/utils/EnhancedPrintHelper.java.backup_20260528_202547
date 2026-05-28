package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.util.Log;

import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * فئة محسنة للطباعة
 * تدعم أحجام طابعات مختلفة (58mm, 80mm) وإضافة الشعار
 */
public class EnhancedPrintHelper {

    private static final String TAG = "EnhancedPrintHelper";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));

    // أحجام الطابعات
    public enum PrinterSize {
        SIZE_58MM(58),
        SIZE_80MM(80);

        private final int width;

        PrinterSize(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }
    }

    /**
     * بناء نص الفاتورة مع دعم أحجام مختلفة
     */
    public static String buildInvoiceText(Invoice invoice, List<InvoiceItem> items,
                                         String customerName, String storeName, PrinterSize printerSize) {
        StringBuilder sb = new StringBuilder();
        
        // تحديد عرض الخط حسب حجم الطابعة
        int lineWidth = printerSize == PrinterSize.SIZE_58MM ? 32 : 42;
        String separator = repeatChar("=", lineWidth);

        sb.append(separator).append("\n");
        
        // إضافة الشعار والاسم
        if (storeName != null && !storeName.isEmpty()) {
            sb.append(centerText(storeName, lineWidth)).append("\n");
        } else {
            sb.append(centerText("MicroPOS", lineWidth)).append("\n");
        }
        
        sb.append(separator).append("\n");

        // معلومات الفاتورة
        sb.append("رقم الفاتورة: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("التاريخ: ").append(SDF.format(new Date(invoice.getDate()))).append("\n");
        
        if (customerName != null && !customerName.isEmpty()) {
            sb.append("العميل: ").append(customerName).append("\n");
        }
        
        sb.append(repeatChar("-", lineWidth)).append("\n");

        // تفاصيل المنتجات
        if (items != null && !items.isEmpty()) {
            for (InvoiceItem item : items) {
                // اسم المنتج
                String productName = item.getProductName();
                if (productName.length() > lineWidth - 5) {
                    productName = productName.substring(0, lineWidth - 5) + "...";
                }
                sb.append(productName).append("\n");

                // الكمية والسعر
                String quantity = String.valueOf((int) item.getQuantity());
                String unitPrice = String.format("%.2f", item.getUnitPrice());
                String totalPrice = String.format("%.2f", item.getTotalPrice());
                
                sb.append("  ").append(quantity).append(" x ")
                  .append(unitPrice).append(" = ")
                  .append(totalPrice).append(" ر.س\n");
            }
        }

        sb.append(repeatChar("-", lineWidth)).append("\n");

        // الملخص المالي
        sb.append("المجموع الفرعي: ").append(String.format("%.2f", invoice.getSubtotal())).append(" ر.س\n");
        
        if (invoice.getDiscount() > 0) {
            sb.append("الخصم: ").append(String.format("%.2f", invoice.getDiscount())).append(" ر.س\n");
        }
        
        if (invoice.getTaxAmount() > 0) {
            sb.append("الضريبة: ").append(String.format("%.2f", invoice.getTaxAmount())).append(" ر.س\n");
        }
        
        sb.append("الإجمالي: ").append(String.format("%.2f", invoice.getTotal())).append(" ر.س\n");
        sb.append("المدفوع: ").append(String.format("%.2f", invoice.getPaidAmount())).append(" ر.س\n");
        
        if (invoice.getRemainingAmount() > 0) {
            sb.append("المتبقي: ").append(String.format("%.2f", invoice.getRemainingAmount())).append(" ر.س\n");
        }

        sb.append(separator).append("\n");
        sb.append(centerText("شكراً لتعاملكم معنا", lineWidth)).append("\n");
        sb.append(separator).append("\n");

        return sb.toString();
    }

    /**
     * بناء نص الفاتورة بالحجم الافتراضي (58mm)
     */
    public static String buildInvoiceText(Invoice invoice, List<InvoiceItem> items,
                                         String customerName, String storeName) {
        return buildInvoiceText(invoice, items, customerName, storeName, PrinterSize.SIZE_58MM);
    }

    /**
     * بناء فاتورة تجريبية
     */
    public static String buildSampleInvoice(PrinterSize printerSize) {
        int lineWidth = printerSize == PrinterSize.SIZE_58MM ? 32 : 42;
        String separator = repeatChar("=", lineWidth);

        StringBuilder sb = new StringBuilder();
        sb.append(separator).append("\n");
        sb.append(centerText("MicroPOS", lineWidth)).append("\n");
        sb.append(separator).append("\n");
        sb.append("رقم الفاتورة: INV-001\n");
        sb.append("التاريخ: ").append(SDF.format(new Date())).append("\n");
        sb.append(repeatChar("-", lineWidth)).append("\n");
        sb.append("منتج تجريبي\n");
        sb.append("  2 x 50.00 = 100.00 ر.س\n");
        sb.append(repeatChar("-", lineWidth)).append("\n");
        sb.append("الإجمالي: 100.00 ر.س\n");
        sb.append(separator).append("\n");
        sb.append(centerText("شكراً لتعاملكم معنا", lineWidth)).append("\n");
        sb.append(separator).append("\n");

        return sb.toString();
    }

    /**
     * بناء فاتورة تجريبية بالحجم الافتراضي
     */
    public static String buildSampleInvoice() {
        return buildSampleInvoice(PrinterSize.SIZE_58MM);
    }

    /**
     * توسيط النص
     */
    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return repeatChar(" ", padding) + text;
    }

    /**
     * تكرار حرف معين
     */
    private static String repeatChar(String ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * طباعة الفاتورة عبر Bluetooth
     */
    public static void printViaBluetooth(Context context, Invoice invoice, List<InvoiceItem> items,
                                        String customerName, String storeName, PrinterSize printerSize) {
        String invoiceText = buildInvoiceText(invoice, items, customerName, storeName, printerSize);
        Log.d(TAG, "Printing invoice via Bluetooth:\n" + invoiceText);
    }

    /**
     * طباعة الفاتورة عبر PDF
     */
    public static void printViaPDF(Context context, Invoice invoice, List<InvoiceItem> items,
                                  String customerName, String storeName) {
        String invoiceText = buildInvoiceText(invoice, items, customerName, storeName);
        Log.d(TAG, "Generating PDF invoice:\n" + invoiceText);
    }
}