package com.posmix.mixtuvgag.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class InvoiceDao {

    private final AppDatabase db;

    public InvoiceDao(AppDatabase db) {
        this.db = db;
    }

    private Invoice fromCursor(Cursor c) {
        Invoice inv = new Invoice();
        inv.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        inv.setInvoiceNumber(c.getString(c.getColumnIndexOrThrow("invoice_number")));
        inv.setType(c.getInt(c.getColumnIndexOrThrow("type")));
        int custIdx = c.getColumnIndex("customer_id");
        if (custIdx != -1 && !c.isNull(custIdx)) inv.setCustomerId(c.getInt(custIdx));
        int suppIdx = c.getColumnIndex("supplier_id");
        if (suppIdx != -1 && !c.isNull(suppIdx)) inv.setSupplierId(c.getInt(suppIdx));
        inv.setDate(c.getLong(c.getColumnIndexOrThrow("date")));
        inv.setSubtotal(c.getDouble(c.getColumnIndexOrThrow("subtotal")));
        inv.setTaxAmount(c.getDouble(c.getColumnIndexOrThrow("tax_amount")));
        inv.setDiscount(c.getDouble(c.getColumnIndexOrThrow("discount")));
        inv.setTotal(c.getDouble(c.getColumnIndexOrThrow("total")));
        inv.setPaidAmount(c.getDouble(c.getColumnIndexOrThrow("paid_amount")));
        inv.setRemainingAmount(c.getDouble(c.getColumnIndexOrThrow("remaining_amount")));
        inv.setPaymentStatus(c.getInt(c.getColumnIndexOrThrow("payment_status")));
        inv.setPrinted(c.getInt(c.getColumnIndexOrThrow("printed")) == 1);
        return inv;
    }

    private ContentValues toValues(Invoice inv) {
        ContentValues cv = new ContentValues();
        cv.put("invoice_number", inv.getInvoiceNumber());
        cv.put("type", inv.getType());
        if (inv.getCustomerId() != null) cv.put("customer_id", inv.getCustomerId());
        else cv.putNull("customer_id");
        if (inv.getSupplierId() != null) cv.put("supplier_id", inv.getSupplierId());
        else cv.putNull("supplier_id");
        cv.put("date", inv.getDate());
        cv.put("subtotal", inv.getSubtotal());
        cv.put("tax_amount", inv.getTaxAmount());
        cv.put("discount", inv.getDiscount());
        cv.put("total", inv.getTotal());
        cv.put("paid_amount", inv.getPaidAmount());
        cv.put("remaining_amount", inv.getRemainingAmount());
        cv.put("payment_status", inv.getPaymentStatus());
        cv.put("printed", inv.isPrinted() ? 1 : 0);
        return cv;
    }

    // ==================== العمليات الأساسية ====================

    public long insert(Invoice inv) {
        return db.getWritableDatabase().insert("invoices", null, toValues(inv));
    }

    public void update(Invoice inv) {
        db.getWritableDatabase().update("invoices", toValues(inv), "id=?",
                new String[]{String.valueOf(inv.getId())});
    }

    public void delete(Invoice inv) {
        db.getWritableDatabase().delete("invoices", "id=?",
                new String[]{String.valueOf(inv.getId())});
    }

    public void insertItem(InvoiceItem item) {
        ContentValues cv = new ContentValues();
        cv.put("invoice_id", item.getInvoiceId());
        cv.put("product_id", item.getProductId());
        cv.put("product_name", item.getProductName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit_price", item.getUnitPrice());
        cv.put("tax_percentage", item.getTaxPercentage());
        cv.put("discount", item.getDiscount());
        cv.put("total", item.getTotalPrice());
        cv.put("notes", item.getNotes());
        db.getWritableDatabase().insert("invoice_items", null, cv);
    }

    public void updateItem(InvoiceItem item) {
        ContentValues cv = new ContentValues();
        cv.put("quantity", item.getQuantity());
        cv.put("unit_price", item.getUnitPrice());
        cv.put("total", item.getTotalPrice());
        if (item.getProductName() != null) cv.put("product_name", item.getProductName());
        cv.put("notes", item.getNotes());
        db.getWritableDatabase().update("invoice_items", cv, "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    public void deleteItem(InvoiceItem item) {
        db.getWritableDatabase().delete("invoice_items", "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    // ==================== استعلامات الفواتير ====================

    public Invoice getInvoiceByIdSync(int invoiceId) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE id=?",
                new String[]{String.valueOf(invoiceId)});
        try {
            if (c.moveToFirst()) return fromCursor(c);
        } finally {
            c.close();
        }
        return null;
    }

    public List<Invoice> getAllSync() {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices ORDER BY date DESC", null);
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public List<Invoice> getAllByTypeSync(int type) {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE type=? ORDER BY date DESC",
                new String[]{String.valueOf(type)});
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public List<Invoice> getByTypeBetweenSync(int type, long from, long to) {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE type=? AND date BETWEEN ? AND ? ORDER BY date DESC",
                new String[]{String.valueOf(type), String.valueOf(from), String.valueOf(to)});
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    // ==================== استعلامات العملاء ====================

    public List<Invoice> getInvoicesByCustomerIdSync(int customerId) {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE customer_id=? ORDER BY date DESC",
                new String[]{String.valueOf(customerId)});
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }
    
    // New method to get all credit/partial invoices for a customer (including fully paid credit ones)
    public List<Invoice> getCreditAndPartialInvoicesByCustomerSync(int customerId) {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE customer_id=? AND (payment_status=? OR payment_status=?) ORDER BY date DESC",
                new String[]{String.valueOf(customerId), String.valueOf(Invoice.STATUS_CREDIT), String.valueOf(Invoice.STATUS_PARTIAL)});
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public List<Invoice> getAllInvoicesByCustomerSync(int customerId) {
        return getInvoicesByCustomerIdSync(customerId);
    }

    public List<Invoice> getCreditInvoicesByCustomerSync(int customerId) {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE customer_id=? AND remaining_amount > 0 ORDER BY date DESC",
                new String[]{String.valueOf(customerId)});
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }
    
    // New LiveData method for credit invoices by customer
    public LiveData<List<Invoice>> getCreditInvoicesByCustomer(int customerId) {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            liveData.postValue(getCreditInvoicesByCustomerSync(customerId));
        });
        return liveData;
    }

    public double getTotalCreditByCustomer(int customerId) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(remaining_amount), 0) FROM invoices WHERE customer_id=? AND remaining_amount > 0",
                new String[]{String.valueOf(customerId)});
        try {
            if (c.moveToFirst()) return c.getDouble(0);
        } finally {
            c.close();
        }
        return 0;
    }

    // ==================== الفواتير الآجلة ====================

    public List<Invoice> getCreditInvoicesSync() {
        List<Invoice> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoices WHERE remaining_amount > 0 ORDER BY date DESC", null);
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public void recordPayment(int invoiceId, double paymentAmount) {
        Invoice invoice = getInvoiceByIdSync(invoiceId);
        if (invoice != null) {
            double newPaid = invoice.getPaidAmount() + paymentAmount;
            double newRemaining = invoice.getTotal() - newPaid;

            ContentValues cv = new ContentValues();
            cv.put("paid_amount", newPaid);
            cv.put("remaining_amount", Math.max(0, newRemaining));

            if (newRemaining <= 0) {
                cv.put("payment_status", Invoice.STATUS_CASH); // Fully paid
            } else if (newPaid > 0) {
                cv.put("payment_status", Invoice.STATUS_PARTIAL); // Partially paid
            } else {
                cv.put("payment_status", Invoice.STATUS_CREDIT); // Still credit (no payment or fully remaining)
            }

            db.getWritableDatabase().update("invoices", cv, "id=?",
                    new String[]{String.valueOf(invoiceId)});
        }
    }

    // ==================== عناصر الفواتير ====================

    public List<InvoiceItem> getItemsForInvoiceSync(int invoiceId) {
        List<InvoiceItem> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM invoice_items WHERE invoice_id=? ORDER BY id ASC",
                new String[]{String.valueOf(invoiceId)});
        try {
            while (c.moveToNext()) {
                InvoiceItem ii = new InvoiceItem();
                ii.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                ii.setInvoiceId(c.getInt(c.getColumnIndexOrThrow("invoice_id")));
                ii.setProductId(c.getInt(c.getColumnIndexOrThrow("product_id")));
                ii.setProductName(c.getString(c.getColumnIndexOrThrow("product_name")));
                ii.setQuantity(c.getDouble(c.getColumnIndexOrThrow("quantity")));
                ii.setUnitPrice(c.getDouble(c.getColumnIndexOrThrow("unit_price")));
                ii.setTaxPercentage(c.getDouble(c.getColumnIndexOrThrow("tax_percentage")));
                ii.setDiscount(c.getDouble(c.getColumnIndexOrThrow("discount")));
                ii.setTotal(c.getDouble(c.getColumnIndexOrThrow("total")));
                int notesIndex = c.getColumnIndex("notes");
                if (notesIndex != -1) {
                    ii.setNotes(c.getString(notesIndex));
                }
                int notesIndex = c.getColumnIndex("notes");
                if (notesIndex != -1) {
                    ii.setNotes(c.getString(notesIndex));
                }
                list.add(ii);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void recalculateInvoiceTotals(int invoiceId) {
        List<InvoiceItem> items = getItemsForInvoiceSync(invoiceId);
        Invoice invoice = getInvoiceByIdSync(invoiceId);
        if (invoice == null) return;

        double newSubtotal = 0;
        double newTaxAmount = 0;

        for (InvoiceItem item : items) {
            newSubtotal += item.getTotalPrice();
            newTaxAmount += (item.getUnitPrice() * item.getQuantity()) * (item.getTaxPercentage() / 100.0);
        }

        invoice.setSubtotal(newSubtotal);
        invoice.setTaxAmount(newTaxAmount);
        invoice.setTotal(newSubtotal + newTaxAmount - invoice.getDiscount());
        invoice.setRemainingAmount(invoice.getTotal() - invoice.getPaidAmount());

        update(invoice);
    }

    // ==================== LiveData للإحصائيات ====================

    public LiveData<List<Invoice>> getByType(int type) {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            liveData.postValue(getAllByTypeSync(type));
        });
        return liveData;
    }

    public LiveData<List<Invoice>> getByTypeBetween(int type, long from, long to) {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            liveData.postValue(getByTypeBetweenSync(type, from, to));
        });
        return liveData;
    }

    public LiveData<List<Invoice>> getInvoicesByCustomerId(int customerId) {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            liveData.postValue(getInvoicesByCustomerIdSync(customerId));
        });
        return liveData;
    }

    public LiveData<List<Invoice>> getRecent() {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Cursor c = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM invoices ORDER BY date DESC LIMIT 50", null);
            List<Invoice> list = new ArrayList<>();
            try {
                while (c.moveToNext()) list.add(fromCursor(c));
            } finally {
                c.close();
            }
            liveData.postValue(list);
        });
        return liveData;
    }

    // ==================== دوال إحصائية ====================

    public double getTotalByTypeBetween(int type, long from, long to) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(total),0) FROM invoices WHERE type=? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(type), String.valueOf(from), String.valueOf(to)});
        try {
            if (c.moveToFirst()) return c.getDouble(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public int getCountByTypeBetween(int type, long from, long to) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM invoices WHERE type=? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(type), String.valueOf(from), String.valueOf(to)});
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public void clearAll() {
        db.getWritableDatabase().delete("invoices", null, null);
        db.getWritableDatabase().delete("invoice_items", null, null);
    }
}