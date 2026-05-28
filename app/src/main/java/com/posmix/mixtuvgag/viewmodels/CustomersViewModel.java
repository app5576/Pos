package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.database.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class CustomersViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LiveData<List<Customer>> customers;
    private final ExecutorService exec;

    public CustomersViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        customers = db.customerDao().getAll();
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Customer>> getCustomers() { return customers; }
    public void insert(Customer c) { exec.execute(() -> db.customerDao().insert(c)); }
    public void update(Customer c) { exec.execute(() -> db.customerDao().update(c)); }
    public void delete(Customer c) { exec.execute(() -> db.customerDao().delete(c)); }
}
