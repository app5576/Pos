package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Unit;
import java.util.ArrayList;
import java.util.List;

public class UnitDao {
    private final AppDatabase db;
    public UnitDao(AppDatabase db) { this.db = db; }
    
    private Unit fromCursor(Cursor c) {
        Unit u = new Unit();
        u.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        u.setName(c.getString(c.getColumnIndexOrThrow("name")));
        u.setDefault(c.getInt(c.getColumnIndexOrThrow("is_default")) == 1);
        return u;
    }
    
    private ContentValues toValues(Unit u) {
        ContentValues cv = new ContentValues();
        if (u.getId() > 0) cv.put("id", u.getId());
        cv.put("name", u.getName());
        cv.put("is_default", u.isDefault() ? 1 : 0);
        return cv;
    }
    
    public long insert(Unit u) { 
        return db.getWritableDatabase().insertWithOnConflict("units", null, toValues(u), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Unit getById(int id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM units WHERE id=?", new String[]{String.valueOf(id)});
        try { if (c.moveToFirst()) return fromCursor(c); } finally { c.close(); }
        return null;
    }

    public void update(Unit u) { db.getWritableDatabase().update("units", toValues(u), "id=?", new String[]{String.valueOf(u.getId())}); }
    public void delete(Unit u) { db.getWritableDatabase().delete("units", "id=?", new String[]{String.valueOf(u.getId())}); }
    public List<Unit> getAllSync() {
        List<Unit> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM units ORDER BY name", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }
}