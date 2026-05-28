package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.posmix.mixtuvgag.activities.MainActivity;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private final Context context;
    private final ProgressBar progressBar;
    private final CoordinatorLayout mainLayout;
    private final ExecutorService executorService;
    private final AppDatabase database;
    private final Handler uiHandler;

    // Firebase References
    private DatabaseReference firebaseDatabaseRef;
    private FirebaseFirestore firestoreDatabaseRef;

    public SyncManager(Context context, ProgressBar progressBar, CoordinatorLayout mainLayout) {
        this.context = context;
        this.progressBar = progressBar;
        this.mainLayout = mainLayout;
        this.executorService = Executors.newSingleThreadExecutor();
        this.database = AppDatabase.getInstance(context);
        this.uiHandler = new Handler(Looper.getMainLooper());

        // Initialize Firebase references (choose one database or both as needed)
        // For Firebase Realtime Database:
        firebaseDatabaseRef = FirebaseDatabase.getInstance().getReference("products");
        // For Cloud Firestore:
        firestoreDatabaseRef = FirebaseFirestore.getInstance();
    }

    public void startSync() {
        showProgress();
        // Initiate both upload and download (or choose one based on strategy)
        uploadLocalData();
        downloadFirebaseData();
    }

    private void showProgress() {
        uiHandler.post(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (mainLayout != null) {
                // Optionally dim the main content or disable interactions
            }
        });
    }

    private void hideProgress() {
        uiHandler.post(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (mainLayout != null) {
                // Optionally restore main content interactions
            }
        });
    }

    private void showMessage(String message) {
        uiHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    // --- Upload Data to Firebase ---
    private void uploadLocalData() {
        executorService.execute(() -> {
            // Example: Upload products to Firebase Realtime Database
            List<Product> products = database.productDao().getAllActiveForSync(); // Assuming you add this method to ProductDao
            if (products == null || products.isEmpty()) {
                Log.d(TAG, "No local products to upload.");
                // If no local data, still hide progress and show message
                hideProgress();
                showMessage("لا توجد بيانات محلية لمزامنتها.");
                return;
            }

            // Using Firebase Realtime Database for upload example
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("name", product.getName());
                productData.put("barcode", product.getBarcode());
                productData.put("categoryId", product.getCategoryId()); // Use categoryId
                productData.put("categoryName", product.getCategoryName()); // Use categoryName
                productData.put("buyPrice", product.getBuyPrice());
                productData.put("sellPrice", product.getSellPrice());
                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("minStockAlert", product.getMinStockAlert());
                productData.put("taxPercentage", product.getTaxPercentage());
                productData.put("isActive", product.isActive());
                productData.put("notes", product.getNotes());

                firebaseDatabaseRef.child(String.valueOf(product.getId())).setValue(productData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Product uploaded: " + product.getName()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload product: " + product.getName(), e);
                        showMessage("فشل في رفع بيانات المنتج: " + product.getName());
                    });
            }

            // Using Cloud Firestore for upload example (replace if using Firestore)
            /*
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("name", product.getName());
                // ... other fields
                firestoreDatabaseRef.collection("products").document(String.valueOf(product.getId()))
                    .set(productData, SetOptions.merge()) // Use merge to update existing or create new
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Product uploaded to Firestore: " + product.getName()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload product to Firestore: " + product.getName(), e);
                        showMessage("فشل في رفع بيانات المنتج إلى Firestore: " + product.getName());
                    });
            }
            */

            // After attempting upload, proceed to download or finish sync process
            // In a real app, you'd have more sophisticated conflict resolution and completion handling
            Log.d(TAG, "Upload process finished (or initiated).");
            // If download is also initiated here, you might hide progress after both complete
        });
    }

    // --- Download Data from Firebase ---
    private void downloadFirebaseData() {
        executorService.execute(() -> {
            // Example: Download products from Firebase Realtime Database
            firebaseDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Product> productsToInsertOrUpdate = new ArrayList<>();
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        Map<String, Object> data = (Map<String, Object>) productSnapshot.getValue();
                        if (data != null) {
                            Product product = new Product();
                            try {
                                product.setId(Integer.parseInt(productSnapshot.getKey())); // Key is usually the ID
                                product.setName((String) data.get("name"));
                                product.setBarcode((String) data.get("barcode"));
                                product.setCategoryId(toInt(data.get("categoryId"))); // Use categoryId
                                product.setCategoryName((String) data.get("categoryName")); // Use categoryName
                                product.setBuyPrice(toDouble(data.get("buyPrice")));
                                product.setSellPrice(toDouble(data.get("sellPrice")));
                                product.setStockQuantity(toInt(data.get("stockQuantity")));
                                product.setMinStockAlert(toInt(data.get("minStockAlert")));
                                product.setTaxPercentage(toDouble(data.get("taxPercentage")));
                                product.setActive(toBoolean(data.get("isActive")));
                                product.setNotes((String) data.get("notes"));
                                productsToInsertOrUpdate.add(product);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing product data: " + productSnapshot.getKey(), e);
                                // Handle parsing errors, maybe skip this item or show error message
                            }
                        }
                    }

                    if (!productsToInsertOrUpdate.isEmpty()) {
                        // Update local database in a transaction
                        database.getWritableDatabase().beginTransaction();
                        try {
                            // Clear existing products or handle updates more granularly
                            // For simplicity, we'll assume a full sync and clear/re-add
                            // In a real app, you'd compare and update/insert/delete
                            database.productDao().clearAllProducts(); // Assuming this method exists
                            for (Product p : productsToInsertOrUpdate) {
                                database.productDao().insert(p); // Use insert or update based on logic
                            }
                            database.getWritableDatabase().setTransactionSuccessful();
                            Log.d(TAG, "Local product data synced successfully.");
                            showMessage("تمت مزامنة المنتجات بنجاح!");
                        } catch (Exception e) {
                            Log.e(TAG, "Error during local database sync", e);
                            showMessage("خطأ في مزامنة بيانات المنتجات.");
                        } finally {
                            database.getWritableDatabase().endTransaction();
                            hideProgress(); // Hide progress bar after download is processed
                        }
                    } else {
                        Log.d(TAG, "No products found in Firebase or data parsing failed.");
                        showMessage("لم يتم العثور على منتجات في Firebase أو فشلت قراءة البيانات.");
                        hideProgress();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase download cancelled: " + error.getMessage());
                    showMessage("تم إلغاء تحميل بيانات Firebase.");
                    hideProgress();
                }
            });

            // Example: Download products from Cloud Firestore (replace if using Firestore)
            /*
            firestoreDatabaseRef.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productsToInsertOrUpdate = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            Product product = new Product();
                            try {
                                product.setId(Integer.parseInt(document.getId())); // Document ID is the product ID
                                product.setName((String) data.get("name"));
                                // ... parse other fields
                                productsToInsertOrUpdate.add(product);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing Firestore product data: " + document.getId(), e);
                            }
                        }
                    }
                    if (!productsToInsertOrUpdate.isEmpty()) {
                        // Update local database (similar logic as above)
                        Log.d(TAG, "Firestore data processed.");
                        showMessage("تمت مزامنة المنتجات من Firestore بنجاح!");
                    } else {
                        Log.d(TAG, "No products found in Firestore or data parsing failed.");
                        showMessage("لم يتم العثور على منتجات في Firestore أو فشلت قراءة البيانات.");
                    }
                    hideProgress();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to download data from Firestore", e);
                    showMessage("فشل تحميل البيانات من Firestore.");
                    hideProgress();
                });
            */
        });
    }

    // Helper methods for type casting from Firebase data
    private double toDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return 0.0;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    private boolean toBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return false;
    }

    // Add a method to ProductDao to get all active products for sync
    // This would typically be:
    // public List<Product> getAllActiveForSync() { ... query ... return list; }
    // For now, assuming it exists or needs to be added.
}