package com.posmix.mixtuvgag.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.CashTransaction;
import com.posmix.mixtuvgag.models.Invoice;
import com.posmix.mixtuvgag.utils.CurrencyHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomerHistoryAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_INVOICE = 1;
    private static final int VIEW_TYPE_CASHTRANSACTION = 2;

    public interface OnHistoryItemClickListener {
        void onInvoiceClick(Invoice invoice);
        void onCashTransactionClick(CashTransaction transaction);
    }

    private final OnHistoryItemClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US); // Use US locale for digits

    public CustomerHistoryAdapter(OnHistoryItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {
        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof Invoice && newItem instanceof Invoice) {
                return ((Invoice) oldItem).getId() == ((Invoice) newItem).getId();
            } else if (oldItem instanceof CashTransaction && newItem instanceof CashTransaction) {
                return ((CashTransaction) oldItem).getId() == ((CashTransaction) newItem).getId();
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof Invoice && newItem instanceof Invoice) {
                Invoice oldInv = (Invoice) oldItem;
                Invoice newInv = (Invoice) newItem;
                return oldInv.getTotal() == newInv.getTotal() &&
                       oldInv.getRemainingAmount() == newInv.getRemainingAmount() &&
                       oldInv.getInvoiceNumber().equals(newInv.getInvoiceNumber()) &&
                       oldInv.getDate() == newInv.getDate();
            } else if (oldItem instanceof CashTransaction && newItem instanceof CashTransaction) {
                CashTransaction oldTrans = (CashTransaction) oldItem;
                CashTransaction newTrans = (CashTransaction) newItem;
                return oldTrans.getAmount() == newTrans.getAmount() &&
                       oldTrans.getDescription().equals(newTrans.getDescription()) &&
                       oldTrans.getDate() == newTrans.getDate() &&
                       oldTrans.getType() == newTrans.getType();
            }
            return false;
        }
    };

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Invoice) {
            return VIEW_TYPE_INVOICE;
        } else if (item instanceof CashTransaction) {
            return VIEW_TYPE_CASHTRANSACTION;
        }
        return -1; // Should not happen
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_INVOICE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_summary, parent, false);
            return new InvoiceViewHolder(view);
        } else if (viewType == VIEW_TYPE_CASHTRANSACTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_payment_history, parent, false);
            return new CashTransactionViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_INVOICE) {
            ((InvoiceViewHolder) holder).bind((Invoice) item);
        } else if (holder.getItemViewType() == VIEW_TYPE_CASHTRANSACTION) {
            ((CashTransactionViewHolder) holder).bind((CashTransaction) item);
        }
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvInvoiceDate, tvInvoiceTotal, tvRemainingAmount;
        CardView cardView;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            tvInvoiceDate = itemView.findViewById(R.id.tv_invoice_date);
            tvInvoiceTotal = itemView.findViewById(R.id.tv_invoice_total);
            tvRemainingAmount = itemView.findViewById(R.id.tv_remaining_amount);
            cardView = itemView.findViewById(R.id.card_invoice);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onInvoiceClick((Invoice) getItem(position));
                }
            });
        }

        void bind(Invoice invoice) {
            tvInvoiceNumber.setText(invoice.getInvoiceNumber());
            tvInvoiceDate.setText(sdf.format(new Date(invoice.getDate())));
            tvInvoiceTotal.setText(String.format(Locale.US, "%.2f %s",
                invoice.getTotal(), CurrencyHelper.getSymbol()));

            if (invoice.getRemainingAmount() > 0) {
                tvRemainingAmount.setText(String.format(Locale.US, "متبقي: %.2f %s",
                    invoice.getRemainingAmount(), CurrencyHelper.getSymbol()));
                tvRemainingAmount.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                tvRemainingAmount.setVisibility(View.VISIBLE);
            } else {
                tvRemainingAmount.setText(R.string.invoice_status_fully_paid); // Changed to new string resource
                tvRemainingAmount.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvRemainingAmount.setVisibility(View.VISIBLE);
            }
        }
    }

    class CashTransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvAmount;
        ImageView ivIcon;
        CardView cardView;

        CashTransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_transaction_description);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            cardView = itemView.findViewById(R.id.card_payment_transaction);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCashTransactionClick((CashTransaction) getItem(position));
                }
            });
        }

        void bind(CashTransaction transaction) {
            tvDescription.setText(transaction.getDescription());
            tvDate.setText(sdf.format(new Date(transaction.getDate())));
            
            boolean isIn = transaction.getType() == CashTransaction.TYPE_IN;
            String sign = isIn ? "+ " : "- ";
            tvAmount.setText(String.format(Locale.US, "%s%.2f %s", sign, transaction.getAmount(), CurrencyHelper.getSymbol()));
            tvAmount.setTextColor(isIn ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
            
            ivIcon.setImageResource(isIn ? R.drawable.ic_cashbox : R.drawable.ic_remove_minus); // Reuse ic_remove_minus for cash out
            ivIcon.setColorFilter(isIn ? Color.parseColor("#4CAF50") : Color.parseColor("#C62828"));
        }
    }
}