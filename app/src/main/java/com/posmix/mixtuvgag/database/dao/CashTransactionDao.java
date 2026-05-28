package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.CashTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class CashTransactionDao {

    private final AppDatabase db;

    public CashTransactionDao(AppDatabase db) {
        this.db = db;
    }

    private CashTransaction fromCursor(Cursor c) {
        CashTransaction t = new CashTransaction();
        t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        t.setType(c.getInt(c.getColumnIndexOrThrow("type")));
        t.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
        t.setReferenceId(c.getInt(c.getColumnIndexOrThrow("reference_id")));
        t.setReferenceType(c.getString(c.getColumnIndexOrThrow("reference_type")));
        t.setDate(c.getLong(c.getColumnIndexOrThrow("date")));
        t.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        return t;
    }

    private ContentValues toValues(CashTransaction t) {
        ContentValues cv = new ContentValues();
        // Do not put ID for insert, but include it for update
        if (t.getId() > 0) cv.put("id", t.getId());
        cv.put("type", t.getType());
        cv.put("amount", t.getAmount());
        cv.put("reference_id", t.getReferenceId());
        cv.put("reference_type", t.getReferenceType());
        cv.put("date", t.getDate());
        cv.put("description", t.getDescription());
        return cv;
    }

    public long insert(CashTransaction t) {
        return db.getWritableDatabase().insert("cash_transactions", null, toValues(t));
    }

    public void update(CashTransaction t) {
        db.getWritableDatabase().update("cash_transactions", toValues(t), "id=?", new String[]{String.valueOf(t.getId())});
    }

    public void delete(CashTransaction t) {
        db.getWritableDatabase().delete("cash_transactions", "id=?", new String[]{String.valueOf(t.getId())});
    }

    public double getCurrentBalance() {
        Cursor c = db.getReadableDatabase().rawQuery(
            "SELECT SUM(CASE WHEN type=1 THEN amount ELSE -amount END) FROM cash_transactions", null);
        try {
            if (c.moveToFirst()) return c.getDouble(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public LiveData<List<CashTransaction>> getRecent() {
        MutableLiveData<List<CashTransaction>> liveData = new MutableLiveData<>();
        new Thread(() -> {
            Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM cash_transactions ORDER BY date DESC LIMIT 50", null);
            List<CashTransaction> list = new ArrayList<>();
            try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
            liveData.postValue(list);
        }).start();
        return liveData;
    }

    public List<CashTransaction> getAllSync() {
        List<CashTransaction> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
            "SELECT * FROM cash_transactions ORDER BY date DESC", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }
    
    public List<CashTransaction> getForCustomer(int customerId) {
        List<CashTransaction> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
            "SELECT * FROM cash_transactions WHERE reference_type = 'CUSTOMER_PAYMENT' AND reference_id = ? ORDER BY date DESC",
            new String[]{String.valueOf(customerId)});
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }


    public void clearAll() {
        db.getWritableDatabase().delete("cash_transactions", null, null);
    }
}