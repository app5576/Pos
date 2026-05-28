package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * مساعد تنسيق العملات - يدعم تغيير رمز العملة بشكل حي من الإعدادات
 */
public class CurrencyHelper {
    private static Context appContext;

    /**
     * تهيئة المساعد بسياق التطبيق - تستدعى مرة واحدة عند بدء التطبيق
     * @param context سياق التطبيق (Application Context)
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * تنسيق مبلغ مالي مع رمز العملة الحالي من الإعدادات
     * @param amount المبلغ المراد تنسيقه
     * @return نص منسق مثل "1,500 ر.س" أو "1,500 USD"
     */
    public static String format(double amount) {
        String symbol = "ر.س"; // القيمة الافتراضية
        if (appContext != null) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
                symbol = prefs.getString("currency", "ر.س");
            } catch (Exception e) {
                // في حالة حدوث خطأ، استخدم القيمة الافتراضية
            }
        }
        DecimalFormat df = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.US));
        return df.format(amount) + " " + symbol;
    }
    
    /**
     * الحصول على رمز العملة الحالي فقط
     */
    public static String getSymbol() {
        if (appContext != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            return prefs.getString("currency", "ر.س");
        }
        return "ر.س";
    }
}
