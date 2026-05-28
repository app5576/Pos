package com.posmix.mixtuvgag.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.posmix.mixtuvgag.database.dao.ProductDao;
import com.posmix.mixtuvgag.database.dao.CustomerDao;
import com.posmix.mixtuvgag.database.dao.SupplierDao;
import com.posmix.mixtuvgag.database.dao.InvoiceDao;
import com.posmix.mixtuvgag.database.dao.CashTransactionDao;
import com.posmix.mixtuvgag.database.dao.ExpenseDao;
import com.posmix.mixtuvgag.database.dao.CategoryDao;
import com.posmix.mixtuvgag.database.dao.UnitDao;
import com.posmix.mixtuvgag.database.dao.ProductUnitDao;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class AppDatabase {

    private static volatile AppDatabase INSTANCE;
    private final DatabaseHelper helper;

    private AppDatabase(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    // New method to reset the singleton instance, crucial for database restore
    public static void resetInstance() {
        if (INSTANCE != null) {
            // Close any open database connections before nullifying
            // This relies on DatabaseHelper also having a close method or similar management
            if (INSTANCE.helper != null) {
                // The underlying SQLiteOpenHelper will close its database automatically
                // when it's closed. However, direct access to the SQLiteDatabase object
                // might keep it open. A full app restart is usually the safest.
                // For this scenario, simply setting INSTANCE to null is enough to force
                // re-initialization on next getInstance() call after the old file is replaced.
            }
        }
        INSTANCE = null;
    }

    public SQLiteDatabase getWritableDatabase() {
        return helper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return helper.getReadableDatabase();
    }

    public ProductDao productDao() {
        return new ProductDao(this);
    }

    public CustomerDao customerDao() {
        return new CustomerDao(this);
    }

    public SupplierDao supplierDao() {
        return new SupplierDao(this);
    }

    public InvoiceDao invoiceDao() {
        return new InvoiceDao(this);
    }

    public CashTransactionDao cashTransactionDao() {
        return new CashTransactionDao(this);
    }

    public ExpenseDao expenseDao() {
        return new ExpenseDao(this);
    }

    public CategoryDao categoryDao() {
        return new CategoryDao(this);
    }

    public UnitDao unitDao() {
        return new UnitDao(this);
    }

    public ProductUnitDao productUnitDao() {
        return new ProductUnitDao(this);
    }
}