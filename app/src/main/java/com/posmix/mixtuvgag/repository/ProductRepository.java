package com.posmix.mixtuvgag.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.database.dao.ProductDao;
import com.posmix.mixtuvgag.models.Product;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private final ProductDao dao;
    private final ExecutorService exec;

    public ProductRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        dao = db.productDao();
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllProducts() { return dao.getAllActive(); }
    
    public List<Product> getAllProductsForSync() { return dao.getAllActiveForSync(); }
    
    public LiveData<List<Product>> search(String q) { return dao.search(q); }
    
    public Product getById(int id) { return dao.getById(id); }
    
    public void insert(Product p) { exec.execute(() -> dao.insert(p)); }
    
    public long insertAndGetId(Product p) { 
        return dao.insert(p);
    }
    
    public void update(Product p) { exec.execute(() -> dao.update(p)); }
    
    public void delete(Product p) { exec.execute(() -> dao.delete(p)); }
}