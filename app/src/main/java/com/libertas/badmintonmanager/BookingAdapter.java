package com.libertas.badmintonmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class BookingAdapter extends BaseAdapter {
    private Context context;
    private List<Booking> bookings;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @Override
    public int getCount() { return bookings.size(); }

    @Override
    public Object getItem(int position) { return bookings.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_booking, parent, false);
        }

        Booking booking = bookings.get(position);

        TextView tvCourt = convertView.findViewById(R.id.tvItemCourt);
        TextView tvCustomer = convertView.findViewById(R.id.tvItemCustomer);
        TextView tvTime = convertView.findViewById(R.id.tvItemTime);
        TextView tvPrice = convertView.findViewById(R.id.tvItemPrice);
        TextView tvStatus = convertView.findViewById(R.id.tvItemStatus);

        tvCourt.setText(booking.getCourtName());
        tvCustomer.setText(booking.getCustomerName());
        tvTime.setText(booking.getDate() + " | " + booking.getTime());
        tvPrice.setText(String.format("%,d VNĐ", booking.getPrice()));
        tvStatus.setText(booking.getStatus());

        return convertView;
    }
}