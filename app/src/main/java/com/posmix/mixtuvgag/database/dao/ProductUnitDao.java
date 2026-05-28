package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.ProductUnit;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class ProductUnitDao {
    private final AppDatabase db;
    public ProductUnitDao(AppDatabase db) { this.db = db; }
    
    private ProductUnit fromCursor(Cursor c) {
        ProductUnit pu = new ProductUnit();
        pu.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        pu.setProductId(c.getInt(c.getColumnIndexOrThrow("product_id")));
        pu.setUnitId(c.getInt(c.getColumnIndexOrThrow("unit_id")));
        pu.setUnitName(c.getString(c.getColumnIndexOrThrow("unit_name")));
        pu.setQuantity(c.getInt(c.getColumnIndexOrThrow("quantity")));
        pu.setBarcode(c.getString(c.getColumnIndexOrThrow("barcode")));
        pu.setSellPrice(c.getDouble(c.getColumnIndexOrThrow("sell_price")));
        return pu;
    }
    
    private ContentValues toValues(ProductUnit pu) {
        ContentValues cv = new ContentValues();
        cv.put("product_id", pu.getProductId());
        cv.put("unit_id", pu.getUnitId());
        cv.put("unit_name", pu.getUnitName());
        cv.put("quantity", pu.getQuantity());
        cv.put("barcode", pu.getBarcode());
        cv.put("sell_price", pu.getSellPrice());
        return cv;
    }
    
    public long insert(ProductUnit pu) { return db.getWritableDatabase().insert("product_units", null, toValues(pu)); }
    public void update(ProductUnit pu) { db.getWritableDatabase().update("product_units", toValues(pu), "id=?", new String[]{String.valueOf(pu.getId())}); }
    public void delete(ProductUnit pu) { db.getWritableDatabase().delete("product_units", "id=?", new String[]{String.valueOf(pu.getId())}); }
    public void deleteByProduct(int productId) { db.getWritableDatabase().delete("product_units", "product_id=?", new String[]{String.valueOf(productId)}); }
    
    public List<ProductUnit> getForProduct(int productId) {
        List<ProductUnit> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM product_units WHERE product_id=? ORDER BY id", new String[]{String.valueOf(productId)});
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }

    public List<ProductUnit> getAllSync() {
        List<ProductUnit> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM product_units ORDER BY id", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }

    public void clearAll() {
        db.getWritableDatabase().delete("product_units", null, null);
    }
}