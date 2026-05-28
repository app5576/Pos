package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Supplier;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class SupplierDao {

    private final AppDatabase db;

    public SupplierDao(AppDatabase db) {
        this.db = db;
    }

    private Supplier fromCursor(Cursor c) {
        Supplier s = new Supplier();
        s.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        s.setName(c.getString(c.getColumnIndexOrThrow("name")));
        s.setPhone(c.getString(c.getColumnIndexOrThrow("phone")));
        s.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
        s.setCurrentBalance(c.getDouble(c.getColumnIndexOrThrow("current_balance")));
        return s;
    }

    private ContentValues toValues(Supplier s) {
        ContentValues cv = new ContentValues();
        cv.put("name", s.getName());
        cv.put("phone", s.getPhone());
        cv.put("address", s.getAddress());
        cv.put("current_balance", s.getCurrentBalance());
        return cv;
    }

    public long insert(Supplier s) {
        return db.getWritableDatabase().insert("suppliers", null, toValues(s));
    }

    public void update(Supplier s) {
        db.getWritableDatabase().update("suppliers", toValues(s), "id=?", new String[]{String.valueOf(s.getId())});
    }

    public void delete(Supplier s) {
        db.getWritableDatabase().delete("suppliers", "id=?", new String[]{String.valueOf(s.getId())});
    }

    public Supplier getByIdSync(int id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM suppliers WHERE id=?", new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) return fromCursor(c);
        } finally {
            c.close();
        }
        return null;
    }

    public List<Supplier> getAllSync() {
        List<Supplier> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM suppliers ORDER BY name", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }

    public LiveData<List<Supplier>> getAll() {
        MutableLiveData<List<Supplier>> liveData = new MutableLiveData<>();
        new Thread(() -> {
            List<Supplier> list = getAllSync();
            liveData.postValue(list);
        }).start();
        return liveData;
    }

    public LiveData<List<Supplier>> search(String q) {
        MutableLiveData<List<Supplier>> liveData = new MutableLiveData<>();
        new Thread(() -> {
            String like = "%" + q + "%";
            Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM suppliers WHERE name LIKE ? OR phone LIKE ?",
                new String[]{like, like});
            List<Supplier> list = new ArrayList<>();
            try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
            liveData.postValue(list);
        }).start();
        return liveData;
    }

    public void clearAll() {
        db.getWritableDatabase().delete("suppliers", null, null);
    }
}