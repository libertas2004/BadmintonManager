package com.libertas.badmintonmanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> slots;
    private OnSlotSelectedListener listener;

    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlot slot, boolean isSelected);
    }

    public TimeSlotAdapter(List<TimeSlot> slots, OnSlotSelectedListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeslot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);

        if (slot.isPast()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#BDBDBD")); // Gray
            holder.itemView.setEnabled(false);
            holder.tvBookedBy.setVisibility(View.GONE);
        } else if (slot.isBooked()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F44336")); // Red
            holder.itemView.setEnabled(false);
            if (!slot.getBookedBy().isEmpty()) {
                holder.tvBookedBy.setText(slot.getBookedBy());
                holder.tvBookedBy.setVisibility(View.VISIBLE);
            } else {
                holder.tvBookedBy.setVisibility(View.GONE);
            }
        } else if (slot.isSelected()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.tvBookedBy.setVisibility(View.GONE);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvBookedBy.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!slot.isBooked() && !slot.isPast()) {
                slot.setSelected(!slot.isSelected());
                notifyItemChanged(position);
                listener.onSlotSelected(slot, slot.isSelected());
            }
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public List<TimeSlot> getSlots() {
        return slots;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookedBy;

        ViewHolder(View itemView) {
            super(itemView);
            tvBookedBy = itemView.findViewById(R.id.tvBookedBy);
        }
    }
}