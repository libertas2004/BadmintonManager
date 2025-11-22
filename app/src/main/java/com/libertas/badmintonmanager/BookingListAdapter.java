package com.libertas.badmintonmanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingListAdapter extends RecyclerView.Adapter<BookingListAdapter.ViewHolder> {

    private List<Booking> bookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingListAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.tvCourt.setText(booking.getCourtName());
        holder.tvCustomer.setText("Khách hàng: " + booking.getCustomerName());
        holder.tvTime.setText(booking.getDate() + " | " + booking.getTime());
        holder.tvPrice.setText(String.format("%,d VNĐ", booking.getPrice()));
        holder.tvStatus.setText(booking.getStatusText());

        // Set status color
        switch (booking.getStatus()) {
            case "pending":
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case "paid":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case "confirmed":
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                break;
            case "cancelled":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onBookingClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCourt, tvCustomer, tvTime, tvPrice, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCourt = itemView.findViewById(R.id.tvCourt);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}