package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.models.Supplier;

public class SuppliersAdapter extends ListAdapter<Supplier, SuppliersAdapter.VH> {

    public interface SupplierClickListener {
        void onEdit(Supplier supplier);
        void onDelete(Supplier supplier);
    }

    private final SupplierClickListener listener;

    public SuppliersAdapter(SupplierClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Supplier> DIFF = new DiffUtil.ItemCallback<Supplier>() {
        @Override public boolean areItemsTheSame(@NonNull Supplier a, @NonNull Supplier b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Supplier a, @NonNull Supplier b) {
            return a.getName().equals(b.getName()) && a.getCurrentBalance() == b.getCurrentBalance();
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Supplier s = getItem(pos);
        h.tvName.setText(s.getName());
        h.tvPhone.setText(s.getPhone() != null ? s.getPhone() : "");
        h.tvBalance.setText(String.format("الرصيد: %.2f ر.س", s.getCurrentBalance()));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvBalance;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_supplier_name);
            tvPhone = v.findViewById(R.id.tv_supplier_phone);
            tvBalance = v.findViewById(R.id.tv_supplier_balance);
            btnEdit = v.findViewById(R.id.btn_edit_supplier);
            btnDelete = v.findViewById(R.id.btn_delete_supplier);
        }
    }
}
