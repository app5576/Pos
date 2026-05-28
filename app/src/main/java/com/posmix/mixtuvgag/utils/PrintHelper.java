package com.posmix.mixtuvgag.utils;

import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrintHelper {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));

    public static String buildInvoiceText(Invoice invoice, List<InvoiceItem> items,
                                          String customerName, String storeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("        ").append(storeName != null ? storeName : "MicroPOS").append("\n");
        sb.append("================================\n");
        sb.append("رقم الفاتورة: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("التاريخ: ").append(SDF.format(new Date(invoice.getDate()))).append("\n");
        if (customerName != null && !customerName.isEmpty())
            sb.append("العميل: ").append(customerName).append("\n");
        sb.append("--------------------------------\n");
        if (items != null) {
            for (InvoiceItem item : items) {
                sb.append(item.getProductName()).append("\n");
                sb.append("  ").append((int)item.getQuantity()).append(" x ")
                  .append(String.format("%.2f", item.getUnitPrice())).append(" = ")
                  .append(String.format("%.2f", item.getTotalPrice())).append(" ر.س\n");
            }
        }
        sb.append("--------------------------------\n");
        sb.append("المجموع الفرعي: ").append(String.format("%.2f", invoice.getSubtotal())).append(" ر.س\n");
        if (invoice.getDiscount() > 0)
            sb.append("الخصم: ").append(String.format("%.2f", invoice.getDiscount())).append(" ر.س\n");
        if (invoice.getTaxAmount() > 0)
            sb.append("الضريبة: ").append(String.format("%.2f", invoice.getTaxAmount())).append(" ر.س\n");
        sb.append("الإجمالي: ").append(String.format("%.2f", invoice.getTotal())).append(" ر.س\n");
        sb.append("المدفوع: ").append(String.format("%.2f", invoice.getPaidAmount())).append(" ر.س\n");
        if (invoice.getRemainingAmount() > 0)
            sb.append("المتبقي: ").append(String.format("%.2f", invoice.getRemainingAmount())).append(" ر.س\n");
        sb.append("================================\n");
        sb.append("       شكراً لتعاملكم معنا\n");
        sb.append("================================\n");
        return sb.toString();
    }

    public static String buildSampleInvoice() {
        return "================================\n" +
               "           MicroPOS\n" +
               "================================\n" +
               "رقم الفاتورة: INV-001\n" +
               "التاريخ: " + SDF.format(new Date()) + "\n" +
               "--------------------------------\n" +
               "منتج تجريبي\n" +
               "  2 x 50.00 = 100.00 ر.س\n" +
               "--------------------------------\n" +
               "الإجمالي: 100.00 ر.س\n" +
               "================================\n" +
               "       شكراً لتعاملكم معنا\n" +
               "================================\n";
    }
}