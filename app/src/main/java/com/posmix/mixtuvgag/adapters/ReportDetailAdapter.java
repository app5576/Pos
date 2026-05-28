package com.posmix.mixtuvgag.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.posmix.mixtuvgag.R;
import java.util.ArrayList;
import java.util.List;

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ViewHolder> {

    private List<String> items = new ArrayList<>();

    public void submitList(List<String> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvText.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(android.R.id.text1);
        }
    }
}