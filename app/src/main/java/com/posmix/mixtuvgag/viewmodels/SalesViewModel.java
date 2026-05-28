package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.repository.ProductRepository;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Customer; // Import Customer model
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class SalesViewModel extends AndroidViewModel {
    private final ProductRepository repo;
    private final LiveData<List<Product>> products;
    private final LiveData<List<Customer>> customers; // Add LiveData for customers

    public SalesViewModel(Application app) {
        super(app);
        repo = new ProductRepository(app);
        products = repo.getAllProducts();
        customers = AppDatabase.getInstance(app).customerDao().getAll(); // Initialize customers LiveData
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<List<Product>> search(String q) { return repo.search(q); }
    public LiveData<List<Customer>> getCustomers() { return customers; } // Getter for customers
}