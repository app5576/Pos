package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Expense;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class ExpenseDao {

    private final AppDatabase db;

    public ExpenseDao(AppDatabase db) {
        this.db = db;
    }

    private Expense fromCursor(Cursor c) {
        Expense e = new Expense();
        e.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        e.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        e.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
        e.setDate(c.getLong(c.getColumnIndexOrThrow("date")));
        e.setNotes(c.getString(c.getColumnIndexOrThrow("notes")));
        return e;
    }

    private ContentValues toValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put("category", e.getCategory());
        cv.put("amount", e.getAmount());
        cv.put("date", e.getDate());
        cv.put("notes", e.getNotes());
        return cv;
    }

    public long insert(Expense e) {
        return db.getWritableDatabase().insert("expenses", null, toValues(e));
    }

    public void update(Expense e) {
        db.getWritableDatabase().update("expenses", toValues(e), "id=?", new String[]{String.valueOf(e.getId())});
    }

    public void delete(Expense e) {
        db.getWritableDatabase().delete("expenses", "id=?", new String[]{String.valueOf(e.getId())});
    }

    public List<Expense> getAllSync() {
        List<Expense> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM expenses ORDER BY date DESC", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }

    public LiveData<List<Expense>> getAll() {
        MutableLiveData<List<Expense>> liveData = new MutableLiveData<>();
        new Thread(() -> {
            List<Expense> list = getAllSync();
            liveData.postValue(list);
        }).start();
        return liveData;
    }

    public double getTotalBetween(long from, long to) {
        Cursor c = db.getReadableDatabase().rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE date BETWEEN ? AND ?",
            new String[]{String.valueOf(from), String.valueOf(to)});
        try {
            if (c.moveToFirst()) return c.getDouble(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public void clearAll() {
        db.getWritableDatabase().delete("expenses", null, null);
    }
}