package com.posmix.mixtuvgag.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.models.Invoice;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;
public class ReportsViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService exec;
    private final MutableLiveData<ReportData> reportData = new MutableLiveData<>();

    public ReportsViewModel(Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        exec = Executors.newSingleThreadExecutor();
    }

    public LiveData<ReportData> getReportData() { return reportData; }

    public void loadSalesReport(long from, long to) {
        exec.execute(() -> {
            double totalSales = db.invoiceDao().getTotalByTypeBetween(Invoice.TYPE_SALE, from, to);
            double totalPurchases = db.invoiceDao().getTotalByTypeBetween(Invoice.TYPE_PURCHASE, from, to);
            double totalExpenses = db.expenseDao().getTotalBetween(from, to);
            int invoiceCount = db.invoiceDao().getCountByTypeBetween(Invoice.TYPE_SALE, from, to);
            double profit = totalSales - totalPurchases - totalExpenses;
            reportData.postValue(new ReportData(totalSales, totalPurchases, totalExpenses, profit, invoiceCount));
        });
    }

    public static class ReportData {
        public final double totalSales, totalPurchases, totalExpenses, profit;
        public final int invoiceCount;
        public ReportData(double sales, double purchases, double expenses, double profit, int count) {
            this.totalSales = sales; this.totalPurchases = purchases;
            this.totalExpenses = expenses; this.profit = profit; this.invoiceCount = count;
        }
    }
}
