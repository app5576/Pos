
package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Product;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ExcelHelper - استيراد وتصدير المنتجات
 * 
 * تنسيق الأعمدة:
 * A: اسم المنتج
 * B: سعر البيع
 * C: سعر الشراء
 */
public class ExcelHelper {

    private static final String TAG = "ExcelHelper";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ================================================================
    // استيراد
    // ================================================================
    public static void importProductsFromExcel(Context context, Uri fileUri, ImportCallback callback) {
        executor.execute(() -> {
            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();

            try {
                InputStream is = context.getContentResolver().openInputStream(fileUri);
                if (is == null) {
                    if (callback != null) callback.onError("❌ لا يمكن فتح الملف");
                    return;
                }

                byte[] data = readAllBytes(is);
                is.close();

                if (data == null || data.length == 0) {
                    if (callback != null) callback.onError("❌ الملف فارغ");
                    return;
                }

                List<String[]> rows = null;

                // تحديد نوع الملف
                if (isXlsx(data)) {
                    rows = readXlsx(data);
                } else if (isXls(data)) {
                    rows = readXls(context, fileUri);
                } else {
                    rows = readCsv(data);
                }

                if (rows == null || rows.isEmpty()) {
                    if (callback != null) callback.onError(
                        "❌ لا توجد بيانات\n\nالتنسيق المطلوب:\nA: اسم المنتج\nB: سعر البيع\nC: سعر الشراء");
                    return;
                }

                AppDatabase db = AppDatabase.getInstance(context);

                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (row == null || row.length == 0) continue;

                    try {
                        String name = getCell(row, 0);
                        if (name.isEmpty()) continue;
                        
                        // تجاهل صف العناوين
                        if (name.contains("اسم") || name.contains("Product") || name.contains("الاسم")) continue;

                        double sellPrice = parseDouble(getCell(row, 1));
                        if (sellPrice <= 0) {
                            failCount++;
                            errors.append("سعر بيع غير صالح: ").append(name).append("\n");
                            continue;
                        }

                        Product p = new Product();
                        p.setName(name);
                        p.setSellPrice(sellPrice);
                        p.setBuyPrice(parseDouble(getCell(row, 2)));
                        p.setStockQuantity((int) parseDouble(getCell(row, 3)));
                        p.setBaseUnitName(getCell(row, 4, "حبة"));
                        p.setBaseUnitId(1);
                        p.setMinStockAlert(Math.max((int) parseDouble(getCell(row, 5)), 5));
                        p.setTaxPercentage(parseDouble(getCell(row, 6)));
                        p.setCategoryId(1);
                        p.setCategoryName(getCell(row, 7, "عام"));
                        p.setBarcode(getCell(row, 8));
                        p.setNotes(getCell(row, 9));
                        p.setActive(true);

                        long id = db.productDao().insert(p);
                        if (id > 0) successCount++;
                        else failCount++;

                    } catch (Exception e) {
                        failCount++;
                        errors.append("خطأ: ").append(e.getMessage()).append("\n");
                    }
                }

                String msg = "✅ تم استيراد " + successCount + " منتج";
                if (failCount > 0) msg += "\n❌ فشل " + failCount + " منتج\n" + errors.toString();
                if (callback != null) callback.onComplete(successCount, failCount, msg);

            } catch (Exception e) {
                Log.e(TAG, "خطأ استيراد", e);
                if (callback != null) callback.onError("❌ خطأ: " + e.getMessage());
            }
        });
    }

    // ================================================================
    // تصدير
    // ================================================================
    public static void exportProductsToExcel(Context context, Uri fileUri, ExportCallback callback) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Product> products = db.productDao().getAllActiveForSync();

                if (products == null || products.isEmpty()) {
                    if (callback != null) callback.onError("❌ لا توجد منتجات للتصدير");
                    return;
                }

                OutputStream os = context.getContentResolver().openOutputStream(fileUri);
                if (os == null) {
                    if (callback != null) callback.onError("❌ لا يمكن إنشاء الملف");
                    return;
                }

                StringBuilder csv = new StringBuilder();
                csv.append("\uFEFF"); // BOM UTF-8 للعربية
                csv.append("اسم المنتج,سعر البيع,سعر الشراء,الكمية,العبوة,الحد الأدنى,الضريبة%,المجموعة,الباركود,ملاحظات\n");

                for (Product p : products) {
                    csv.append(escape(p.getName())).append(",");
                    csv.append(p.getSellPrice()).append(",");
                    csv.append(p.getBuyPrice()).append(",");
                    csv.append(p.getStockQuantity()).append(",");
                    csv.append(escape(p.getBaseUnitName())).append(",");
                    csv.append(p.getMinStockAlert()).append(",");
                    csv.append(p.getTaxPercentage()).append(",");
                    csv.append(escape(p.getCategoryName())).append(",");
                    csv.append(escape(p.getBarcode())).append(",");
                    csv.append(escape(p.getNotes())).append("\n");
                }

                os.write(csv.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                if (callback != null) callback.onComplete(products.size(),
                    "✅ تم تصدير " + products.size() + " منتج\n\nA: اسم المنتج\nB: سعر البيع\nC: سعر الشراء");

            } catch (Exception e) {
                Log.e(TAG, "خطأ تصدير", e);
                if (callback != null) callback.onError("❌ خطأ: " + e.getMessage());
            }
        });
    }

    // ================================================================
    // قراءة الملفات
    // ================================================================

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int n;
        while ((n = is.read(tmp)) != -1) buf.write(tmp, 0, n);
        return buf.toByteArray();
    }

    private static boolean isXlsx(byte[] data) {
        return data.length > 4 && data[0] == 0x50 && data[1] == 0x4B;
    }

    private static boolean isXls(byte[] data) {
        return data.length > 8 && data[0] == (byte) 0xD0 && data[1] == (byte) 0xCF;
    }

    private static List<String[]> readXlsx(byte[] data) {
        List<String[]> rows = new ArrayList<>();
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            ZipEntry entry;
            String sheetXml = null;
            Map<Integer, String> sharedStrings = new HashMap<>();

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("xl/sharedStrings.xml")) {
                    String xml = readEntry(zis);
                    int idx = 0, pos = 0;
                    while ((pos = xml.indexOf("<t", pos)) != -1) {
                        int s = xml.indexOf(">", pos) + 1;
                        int e = xml.indexOf("</t>", s);
                        if (s > 0 && e > s) sharedStrings.put(idx++, xml.substring(s, e));
                        pos = e + 5;
                    }
                } else if (name.startsWith("xl/worksheets/sheet1")) {
                    sheetXml = readEntry(zis);
                }
            }
            zis.close();

            if (sheetXml == null) return rows;

            String[] rowParts = sheetXml.split("<row ");
            for (int i = 1; i < rowParts.length; i++) {
                List<String> cells = new ArrayList<>();
                String[] cellParts = rowParts[i].split("<c ");
                for (int j = 1; j < cellParts.length; j++) {
                    String cell = cellParts[j];
                    String val = "";
                    if (cell.contains("<v>")) {
                        int vs = cell.indexOf("<v>") + 3;
                        int ve = cell.indexOf("</v>");
                        String v = cell.substring(vs, ve);
                        if (cell.contains(" t=\"s\"") || cell.contains(" t=\"str\"")) {
                            try { val = sharedStrings.getOrDefault(Integer.parseInt(v), v); }
                            catch (Exception ex) { val = v; }
                        } else {
                            val = v;
                        }
                    }
                    cells.add(val);
                }
                if (!cells.isEmpty()) rows.add(cells.toArray(new String[0]));
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ xlsx", e);
        }
        return rows;
    }

    private static List<String[]> readXls(Context context, Uri uri) {
        List<String[]> rows = new ArrayList<>();
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return rows;
            jxl.WorkbookSettings ws = new jxl.WorkbookSettings();
            ws.setEncoding("UTF-8");
            ws.setSuppressWarnings(true);
            jxl.Workbook wb = jxl.Workbook.getWorkbook(is, ws);
            jxl.Sheet sheet = wb.getSheet(0);
            for (int i = 0; i < sheet.getRows(); i++) {
                String[] row = new String[sheet.getColumns()];
                for (int j = 0; j < sheet.getColumns(); j++) {
                    row[j] = sheet.getCell(j, i).getContents();
                }
                rows.add(row);
            }
            wb.close();
            is.close();
        } catch (Exception ex) {
            Log.e(TAG, "خطأ xls", ex);
        }
        return rows;
    }

    private static List<String[]> readCsv(byte[] data) {
        List<String[]> rows = new ArrayList<>();
        try {
            String content = new String(data, "UTF-8");
            if (content.startsWith("\uFEFF")) content = content.substring(1);
            String sep = content.contains("\t") ? "\t" : ",";
            for (String line : content.split("\n")) {
                line = line.trim();
                if (!line.isEmpty()) rows.add(split(line, sep));
            }
        } catch (Exception ex) {
            Log.e(TAG, "خطأ CSV", ex);
        }
        return rows;
    }

    // ================================================================
    // دوال مساعدة
    // ================================================================

    private static String readEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = zis.read(buf)) > 0) bos.write(buf, 0, len);
        return new String(bos.toByteArray(), "UTF-8");
    }

    private static String getCell(String[] row, int idx) {
        return getCell(row, idx, "");
    }

    private static String getCell(String[] row, int idx, String def) {
        if (row == null || idx >= row.length) return def;
        String val = row[idx];
        return (val != null && !val.trim().isEmpty()) ? clean(val.trim()) : def;
    }

    private static double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        s = clean(s.trim()).replaceAll("[^0-9.\\-]", "");
        if (s.isEmpty() || s.equals("-") || s.equals(".")) return 0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String clean(String s) {
        if (s == null) return "";
        s = s.replaceAll("^[\"']|[\"']$", "");
        s = s.replace('١','1').replace('٢','2').replace('٣','3')
             .replace('٤','4').replace('٥','5').replace('٦','6')
             .replace('٧','7').replace('٨','8').replace('٩','9')
             .replace('٠','0');
        return s;
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private static String[] split(String line, String sep) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (String.valueOf(c).equals(sep) && !inQuotes) {
                result.add(cur.toString());
                cur = new StringBuilder();
            } else cur.append(c);
        }
        result.add(cur.toString());
        return result.toArray(new String[0]);
    }

    public interface ImportCallback {
        void onComplete(int successCount, int failCount, String message);
        void onError(String errorMessage);
    }

    public interface ExportCallback {
        void onComplete(int totalProducts, String message);
        void onError(String errorMessage);
    }
}
