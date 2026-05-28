package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.Expense;
import com.posmix.mixtuvgag.database.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class ExpensesViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LiveData<List<Expense>> expenses;
    private final ExecutorService exec;

    public ExpensesViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        expenses = db.expenseDao().getAll();
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Expense>> getExpenses() { return expenses; }
    public void insert(Expense e) { exec.execute(() -> db.expenseDao().insert(e)); }
    public void delete(Expense e) { exec.execute(() -> db.expenseDao().delete(e)); }
}
