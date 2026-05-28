package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.Supplier;
import com.posmix.mixtuvgag.database.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class SuppliersViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LiveData<List<Supplier>> suppliers;
    private final ExecutorService exec;

    public SuppliersViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        suppliers = db.supplierDao().getAll();
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Supplier>> getSuppliers() { return suppliers; }
    public LiveData<List<Supplier>> search(String q) { return db.supplierDao().search(q); }
    public void insert(Supplier s) { exec.execute(() -> db.supplierDao().insert(s)); }
    public void update(Supplier s) { exec.execute(() -> db.supplierDao().update(s)); }
    public void delete(Supplier s) { exec.execute(() -> db.supplierDao().delete(s)); }
}
