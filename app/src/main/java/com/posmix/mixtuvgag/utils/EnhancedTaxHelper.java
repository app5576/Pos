package com.posmix.mixtuvgag.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * فئة محسنة للتعامل مع الضرائب
 */
public class EnhancedTaxHelper {

    // معدلات الضريبة الافتراضية
    public static final double VAT_RATE_SA = 15.0;
    public static final double VAT_RATE_AE = 5.0;

    /**
     * حساب مبلغ الضريبة
     */
    public static double calculateTax(double subtotal, double taxRate) {
        return subtotal * (taxRate / 100.0);
    }

    /**
     * حساب الإجمالي مع الضريبة
     */
    public static double calculateTotal(double subtotal, double taxRate) {
        return subtotal + calculateTax(subtotal, taxRate);
    }

    /**
     * الحصول على معلومات الضريبة
     */
    public static Map<String, Object> getTaxInfo(double subtotal, double taxRate, double discount) {
        Map<String, Object> taxInfo = new HashMap<>();
        
        double discountedSubtotal = subtotal - discount;
        double taxAmount = calculateTax(discountedSubtotal, taxRate);
        double total = discountedSubtotal + taxAmount;

        taxInfo.put("subtotal", subtotal);
        taxInfo.put("discount", discount);
        taxInfo.put("discountedSubtotal", discountedSubtotal);
        taxInfo.put("taxRate", taxRate);
        taxInfo.put("taxAmount", taxAmount);
        taxInfo.put("total", total);

        return taxInfo;
    }

    /**
     * تنسيق معلومات الضريبة للعرض
     */
    public static String formatTaxInfo(double subtotal, double taxRate, double discount) {
        Map<String, Object> taxInfo = getTaxInfo(subtotal, taxRate, discount);
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("المجموع الفرعي: ").append(String.format("%.2f", subtotal)).append("\n");
        formatted.append("الخصم: ").append(String.format("%.2f", discount)).append("\n");
        formatted.append("المجموع بعد الخصم: ").append(String.format("%.2f", taxInfo.get("discountedSubtotal"))).append("\n");
        formatted.append("معدل الضريبة: ").append(String.format("%.2f", taxRate)).append("%\n");
        formatted.append("مبلغ الضريبة: ").append(String.format("%.2f", taxInfo.get("taxAmount"))).append("\n");
        formatted.append("الإجمالي: ").append(String.format("%.2f", taxInfo.get("total")));

        return formatted.toString();
    }
}