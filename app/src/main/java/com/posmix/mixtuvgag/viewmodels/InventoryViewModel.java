package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.repository.ProductRepository;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class InventoryViewModel extends AndroidViewModel {
    private final ProductRepository repo;
    private final LiveData<List<Product>> products;

    public InventoryViewModel(Application application) {
        super(application);
        repo = new ProductRepository(application);
        products = repo.getAllProducts();
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public void insert(Product p) { repo.insert(p); }
    public long insertAndGetId(Product p) { return repo.insertAndGetId(p); }
    public void update(Product p) { repo.update(p); }
    public void delete(Product p) { repo.delete(p); }
    public LiveData<List<Product>> searchProducts(String q) { return repo.search(q); }
}