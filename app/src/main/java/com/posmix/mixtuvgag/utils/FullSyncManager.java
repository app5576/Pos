
package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * نظام مزامنة ذكي ثنائي الاتجاه
 * 
 * المميزات:
 * - لا يحذف البيانات المحلية أبداً
 * - يدمج البيانات الجديدة من Firebase مع المحلية
 * - يرفع البيانات المحلية الجديدة إلى Firebase
 * - يحافظ على البيانات في كلا الجانبين
 */
public class FullSyncManager {
    private static final String TAG = "FullSyncManager";
    private final AppDatabase db;
    private final DatabaseReference rootRef;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String lastSyncTime = "لم تتم المزامنة بعد";

    public interface SyncCallback {
        void onSyncStart();
        void onSyncProgress(String message);
        void onSyncComplete(String message);
        void onSyncError(String error);
    }

    public FullSyncManager(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.rootRef = FirebaseDatabase.getInstance().getReference("app_data");
    }

    public String getLastSyncTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));
        return "آخر مزامنة: " + lastSyncTime;
    }

    /**
     * بدء المزامنة الذكية - ضغطة واحدة
     */
    public void syncAllData(SyncCallback callback) {
        callback.onSyncStart();
        executor.execute(() -> {
            try {
                // ============================================
                // الخطوة 1: رفع البيانات المحلية إلى Firebase أولاً
                // ============================================
                callback.onSyncProgress("⬆️ رفع المنتجات المحلية...");
                uploadLocalProducts();

                callback.onSyncProgress("⬆️ رفع العملاء المحليين...");
                uploadLocalCustomers();

                callback.onSyncProgress("⬆️ رفع الموردين المحليين...");
                uploadLocalSuppliers();

                callback.onSyncProgress("⬆️ رفع الفئات...");
                uploadLocalCategories();

                callback.onSyncProgress("⬆️ رفع الوحدات...");
                uploadLocalUnits();

                callback.onSyncProgress("⬆️ رفع الفواتير...");
                uploadLocalInvoices();

                // ============================================
                // الخطوة 2: تنزيل البيانات من Firebase ودمجها
                // ============================================
                callback.onSyncProgress("⬇️ مزامنة المنتجات...");
                syncProductsDown();

                callback.onSyncProgress("⬇️ مزامنة العملاء...");
                syncCustomersDown();

                callback.onSyncProgress("⬇️ مزامنة الموردين...");
                syncSuppliersDown();

                callback.onSyncProgress("⬇️ مزامنة الفئات...");
                syncCategoriesDown();

                callback.onSyncProgress("⬇️ مزامنة الوحدات...");
                syncUnitsDown();

                callback.onSyncProgress("⬇️ مزامنة الفواتير...");
                syncInvoicesDown();

                // ============================================
                // الخطوة 3: تحديث الأرصدة
                // ============================================
                callback.onSyncProgress("💰 تحديث الأرصدة...");
                updateAllBalances();

                // تحديث وقت المزامنة
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("ar"));
                lastSyncTime = sdf.format(new Date());

                uiHandler.post(() -> callback.onSyncComplete("✅ تمت المزامنة بنجاح!\nتم رفع وتنزيل جميع البيانات"));

            } catch (Exception e) {
                Log.e(TAG, "خطأ في المزامنة", e);
                uiHandler.post(() -> callback.onSyncError("❌ خطأ: " + e.getMessage()));
            }
        });
    }

    // ================================================================
    // دوال الرفع إلى Firebase
    // ================================================================

    private void uploadLocalProducts() {
        try {
            List<Product> products = db.productDao().getAllActiveForSync();
            if (products != null && !products.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                for (Product p : products) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", p.getId());
                    data.put("name", p.getName() != null ? p.getName() : "");
                    data.put("barcode", p.getBarcode() != null ? p.getBarcode() : "");
                    data.put("buyPrice", p.getBuyPrice());
                    data.put("sellPrice", p.getSellPrice());
                    data.put("stockQuantity", p.getStockQuantity());
                    data.put("minStockAlert", p.getMinStockAlert());
                    data.put("taxPercentage", p.getTaxPercentage());
                    data.put("isActive", p.isActive());
                    data.put("categoryId", p.getCategoryId());
                    data.put("categoryName", p.getCategoryName() != null ? p.getCategoryName() : "");
                    data.put("baseUnitId", p.getBaseUnitId());
                    data.put("baseUnitName", p.getBaseUnitName() != null ? p.getBaseUnitName() : "");
                    data.put("notes", p.getNotes() != null ? p.getNotes() : "");
                    map.put(String.valueOf(p.getId()), data);
                }
                rootRef.child("products").updateChildren(map);
                Log.d(TAG, "⬆️ رفع " + products.size() + " منتج");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع المنتجات", e);
        }
    }

    private void uploadLocalCustomers() {
        try {
            List<Customer> customers = db.customerDao().getAllSync();
            if (customers != null && !customers.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                for (Customer c : customers) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", c.getId());
                    data.put("name", c.getName() != null ? c.getName() : "");
                    data.put("phone", c.getPhone() != null ? c.getPhone() : "");
                    data.put("address", c.getAddress() != null ? c.getAddress() : "");
                    data.put("email", c.getEmail() != null ? c.getEmail() : "");
                    data.put("creditLimit", c.getCreditLimit());
                    data.put("currentBalance", c.getCurrentBalance());
                    map.put(String.valueOf(c.getId()), data);
                }
                rootRef.child("customers").updateChildren(map);
                Log.d(TAG, "⬆️ رفع " + customers.size() + " عميل");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع العملاء", e);
        }
    }

    private void uploadLocalSuppliers() {
        try {
            List<Supplier> suppliers = db.supplierDao().getAllSync();
            if (suppliers != null && !suppliers.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                for (Supplier s : suppliers) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", s.getId());
                    data.put("name", s.getName() != null ? s.getName() : "");
                    data.put("phone", s.getPhone() != null ? s.getPhone() : "");
                    data.put("address", s.getAddress() != null ? s.getAddress() : "");
                    data.put("currentBalance", s.getCurrentBalance());
                    map.put(String.valueOf(s.getId()), data);
                }
                rootRef.child("suppliers").updateChildren(map);
                Log.d(TAG, "⬆️ رفع " + suppliers.size() + " مورد");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع الموردين", e);
        }
    }

    private void uploadLocalCategories() {
        try {
            List<Category> categories = db.categoryDao().getAllSync();
            if (categories != null && !categories.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                for (Category c : categories) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", c.getId());
                    data.put("name", c.getName() != null ? c.getName() : "");
                    data.put("description", c.getDescription() != null ? c.getDescription() : "");
                    map.put(String.valueOf(c.getId()), data);
                }
                rootRef.child("categories").updateChildren(map);
                Log.d(TAG, "⬆️ رفع " + categories.size() + " فئة");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع الفئات", e);
        }
    }

    private void uploadLocalUnits() {
        try {
            List<Unit> units = db.unitDao().getAllSync();
            if (units != null && !units.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                for (Unit u : units) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", u.getId());
                    data.put("name", u.getName() != null ? u.getName() : "");
                    data.put("isDefault", u.isDefault());
                    map.put(String.valueOf(u.getId()), data);
                }
                rootRef.child("units").updateChildren(map);
                Log.d(TAG, "⬆️ رفع " + units.size() + " وحدة");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع الوحدات", e);
        }
    }

    private void uploadLocalInvoices() {
        try {
            List<Invoice> invoices = db.invoiceDao().getAllSync();
            if (invoices != null && !invoices.isEmpty()) {
                for (Invoice inv : invoices) {
                    Map<String, Object> invData = new HashMap<>();
                    invData.put("id", inv.getId());
                    invData.put("invoiceNumber", inv.getInvoiceNumber());
                    invData.put("type", inv.getType());
                    invData.put("customerId", inv.getCustomerId());
                    invData.put("supplierId", inv.getSupplierId());
                    invData.put("date", inv.getDate());
                    invData.put("subtotal", inv.getSubtotal());
                    invData.put("taxAmount", inv.getTaxAmount());
                    invData.put("discount", inv.getDiscount());
                    invData.put("total", inv.getTotal());
                    invData.put("paidAmount", inv.getPaidAmount());
                    invData.put("remainingAmount", inv.getRemainingAmount());
                    invData.put("paymentStatus", inv.getPaymentStatus());

                    // عناصر الفاتورة
                    List<InvoiceItem> items = db.invoiceDao().getItemsForInvoiceSync(inv.getId());
                    if (items != null && !items.isEmpty()) {
                        List<Map<String, Object>> itemsList = new ArrayList<>();
                        for (InvoiceItem item : items) {
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("id", item.getId());
                            itemData.put("productId", item.getProductId());
                            itemData.put("productName", item.getProductName() != null ? item.getProductName() : "");
                            itemData.put("quantity", item.getQuantity());
                            itemData.put("unitPrice", item.getUnitPrice());
                            itemData.put("taxPercentage", item.getTaxPercentage());
                            itemData.put("discount", item.getDiscount());
                            itemData.put("total", item.getTotalPrice());
                            itemsList.add(itemData);
                        }
                        invData.put("items", itemsList);
                    }

                    rootRef.child("invoices").child(String.valueOf(inv.getId())).updateChildren(invData);
                }
                Log.d(TAG, "⬆️ رفع " + invoices.size() + " فاتورة");
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ رفع الفواتير", e);
        }
    }

    // ================================================================
    // دوال التنزيل من Firebase (دمج - لا حذف)
    // ================================================================

    private void syncProductsDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Product remote = child.getValue(Product.class);
                            if (remote == null || remote.getName() == null || remote.getName().isEmpty()) continue;
                            
                            Product local = db.productDao().getById(remote.getId());
                            if (local == null) {
                                // منتج جديد - إضافته محلياً
                                db.productDao().insert(remote);
                            } else {
                                // موجود محلياً - دمج (الاحتفاظ بالأحدث)
                                // تحديث فقط إذا كانت البيانات المحلية قديمة
                                if (remote.getSellPrice() != local.getSellPrice() ||
                                    remote.getStockQuantity() != local.getStockQuantity()) {
                                    local.setName(remote.getName());
                                    local.setBarcode(remote.getBarcode());
                                    local.setBuyPrice(remote.getBuyPrice());
                                    local.setSellPrice(remote.getSellPrice());
                                    local.setCategoryId(remote.getCategoryId());
                                    local.setCategoryName(remote.getCategoryName());
                                    local.setBaseUnitId(remote.getBaseUnitId());
                                    local.setBaseUnitName(remote.getBaseUnitName());
                                    local.setNotes(remote.getNotes());
                                    db.productDao().update(local);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة المنتجات", e);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "إلغاء مزامنة المنتجات: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void syncCustomersDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Customer remote = child.getValue(Customer.class);
                            if (remote == null || remote.getName() == null || remote.getName().isEmpty()) continue;
                            
                            Customer local = db.customerDao().getByIdSync(remote.getId());
                            if (local == null) {
                                // عميل جديد - إضافته
                                db.customerDao().insert(remote);
                            } else {
                                // تحديث بيانات العميل الموجود
                                local.setName(remote.getName());
                                local.setPhone(remote.getPhone());
                                local.setAddress(remote.getAddress());
                                local.setEmail(remote.getEmail());
                                local.setCreditLimit(remote.getCreditLimit());
                                // الاحتفاظ بأحدث رصيد
                                if (Math.abs(remote.getCurrentBalance()) > Math.abs(local.getCurrentBalance())) {
                                    local.setCurrentBalance(remote.getCurrentBalance());
                                }
                                db.customerDao().update(local);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة العملاء", e);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "إلغاء مزامنة العملاء: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void syncSuppliersDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("suppliers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Supplier remote = child.getValue(Supplier.class);
                            if (remote == null || remote.getName() == null || remote.getName().isEmpty()) continue;
                            
                            Supplier local = db.supplierDao().getByIdSync(remote.getId());
                            if (local == null) {
                                db.supplierDao().insert(remote);
                            } else {
                                local.setName(remote.getName());
                                local.setPhone(remote.getPhone());
                                local.setAddress(remote.getAddress());
                                if (Math.abs(remote.getCurrentBalance()) > Math.abs(local.getCurrentBalance())) {
                                    local.setCurrentBalance(remote.getCurrentBalance());
                                }
                                db.supplierDao().update(local);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة الموردين", e);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "إلغاء مزامنة الموردين: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void syncCategoriesDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Category remote = child.getValue(Category.class);
                            if (remote != null) {
                                Category local = db.categoryDao().getById(remote.getId());
                                if (local == null) {
                                    db.categoryDao().insert(remote);
                                } else {
                                    local.setName(remote.getName());
                                    local.setDescription(remote.getDescription());
                                    db.categoryDao().update(local);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة الفئات", e);
                } finally {
                    latch.countDown();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void syncUnitsDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("units").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Unit remote = child.getValue(Unit.class);
                            if (remote != null && remote.getName() != null && !remote.getName().isEmpty()) {
                                db.unitDao().insert(remote); // insertWithOnConflict سيتولى التحديث
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة الوحدات", e);
                } finally {
                    latch.countDown();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void syncInvoicesDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        rootRef.child("invoices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        for (DataSnapshot invSnap : snapshot.getChildren()) {
                            int invId = Integer.parseInt(invSnap.getKey());
                            
                            // قراءة بيانات الفاتورة من Firebase
                            Invoice inv = new Invoice();
                            inv.setId(invId);
                            if (invSnap.hasChild("invoiceNumber"))
                                inv.setInvoiceNumber(invSnap.child("invoiceNumber").getValue(String.class));
                            if (invSnap.hasChild("type"))
                                inv.setType(invSnap.child("type").getValue(Integer.class));
                            if (invSnap.hasChild("customerId"))
                                inv.setCustomerId(invSnap.child("customerId").getValue(Integer.class));
                            if (invSnap.hasChild("supplierId"))
                                inv.setSupplierId(invSnap.child("supplierId").getValue(Integer.class));
                            if (invSnap.hasChild("date"))
                                inv.setDate(invSnap.child("date").getValue(Long.class));
                            if (invSnap.hasChild("subtotal"))
                                inv.setSubtotal(invSnap.child("subtotal").getValue(Double.class));
                            if (invSnap.hasChild("taxAmount"))
                                inv.setTaxAmount(invSnap.child("taxAmount").getValue(Double.class));
                            if (invSnap.hasChild("discount"))
                                inv.setDiscount(invSnap.child("discount").getValue(Double.class));
                            if (invSnap.hasChild("total"))
                                inv.setTotal(invSnap.child("total").getValue(Double.class));
                            if (invSnap.hasChild("paidAmount"))
                                inv.setPaidAmount(invSnap.child("paidAmount").getValue(Double.class));
                            if (invSnap.hasChild("remainingAmount"))
                                inv.setRemainingAmount(invSnap.child("remainingAmount").getValue(Double.class));
                            if (invSnap.hasChild("paymentStatus"))
                                inv.setPaymentStatus(invSnap.child("paymentStatus").getValue(Integer.class));

                            // التحقق من وجود الفاتورة محلياً
                            boolean exists = false;
                            List<Invoice> existing = db.invoiceDao().getAllSync();
                            if (existing != null) {
                                for (Invoice e : existing) {
                                    if (e.getId() == invId) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }

                            if (!exists) {
                                db.invoiceDao().insert(inv);
                                
                                // مزامنة عناصر الفاتورة
                                DataSnapshot itemsSnap = invSnap.child("items");
                                if (itemsSnap.exists()) {
                                    for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                                        InvoiceItem item = new InvoiceItem();
                                        item.setInvoiceId(invId);
                                        if (itemSnap.hasChild("productId"))
                                            item.setProductId(itemSnap.child("productId").getValue(Integer.class));
                                        if (itemSnap.hasChild("productName"))
                                            item.setProductName(itemSnap.child("productName").getValue(String.class));
                                        if (itemSnap.hasChild("quantity"))
                                            item.setQuantity(itemSnap.child("quantity").getValue(Double.class));
                                        if (itemSnap.hasChild("unitPrice"))
                                            item.setUnitPrice(itemSnap.child("unitPrice").getValue(Double.class));
                                        if (itemSnap.hasChild("total"))
                                            item.setTotalPrice(itemSnap.child("total").getValue(Double.class));
                                        
                                        db.invoiceDao().insertItem(item);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "خطأ مزامنة الفواتير", e);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "إلغاء مزامنة الفواتير: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    // ================================================================
    // تحديث الأرصدة
    // ================================================================

    private void updateAllBalances() {
        Log.d(TAG, "💰 جاري تحديث أرصدة العملاء والموردين...");
        
        // تحديث أرصدة العملاء من مجموع المبالغ المتبقية في فواتيرهم
        List<Customer> customers = db.customerDao().getAllSync();
        if (customers != null) {
            for (Customer c : customers) {
                double balance = 0;
                List<Invoice> invoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_SALE);
                if (invoices != null) {
                    for (Invoice inv : invoices) {
                        if (inv.getCustomerId() != null && inv.getCustomerId().equals(c.getId())) {
                            balance += inv.getRemainingAmount();
                        }
                    }
                }
                c.setCurrentBalance(balance);
                db.customerDao().update(c);
            }
            Log.d(TAG, "✅ تم تحديث أرصدة " + customers.size() + " عميل");
        }

        // تحديث أرصدة الموردين
        List<Supplier> suppliers = db.supplierDao().getAllSync();
        if (suppliers != null) {
            for (Supplier s : suppliers) {
                double balance = 0;
                List<Invoice> invoices = db.invoiceDao().getAllByTypeSync(Invoice.TYPE_PURCHASE);
                if (invoices != null) {
                    for (Invoice inv : invoices) {
                        if (inv.getSupplierId() != null && inv.getSupplierId().equals(s.getId())) {
                            balance += inv.getRemainingAmount();
                        }
                    }
                }
                s.setCurrentBalance(balance);
                db.supplierDao().update(s);
            }
            Log.d(TAG, "✅ تم تحديث أرصدة " + suppliers.size() + " مورد");
        }
    }
}
