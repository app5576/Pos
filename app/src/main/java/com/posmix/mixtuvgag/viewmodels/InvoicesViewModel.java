package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Invoice;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvoicesViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InvoicesViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<Invoice>> getAllInvoices() {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Invoice> list = db.invoiceDao().getAllSync();
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Invoice>> getInvoicesByType(int type) {
        MutableLiveData<List<Invoice>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Invoice> list = db.invoiceDao().getAllByTypeSync(type);
            liveData.postValue(list);
        });
        return liveData;
    }

    public void deleteInvoice(Invoice invoice) {
        executor.execute(() -> {
            db.invoiceDao().delete(invoice);
        });
    }
}
