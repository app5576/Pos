package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final AppDatabase db;
    public CategoryDao(AppDatabase db) { this.db = db; }
    
    private Category fromCursor(Cursor c) {
        Category cat = new Category();
        cat.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        cat.setName(c.getString(c.getColumnIndexOrThrow("name")));
        cat.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        return cat;
    }
    
    private ContentValues toValues(Category cat) {
        ContentValues cv = new ContentValues();
        if (cat.getId() > 0) cv.put("id", cat.getId());
        cv.put("name", cat.getName());
        cv.put("description", cat.getDescription());
        return cv;
    }
    
    public long insert(Category cat) { 
        return db.getWritableDatabase().insertWithOnConflict("categories", null, toValues(cat), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Category getById(int id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM categories WHERE id=?", new String[]{String.valueOf(id)});
        try { if (c.moveToFirst()) return fromCursor(c); } finally { c.close(); }
        return null;
    }

    public void clearAll() { db.getWritableDatabase().delete("categories", null, null); }
    public void update(Category cat) { db.getWritableDatabase().update("categories", toValues(cat), "id=?", new String[]{String.valueOf(cat.getId())}); }
    public void delete(Category cat) { db.getWritableDatabase().delete("categories", "id=?", new String[]{String.valueOf(cat.getId())}); }
    public List<Category> getAllSync() {
        List<Category> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM categories ORDER BY name", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }
}