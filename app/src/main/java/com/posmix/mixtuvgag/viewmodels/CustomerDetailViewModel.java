package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.CashTransaction;
import com.posmix.mixtuvgag.models.Customer;
import com.posmix.mixtuvgag.models.Invoice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerDetailViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService exec;

    private final MutableLiveData<Customer> customer = new MutableLiveData<>();
    private LiveData<List<Invoice>> outstandingInvoices; 
    private final MutableLiveData<List<Object>> customerHistory = new MutableLiveData<>(); // Combined history

    public CustomerDetailViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<Customer> getCustomer() {
        return customer;
    }

    public LiveData<List<Invoice>> getOutstandingInvoices() {
        return outstandingInvoices;
    }
    
    public LiveData<List<Object>> getCustomerHistory() {
        return customerHistory;
    }

    public void loadCustomer(int customerId) {
        exec.execute(() -> {
            Customer c = db.customerDao().getByIdSync(customerId);
            customer.postValue(c);
        });
    }

    public void loadOutstandingInvoices(int customerId) {
        // This is for the summary card at the top, showing only truly outstanding invoices.
        outstandingInvoices = db.invoiceDao().getCreditInvoicesByCustomer(customerId);
    }
    
    public void loadCustomerHistory(int customerId) {
        exec.execute(() -> {
            List<Object> history = new ArrayList<>();
            
            // 1. الفواتير الآجلة فقط (التي تم تسجيلها من صفحة الفواتير)
            // التأكد من جلب الفواتير التي نوعها آجل ولها رصيد متبقي
            List<Invoice> creditInvoices = db.invoiceDao().getCreditInvoicesByCustomerSync(customerId);
            if (creditInvoices != null && !creditInvoices.isEmpty()) {
                history.addAll(creditInvoices);
            }
            
            // 2. السندات المرتبطة بالعميل فقط
            List<CashTransaction> cashTransactions = db.cashTransactionDao().getForCustomer(customerId);
            if (cashTransactions != null && !cashTransactions.isEmpty()) {
                history.addAll(cashTransactions);
            }
            
            // IMPORTANT: لا نضيف أي شيء آخر - لا فواتير نقدية، لا فواتير شراء، لا حركات مخزون
            
            // ترتيب الحركات من الأحدث إلى الأقدم
            Collections.sort(history, (o1, o2) -> {
                long date1 = getDateFromObject(o1);
                long date2 = getDateFromObject(o2);
                return Long.compare(date2, date1); // تنازلي
            });
            
            customerHistory.postValue(history);
        });
    }
    
    // دالة مساعدة لاستخراج التاريخ من أي كائن
    private long getDateFromObject(Object obj) {
        if (obj instanceof Invoice) {
            return ((Invoice) obj).getDate();
        } else if (obj instanceof CashTransaction) {
            return ((CashTransaction) obj).getDate();
        }
        return 0;
    }

    public void updateCustomer(Customer c) {
        exec.execute(() -> {
            db.customerDao().update(c);
            loadCustomer(c.getId()); // تحديث البيانات بعد التعديل
            loadCustomerHistory(c.getId()); // تحديث تاريخ العميل أيضاً
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        exec.shutdown();
    }
}