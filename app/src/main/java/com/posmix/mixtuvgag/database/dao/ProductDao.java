package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ProductDao {
    private final AppDatabase db;
    public ProductDao(AppDatabase db) { this.db = db; }

    private Product fromCursor(Cursor c) {
        Product p = new Product();
        p.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        p.setName(c.getString(c.getColumnIndexOrThrow("name")));
        p.setBarcode(c.getString(c.getColumnIndexOrThrow("barcode")));
        p.setCategoryId(c.getInt(c.getColumnIndexOrThrow("category_id")));
        p.setCategoryName(c.getString(c.getColumnIndexOrThrow("category_name")));
        p.setBaseUnitId(c.getInt(c.getColumnIndexOrThrow("base_unit_id")));
        p.setBaseUnitName(c.getString(c.getColumnIndexOrThrow("base_unit_name")));
        p.setBuyPrice(c.getDouble(c.getColumnIndexOrThrow("buy_price")));
        p.setSellPrice(c.getDouble(c.getColumnIndexOrThrow("sell_price")));
        p.setStockQuantity(c.getInt(c.getColumnIndexOrThrow("stock_quantity")));
        p.setMinStockAlert(c.getInt(c.getColumnIndexOrThrow("min_stock_alert")));
        p.setTaxPercentage(c.getDouble(c.getColumnIndexOrThrow("tax_percentage")));
        p.setActive(c.getInt(c.getColumnIndexOrThrow("is_active")) == 1);
        p.setNotes(c.getString(c.getColumnIndexOrThrow("notes")));
        return p;
    }

    private ContentValues toValues(Product p) {
        ContentValues cv = new ContentValues();
        if (p.getId() > 0) cv.put("id", p.getId());
        cv.put("name", p.getName());
        cv.put("barcode", p.getBarcode());
        cv.put("category_id", p.getCategoryId());
        cv.put("category_name", p.getCategoryName());
        cv.put("base_unit_id", p.getBaseUnitId());
        cv.put("base_unit_name", p.getBaseUnitName());
        cv.put("buy_price", p.getBuyPrice());
        cv.put("sell_price", p.getSellPrice());
        cv.put("stock_quantity", p.getStockQuantity());
        cv.put("min_stock_alert", p.getMinStockAlert());
        cv.put("tax_percentage", p.getTaxPercentage());
        cv.put("is_active", p.isActive() ? 1 : 0);
        cv.put("notes", p.getNotes());
        return cv;
    }

    public long insert(Product p) {
        // CONFLICT_REPLACE: إذا كان المنتج موجوداً بنفس الـ ID، يتم تحديثه ولا يتم تكراره
        return db.getWritableDatabase().insertWithOnConflict("products", null, toValues(p), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Product getById(int id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM products WHERE id=?", new String[]{String.valueOf(id)});
        try { if (c.moveToFirst()) return fromCursor(c); } finally { c.close(); }
        return null;
    }

    public LiveData<List<Product>> search(String q) {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            String l = "%" + q + "%";
            Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM products WHERE name LIKE ? OR barcode LIKE ?", new String[]{l, l});
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) list.add(fromCursor(c));
            c.close();
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Product>> getAllActive() {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM products ORDER BY name ASC", null);
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) list.add(fromCursor(c));
            c.close();
            data.postValue(list);
        });
        return data;
    }

    public List<Product> getAllActiveForSync() {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM products ORDER BY name ASC", null);
        List<Product> list = new ArrayList<>();
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public void clearAllProducts() { db.getWritableDatabase().delete("products", null, null); }
    public void update(Product p) { db.getWritableDatabase().update("products", toValues(p), "id=?", new String[]{String.valueOf(p.getId())}); }
    public void delete(Product p) { db.getWritableDatabase().delete("products", "id=?", new String[]{String.valueOf(p.getId())}); }
    public void increaseStock(int id, int q) { db.getWritableDatabase().execSQL("UPDATE products SET stock_quantity=stock_quantity+? WHERE id=?", new Object[]{q, id}); }
    public void decreaseStock(int id, int q) { db.getWritableDatabase().execSQL("UPDATE products SET stock_quantity=stock_quantity-? WHERE id=?", new Object[]{q, id}); }
}