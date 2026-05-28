package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.posmix.mixtuvgag.models.CashTransaction;
import com.posmix.mixtuvgag.database.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;
public class CashFundViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService exec;

    public CashFundViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CashTransaction>> getTransactions() {
        return db.cashTransactionDao().getRecent();
    }

    public void insert(CashTransaction t) {
        exec.execute(() -> db.cashTransactionDao().insert(t));
    }
}
