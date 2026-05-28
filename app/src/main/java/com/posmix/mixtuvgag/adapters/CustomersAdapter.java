package com.posmix.mixtuvgag.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R; // Ensure R is imported
import com.posmix.mixtuvgag.models.Customer;
import java.util.Locale; // Import Locale

public class CustomersAdapter extends ListAdapter<Customer, CustomersAdapter.VH> {
    public interface CustomerActionsListener {
        void onCustomerClick(Customer c);
        void onEdit(Customer c);
        void onDelete(Customer c);
    }
    private CustomerActionsListener listener;

    public CustomersAdapter(CustomerActionsListener l) {
        super(new DiffUtil.ItemCallback<Customer>() {
            public boolean areItemsTheSame(Customer o, Customer n) { return o.getId() == n.getId(); }
            public boolean areContentsTheSame(Customer o, Customer n) {
                return o.getName().equals(n.getName()) &&
                       o.getPhone().equals(n.getPhone()) &&
                       o.getCurrentBalance() == n.getCurrentBalance(); // Compare relevant content
            }
        });
        this.listener = l;
    }

    @Override public VH onCreateViewHolder(ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_customer, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(VH h, int p) { h.bind(getItem(p)); }

    class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvBalance;
        ImageButton btnEdit, btnDelete; // Added for edit and delete buttons

        VH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_customer_name);
            tvPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvBalance = itemView.findViewById(R.id.tv_customer_balance);
            btnEdit = itemView.findViewById(R.id.btn_customer_edit);
            btnDelete = itemView.findViewById(R.id.btn_customer_delete);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCustomerClick(getItem(getAdapterPosition()));
                }
            });
            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEdit(getItem(getAdapterPosition()));
                }
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDelete(getItem(getAdapterPosition()));
                }
            });
        }

        void bind(Customer c) {
            tvName.setText(c.getName());
            // Format phone number to English digits if it might contain Arabic ones
            tvPhone.setText("الهاتف: " + formatNumberToEnglishDigits(c.getPhone() != null && !c.getPhone().isEmpty() ? c.getPhone() : "غير محدد"));
            // Format balance to English digits using Locale.US
            tvBalance.setText(String.format(Locale.US, "الرصيد: %.2f ر.س", c.getCurrentBalance()));
        }
        
        // Helper method to convert Arabic-Indic digits to Western Arabic digits
        private String formatNumberToEnglishDigits(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c >= '٠' && c <= '٩') { // Arabic-Indic digits
                    builder.append((char) (c - '٠' + '0')); // Convert to Western Arabic digits
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        }
    }
}