package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import androidx.preference.PreferenceManager;

import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoicePdfHelper {

    public static File generateThermalPdf(Context context, Invoice invoice, List<InvoiceItem> items) {
        // إعدادات الورقة (عرض 58 ملم يساوي تقريباً 200 نقطة)
        int width = 200;
        // حساب الطول ديناميكياً بناءً على عدد العناصر
        int notesExtraHeight = 0;
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            notesExtraHeight = 40 + (invoice.getNotes().length() / 28) * 12;
        }
        int height = 500 + (items.size() * 30) + notesExtraHeight;
        
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        // إعدادات الخطوط
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(8f);
        paint.setAntiAlias(true);
        
        // جلب بيانات المتجر من الإعدادات
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String storeName = prefs.getString("store_name", "MicroPOS");
        String storePhone = prefs.getString("store_phone", "000000000");
        String storeAddress = prefs.getString("store_address", "الموقع غير محدد");
        String currency = prefs.getString("currency", "ر.س");

        // جلب بيانات العميل إذا وجدت
        String custName = "عميل نقدي";
        String custPhone = "-";
        if (invoice.getCustomerId() != null) {
            Customer customer = AppDatabase.getInstance(context).customerDao().getByIdSync(invoice.getCustomerId());
            if (customer != null) {
                custName = customer.getName();
                custPhone = customer.getPhone();
            }
        }

        int y = 25;
        int centerX = width / 2;

        // 1. ترويسة الفاتورة
        paint.setTextSize(12f);
        paint.setFakeBoldText(true);
        drawCenteredText(canvas, storeName, centerX, y, paint);
        
        paint.setTextSize(7f);
        paint.setFakeBoldText(false);
        y += 15;
        drawCenteredText(canvas, storeAddress, centerX, y, paint);
        y += 10;
        drawCenteredText(canvas, "تلفون: " + storePhone, centerX, y, paint);
        
        y += 20;
        paint.setTextSize(10f);
        paint.setFakeBoldText(true);
        String typeLabel = (invoice.getPaymentStatus() == Invoice.STATUS_CREDIT) ? "فاتورة مبيعات (آجل)" : "فاتورة مبيعات (نقدي)";
        drawCenteredText(canvas, typeLabel, centerX, y, paint);

        // البيانات الأساسية
        paint.setTextSize(7f);
        paint.setFakeBoldText(false);
        y += 20;
        canvas.drawText("الرقم: " + invoice.getInvoiceNumber(), 10, y, paint);
        canvas.drawText("العملة: " + currency, 130, y, paint);
        y += 12;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(invoice.getDate()));
        canvas.drawText("التاريخ: " + dateStr, 10, y, paint);
        
        y += 12;
        canvas.drawText("العميل: " + custName, 10, y, paint);
        y += 12;
        canvas.drawText("رقم العميل: " + custPhone, 10, y, paint);

        // خط فاصل منقط
        y += 10;
        drawDashedLine(canvas, 5, y, width - 5, y, paint);

        // 2. جدول المحتويات (Headers)
        y += 15;
        paint.setFakeBoldText(true);
        canvas.drawText("الصنف", 10, y, paint);
        canvas.drawText("كمية", 80, y, paint);
        canvas.drawText("وحدة", 105, y, paint);
        canvas.drawText("سعر", 140, y, paint);
        canvas.drawText("إجمالي", 170, y, paint);
        paint.setFakeBoldText(false);

        y += 8;
        canvas.drawLine(5, y, width - 5, y, paint);

        // تفاصيل الأصناف
        double totalItemsQty = 0;
        for (InvoiceItem item : items) {
            y += 15;
            // اسم الصنف (قص الاسم إذا كان طويلاً)
            String n = item.getProductName();
            if (n.length() > 15) n = n.substring(0, 13) + "..";
            canvas.drawText(n, 10, y, paint);
            
            canvas.drawText(String.valueOf((int)item.getQuantity()), 85, y, paint);
            canvas.drawText("حبة", 105, y, paint); // يمكنك جلبها من item.getUnit() لاحقاً
            canvas.drawText(formatEng(item.getUnitPrice()), 135, y, paint);
            canvas.drawText(formatEng(item.getTotalPrice()), 170, y, paint);
            
            totalItemsQty += item.getQuantity();
        }

        y += 10;
        drawDashedLine(canvas, 5, y, width - 5, y, paint);
        y += 12;
        canvas.drawText("إجمالي عدد القطع: " + (int)totalItemsQty, 10, y, paint);

        // 3. الملخص المالي
        y += 25;
        int summaryX = 110;
        canvas.drawText("الإجمالي قبل الخصم:", 10, y, paint);
        canvas.drawText(formatEng(invoice.getSubtotal()) + " " + currency, summaryX, y, paint);
        
        y += 12;
        canvas.drawText("قيمة الخصم:", 10, y, paint);
        canvas.drawText(formatEng(invoice.getDiscount()) + " " + currency, summaryX, y, paint);
        
        y += 12;
        canvas.drawText("الضريبة (0%):", 10, y, paint);
        canvas.drawText("0.00", summaryX, y, paint);

        y += 15;
        paint.setFakeBoldText(true);
        paint.setTextSize(9f);
        canvas.drawRect(5, y - 12, width - 5, y + 5, getLightGrayPaint());
        canvas.drawText("الصافي المستحق:", 10, y, paint);
        canvas.drawText(formatEng(invoice.getTotal()) + " " + currency, summaryX, y, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(7f);

        y += 25;
        canvas.drawText("فقط لا غير: " + "سيتم إضافة التفقيط لاحقاً", 10, y, paint);

        // 4. الملاحظات (إن وجدت)
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            y += 15;
            drawDashedLine(canvas, 5, y, width - 5, y, paint);
            y += 14;
            paint.setFakeBoldText(true);
            canvas.drawText("ملاحظات:", 10, y, paint);
            paint.setFakeBoldText(false);
            y += 12;
            String remaining = invoice.getNotes();
            while (!remaining.isEmpty()) {
                String line = remaining.length() > 28 ? remaining.substring(0, 28) : remaining;
                canvas.drawText(line, 10, y, paint);
                y += 11;
                remaining = remaining.length() > 28 ? remaining.substring(28) : "";
            }
        }

        // 5. التوقيعات
        y += 40;
        canvas.drawLine(10, y, 60, y, paint);
        canvas.drawLine(140, y, 190, y, paint);
        y += 10;
        canvas.drawText("المحاسب", 20, y, paint);
        canvas.drawText("المستلم", 155, y, paint);
        
        y += 25;
        drawCenteredText(canvas, "*** شكراً لزيارتكم ***", centerX, y, paint);

        document.finishPage(page);
        return saveFile(context, document, "Invoice_" + invoice.getInvoiceNumber());
    }

    private static void drawCenteredText(Canvas canvas, String text, int x, int y, Paint paint) {
        float width = paint.measureText(text);
        canvas.drawText(text, x - (width / 2), y, paint);
    }

    private static void drawDashedLine(Canvas canvas, float startX, float y, float stopX, float paintY, Paint paint) {
        paint.setPathEffect(new DashPathEffect(new float[]{2, 2}, 0));
        canvas.drawLine(startX, y, stopX, paintY, paint);
        paint.setPathEffect(null);
    }

    private static Paint getLightGrayPaint() {
        Paint p = new Paint();
        p.setColor(Color.LTGRAY);
        p.setAlpha(50);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private static String formatEng(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static File saveFile(Context context, PdfDocument document, String name) {
        File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, name + ".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}