package com.mobile.sca.base;


import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.R;

import java.util.List;

public class SchedulesAdapter extends RecyclerView.Adapter<SchedulesAdapter.VH> {

    public interface OnChangedListener {
        void onChanged(Schedule schedule);
    }

    private final List<Schedule> items;
    private final boolean is24Hour;
    private final OnChangedListener onChanged;

    public SchedulesAdapter(List<Schedule> items, boolean is24Hour, OnChangedListener onChanged) {
        this.items = items;
        this.is24Hour = is24Hour;
        this.onChanged = onChanged;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView titleTv, onTimeTv, offTimeTv;
        Button setOnBtn, setOffBtn;

        public VH(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            onTimeTv = itemView.findViewById(R.id.onTimeTv);
            offTimeTv = itemView.findViewById(R.id.offTimeTv);
            setOnBtn = itemView.findViewById(R.id.setOnBtn);
            setOffBtn = itemView.findViewById(R.id.setOffBtn);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Schedule item = items.get(position);
        holder.titleTv.setText("Schedule #" + item.getId());
        holder.onTimeTv.setText("On: " + item.getOnTimeText());
        holder.offTimeTv.setText("Off: " + item.getOffTimeText());

        holder.setOnBtn.setOnClickListener(v -> {
            int hour = item.getOnHour() != null ? item.getOnHour() : 9;
            int minute = item.getOnMinute() != null ? item.getOnMinute() : 0;

            new TimePickerDialog(v.getContext(), (view, h, m) -> {
                item.setOnTime(h, m);
                notifyItemChanged(holder.getAdapterPosition());
                if (onChanged != null) onChanged.onChanged(item);
            }, hour, minute, is24Hour).show();
        });

        holder.setOffBtn.setOnClickListener(v -> {
            int hour = item.getOffHour() != null ? item.getOffHour() : 18;
            int minute = item.getOffMinute() != null ? item.getOffMinute() : 0;

            new TimePickerDialog(v.getContext(), (view, h, m) -> {
                item.setOffTime(h, m);
                notifyItemChanged(holder.getAdapterPosition());
                if (onChanged != null) onChanged.onChanged(item);
            }, hour, minute, is24Hour).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addNewSchedule() {
        int nextId = 1;
        for (Schedule s : items) nextId = Math.max(nextId, s.getId() + 1);
        items.add(new Schedule(nextId));
        notifyItemInserted(items.size() - 1);
    }
}
