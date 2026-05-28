package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
public class CustomerDao {

    private final AppDatabase db;

    public CustomerDao(AppDatabase db) {
        this.db = db;
    }

    private Customer fromCursor(Cursor c) {
        Customer cu = new Customer();
        cu.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        cu.setName(c.getString(c.getColumnIndexOrThrow("name")));
        cu.setPhone(c.getString(c.getColumnIndexOrThrow("phone")));
        cu.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
        cu.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        cu.setCreditLimit(c.getDouble(c.getColumnIndexOrThrow("credit_limit")));
        cu.setCurrentBalance(c.getDouble(c.getColumnIndexOrThrow("current_balance")));
        return cu;
    }

    private ContentValues toValues(Customer cu) {
        ContentValues cv = new ContentValues();
        cv.put("name", cu.getName());
        cv.put("phone", cu.getPhone());
        cv.put("address", cu.getAddress());
        cv.put("email", cu.getEmail());
        cv.put("credit_limit", cu.getCreditLimit());
        cv.put("current_balance", cu.getCurrentBalance());
        return cv;
    }

    public long insert(Customer c) {
        return db.getWritableDatabase().insert("customers", null, toValues(c));
    }

    public void update(Customer c) {
        db.getWritableDatabase().update("customers", toValues(c), "id=?", new String[]{String.valueOf(c.getId())});
    }

    public void delete(Customer c) {
        db.getWritableDatabase().delete("customers", "id=?", new String[]{String.valueOf(c.getId())});
    }

    public Customer getByIdSync(int id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM customers WHERE id=?", new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) return fromCursor(c);
        } finally {
            c.close();
        }
        return null;
    }

    public List<Customer> getAllSync() {
        List<Customer> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM customers ORDER BY name", null);
        try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
        return list;
    }

    public LiveData<List<Customer>> getAll() {
        MutableLiveData<List<Customer>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Customer> list = getAllSync();
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Customer>> search(String q) {
        MutableLiveData<List<Customer>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            String like = "%" + q + "%";
            Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ?",
                new String[]{like, like});
            List<Customer> list = new ArrayList<>();
            try { while (c.moveToNext()) list.add(fromCursor(c)); } finally { c.close(); }
            liveData.postValue(list);
        });
        return liveData;
    }

    public void clearAll() {
        db.getWritableDatabase().delete("customers", null, null);
    }
}