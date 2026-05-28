package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.models.InvoiceItem;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.repository.ProductRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
public class PurchasesViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ProductRepository productRepo;
    private final LiveData<List<Product>> products;
    private final ExecutorService exec;
    private final MutableLiveData<String> lastInvoiceNumber = new MutableLiveData<>();

    public PurchasesViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        productRepo = new ProductRepository(app);
        products = productRepo.getAllProducts();
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<List<Product>> searchProducts(String q) { return productRepo.search(q); }
    public LiveData<String> getLastInvoiceNumber() { return lastInvoiceNumber; }

    public void savePurchaseInvoice(Invoice invoice, List<InvoiceItem> items, Runnable onComplete) {
        exec.execute(() -> {
            long invoiceId = db.invoiceDao().insert(invoice);
            for (InvoiceItem item : items) {
                item.setInvoiceId((int) invoiceId);
                db.invoiceDao().insertItem(item);
                db.productDao().increaseStock(item.getProductId(), (int) item.getQuantity());
            }
            if (onComplete != null) onComplete.run();
        });
    }
}
