package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Category;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.models.Supplier;

import java.util.ArrayList;
import java.util.List;

/**
 * فئة محسّنة لإدارة المزامنة مع Firebase
 * تدعم مزامنة جميع الجداول الرئيسية
 */
public class EnhancedSyncManager {

    private static final String TAG = "EnhancedSyncManager";
    private final Context context;
    private final AppDatabase database;
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference databaseRef;
    private SyncListener syncListener;

    // واجهة للاستماع إلى أحداث المزامنة
    public interface SyncListener {
        void onSyncStarted();
        void onSyncProgress(String message);
        void onSyncSuccess(String message);
        void onSyncError(String errorMessage);
        void onSyncCompleted();
    }

    public EnhancedSyncManager(Context context, AppDatabase database) {
        this.context = context;
        this.database = database;
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.databaseRef = firebaseDatabase.getReference();
    }

    public void setSyncListener(SyncListener listener) {
        this.syncListener = listener;
    }

    /**
     * بدء المزامنة الكاملة
     */
    public void startFullSync() {
        if (syncListener != null) {
            syncListener.onSyncStarted();
        }

        // مزامنة المنتجات
        syncProducts();
        // مزامنة الفئات
        syncCategories();
        // مزامنة العملاء
        syncCustomers();
        // مزامنة الموردين
        syncSuppliers();
        // مزامنة الفواتير
        syncInvoices();
    }

