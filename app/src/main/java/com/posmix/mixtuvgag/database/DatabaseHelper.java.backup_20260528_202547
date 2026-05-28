package com.posmix.mixtuvgag.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "micropos_db";
    private static final int DB_VERSION = 3; // رفع الإصدار لإضافة عمود notes في invoice_items
    private static volatile DatabaseHelper INSTANCE;

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        // جدول المنتجات (مع الحقول الجديدة)
        // تم دمج "category" في "category_name" لتجنب التكرار
        db.execSQL("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "barcode TEXT," +
                "category_id INTEGER DEFAULT 0," +
                "category_name TEXT," +
                "base_unit_id INTEGER DEFAULT 0," +
                "base_unit_name TEXT," +
                "buy_price REAL DEFAULT 0," +
                "sell_price REAL DEFAULT 0," +
                "stock_quantity INTEGER DEFAULT 0," +
                "min_stock_alert INTEGER DEFAULT 5," +
                "tax_percentage REAL DEFAULT 0," +
                "is_active INTEGER DEFAULT 1," +
                "notes TEXT)");

        // جدول العملاء
        db.execSQL("CREATE TABLE IF NOT EXISTS customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "phone TEXT," +
                "address TEXT," +
                "email TEXT," +
                "credit_limit REAL DEFAULT 10000," +
                "current_balance REAL DEFAULT 0)");

        // جدول الموردين
        db.execSQL("CREATE TABLE IF NOT EXISTS suppliers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "phone TEXT," +
                "address TEXT," +
                "current_balance REAL DEFAULT 0)");

        // جدول الفواتير
        db.execSQL("CREATE TABLE IF NOT EXISTS invoices (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "invoice_number TEXT," +
                "type INTEGER," +
                "customer_id INTEGER," +
                "supplier_id INTEGER," +
                "date INTEGER," +
                "subtotal REAL DEFAULT 0," +
                "tax_amount REAL DEFAULT 0," +
                "discount REAL DEFAULT 0," +
                "total REAL DEFAULT 0," +
                "paid_amount REAL DEFAULT 0," +
                "remaining_amount REAL DEFAULT 0," +
                "payment_status INTEGER DEFAULT 1," +
                "printed INTEGER DEFAULT 0," +
                "FOREIGN KEY(customer_id) REFERENCES customers(id)," +
                "FOREIGN KEY(supplier_id) REFERENCES suppliers(id))");

        // جدول عناصر الفاتورة
        db.execSQL("CREATE TABLE IF NOT EXISTS invoice_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "invoice_id INTEGER," +
                "product_id INTEGER," +
                "product_name TEXT," +
                "quantity REAL DEFAULT 0," +
                "unit_price REAL DEFAULT 0," +
                "tax_percentage REAL DEFAULT 0," +
                "discount REAL DEFAULT 0," +
                "total REAL DEFAULT 0," +
                "notes TEXT," + // Added notes column
                "FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE," +
                "FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");

        // جدول معاملات الصندوق
        db.execSQL("CREATE TABLE IF NOT EXISTS cash_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type INTEGER," +
                "amount REAL DEFAULT 0," +
                "reference_id INTEGER DEFAULT 0," +
                "reference_type TEXT," +
                "date INTEGER," +
                "description TEXT)");

        // جدول المصروفات
        db.execSQL("CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT," +
                "amount REAL DEFAULT 0," +
                "date INTEGER," +
                "notes TEXT)");

        // جدول المجموعات (categories)
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT)");

        // جدول الوحدات الأساسية (units)
        db.execSQL("CREATE TABLE IF NOT EXISTS units (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "is_default INTEGER DEFAULT 0)");

        // جدول وحدات المنتج (product_units)
        db.execSQL("CREATE TABLE IF NOT EXISTS product_units (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "product_id INTEGER," +
                "unit_id INTEGER," +
                "unit_name TEXT," +
                "quantity INTEGER," +
                "barcode TEXT," +
                "sell_price REAL," +
                "FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE," +
                "FOREIGN KEY(unit_id) REFERENCES units(id))");
                
        // إضافة بيانات افتراضية للوحدات الأساسية إذا كانت فارغة
        insertDefaultData(db);
    }
    
    private void insertDefaultData(SQLiteDatabase db) {
        // إضافة وحدات أساسية افتراضية
        db.execSQL("INSERT OR IGNORE INTO units (id, name, is_default) VALUES (1, 'حبة', 1)");
        db.execSQL("INSERT OR IGNORE INTO units (id, name, is_default) VALUES (2, 'كرتون', 0)");
        db.execSQL("INSERT OR IGNORE INTO units (id, name, is_default) VALUES (3, 'كيلو', 0)");
        db.execSQL("INSERT OR IGNORE INTO units (id, name, is_default) VALUES (4, 'لتر', 0)");
        db.execSQL("INSERT OR IGNORE INTO units (id, name, is_default) VALUES (5, 'علبة', 0)");
        
        // إضافة مجموعات افتراضية
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, description) VALUES (1, 'عام', 'منتجات عامة')");
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, description) VALUES (2, 'إلكترونيات', 'أجهزة إلكترونية')");
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, description) VALUES (3, 'ملابس', 'ملابس وأزياء')");
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, description) VALUES (4, 'مواد غذائية', 'أطعمة ومشروبات')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // إضافة الجداول الجديدة
            db.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT)");
                    
            db.execSQL("CREATE TABLE IF NOT EXISTS units (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "is_default INTEGER DEFAULT 0)");
                    
            db.execSQL("CREATE TABLE IF NOT EXISTS product_units (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "product_id INTEGER," +
                    "unit_id INTEGER," +
                    "unit_name TEXT," +
                    "quantity INTEGER," +
                    "barcode TEXT," +
                    "sell_price REAL," +
                    "FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(unit_id) REFERENCES units(id))");
            
            // إضافة الأعمدة الجديدة إلى جدول products إذا لم تكن موجودة
            // تم تحديث هذا القسم لإزالة "category" واستخدام "category_name"
            try {
                db.execSQL("ALTER TABLE products ADD COLUMN category_id INTEGER DEFAULT 0");
            } catch (Exception e) { /* Column already exists */ }
            try {
                db.execSQL("ALTER TABLE products ADD COLUMN category_name TEXT");
            } catch (Exception e) { /* Column already exists */ }
            try {
                db.execSQL("ALTER TABLE products ADD COLUMN base_unit_id INTEGER DEFAULT 0");
            } catch (Exception e) { /* Column already exists */ }
            try {
                db.execSQL("ALTER TABLE products ADD COLUMN base_unit_name TEXT");
            } catch (Exception e) { /* Column already exists */ }
            try {
                db.execSQL("ALTER TABLE products ADD COLUMN notes TEXT");
            } catch (Exception e) { /* Column already exists */ }
            
            // محاولة إزالة العمود القديم 'category' إذا كان موجودًا،
            // ولكن SQLite لا يدعم DROP COLUMN مباشرة قبل API 30.
            // الحل الأكثر أمانًا هو إعادة إنشاء الجدول أو تجاهله
            // طالما أننا لا نستخدمه في الكود الجديد.
            // for example: if (oldVersion < 3) { db.execSQL("ALTER TABLE products DROP COLUMN category"); }
            // For now, we'll just ensure new columns are there and the DAO uses them.

            insertDefaultData(db);
        }
        if (oldVersion < 3) {
            // Add notes column to invoice_items
            try {
                db.execSQL("ALTER TABLE invoice_items ADD COLUMN notes TEXT");
            } catch (Exception e) { /* Column already exists, or other issue */ }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }
}