package com.mobile.sca.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.R;

import java.util.List;

public class SimpleSchedulesAdapter extends RecyclerView.Adapter<SimpleSchedulesAdapter.VH> {

    private final List<SimpleSchedule> items;

    public SimpleSchedulesAdapter(List<SimpleSchedule> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView scheduleText;

        VH(@NonNull View itemView) {
            super(itemView);
            scheduleText = itemView.findViewById(R.id.scheduleText);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item1, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.scheduleText.setText(items.get(position).display());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void add(SimpleSchedule s) {
        items.add(s);
        notifyItemInserted(items.size() - 1);
    }
}