    /**
     * مزامنة المنتجات من Firebase
     */
    private void syncProducts() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري مزامنة المنتجات...");
        }

        databaseRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    List<Product> products = new ArrayList<>();
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            products.add(product);
                        }
                    }

                    if (!products.isEmpty()) {
                        database.getWritableDatabase().beginTransaction();
                        try {
                            database.productDao().clearAllProducts();
                            for (Product product : products) {
                                database.productDao().insert(product);
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Products synced successfully: " + products.size());
                            if (syncListener != null) {
                                syncListener.onSyncProgress("تم مزامنة " + products.size() + " منتج");
                            }
                        } finally {
                            database.getWritableDatabase().endTransaction();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing products", e);
                    if (syncListener != null) {
                        syncListener.onSyncError("خطأ في مزامنة المنتجات: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Products sync cancelled: " + error.getMessage());
                if (syncListener != null) {
                    syncListener.onSyncError("تم إلغاء مزامنة المنتجات");
                }
            }
        });
    }

    /**
     * مزامنة الفئات من Firebase
     */
    private void syncCategories() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري مزامنة الفئات...");
        }

        databaseRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    List<Category> categories = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        Category category = categorySnapshot.getValue(Category.class);
                        if (category != null) {
                            categories.add(category);
                        }
                    }

                    if (!categories.isEmpty()) {
                        database.getWritableDatabase().beginTransaction();
                        try {
                            database.categoryDao().clearAll(); // Changed from clearAllCategories()
                            for (Category category : categories) {
                                database.categoryDao().insert(category);
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Categories synced successfully: " + categories.size());
                            if (syncListener != null) {
                                syncListener.onSyncProgress("تم مزامنة " + categories.size() + " فئة");
                            }
                        } finally {
                            database.getWritableDatabase().endTransaction();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing categories", e);
                    if (syncListener != null) {
                        syncListener.onSyncError("خطأ في مزامنة الفئات: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Categories sync cancelled: " + error.getMessage());
                if (syncListener != null) {
                    syncListener.onSyncError("تم إلغاء مزامنة الفئات");
                }
            }
        });
    }

    /**
     * مزامنة العملاء من Firebase
     */
    private void syncCustomers() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري مزامنة العملاء...");
        }

        databaseRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    List<Customer> customers = new ArrayList<>();
                    for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
                        Customer customer = customerSnapshot.getValue(Customer.class);
                        if (customer != null) {
                            customers.add(customer);
                        }
                    }

                    if (!customers.isEmpty()) {
                        database.getWritableDatabase().beginTransaction();
                        try {
                            database.customerDao().clearAll(); // Changed from clearAllCustomers()
                            for (Customer customer : customers) {
                                database.customerDao().insert(customer);
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Customers synced successfully: " + customers.size());
                            if (syncListener != null) {
                                syncListener.onSyncProgress("تم مزامنة " + customers.size() + " عميل");
                            }
                        } finally {
                            database.getWritableDatabase().endTransaction();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing customers", e);
                    if (syncListener != null) {
                        syncListener.onSyncError("خطأ في مزامنة العملاء: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Customers sync cancelled: " + error.getMessage());
                if (syncListener != null) {
                    syncListener.onSyncError("تم إلغاء مزامنة العملاء");
                }
            }
        });
    }

    /**
     * مزامنة الموردين من Firebase
     */
    private void syncSuppliers() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري مزامنة الموردين...");
        }

        databaseRef.child("suppliers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    List<Supplier> suppliers = new ArrayList<>();
                    for (DataSnapshot supplierSnapshot : snapshot.getChildren()) {
                        Supplier supplier = supplierSnapshot.getValue(Supplier.class);
                        if (supplier != null) {
                            suppliers.add(supplier);
                        }
                    }

                    if (!suppliers.isEmpty()) {
                        database.getWritableDatabase().beginTransaction();
                        try {
                            database.supplierDao().clearAll(); // Changed from clearAllSuppliers()
                            for (Supplier supplier : suppliers) {
                                database.supplierDao().insert(supplier);
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Suppliers synced successfully: " + suppliers.size());
                            if (syncListener != null) {
                                syncListener.onSyncProgress("تم مزامنة " + suppliers.size() + " مورد");
                            }
                        } finally {
                            database.getWritableDatabase().endTransaction();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing suppliers", e);
                    if (syncListener != null) {
                        syncListener.onSyncError("خطأ في مزامنة الموردين: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Suppliers sync cancelled: " + error.getMessage());
                if (syncListener != null) {
                    syncListener.onSyncError("تم إلغاء مزامنة الموردين");
                }
            }
        });
    }

    /**
     * مزامنة الفواتير من Firebase
     */
    private void syncInvoices() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري مزامنة الفواتير...");
        }

        databaseRef.child("invoices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    List<Invoice> invoices = new ArrayList<>();
                    for (DataSnapshot invoiceSnapshot : snapshot.getChildren()) {
                        Invoice invoice = invoiceSnapshot.getValue(Invoice.class);
                        if (invoice != null) {
                            invoices.add(invoice);
                        }
                    }

                    if (!invoices.isEmpty()) {
                        database.getWritableDatabase().beginTransaction();
                        try {
                            // No clearAll() for invoices here, as it might delete items as well.
                            // Assuming a more granular sync for invoices or they are handled by FullSyncManager.
                            for (Invoice invoice : invoices) {
                                database.invoiceDao().insert(invoice);
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Invoices synced successfully: " + invoices.size());
                            if (syncListener != null) {
                                syncListener.onSyncProgress("تم مزامنة " + invoices.size() + " فاتورة");
                                syncListener.onSyncSuccess("تمت المزامنة بنجاح!");
                                syncListener.onSyncCompleted();
                            }
                        } finally {
                            database.getWritableDatabase().endTransaction();
                        }
                    } else {
                        if (syncListener != null) {
                            syncListener.onSyncSuccess("تمت المزامنة بنجاح!");
                            syncListener.onSyncCompleted();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing invoices", e);
                    if (syncListener != null) {
                        syncListener.onSyncError("خطأ في مزامنة الفواتير: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Invoices sync cancelled: " + error.getMessage());
                if (syncListener != null) {
                    syncListener.onSyncError("تم إلغاء مزامنة الفواتير");
                }
            }
        });
    }

    /**
     * رفع البيانات المحلية إلى Firebase
     */
    public void uploadLocalDataToFirebase() {
        if (syncListener != null) {
            syncListener.onSyncProgress("جاري رفع البيانات...");
        }

        try {
            // رفع المنتجات
            List<Product> products = database.productDao().getAllActiveForSync(); // Changed from getAllProducts()
            if (!products.isEmpty()) {
                databaseRef.child("products").setValue(products);
            }

            // رفع الفواتير
            List<Invoice> invoices = database.invoiceDao().getAllSync(); // Changed from getAllInvoices()
            if (!invoices.isEmpty()) {
                databaseRef.child("invoices").setValue(invoices);
            }

            if (syncListener != null) {
                syncListener.onSyncSuccess("تم رفع البيانات بنجاح!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error uploading data to Firebase", e);
            if (syncListener != null) {
                syncListener.onSyncError("خطأ في رفع البيانات: " + e.getMessage());
            }
        }
    }
}