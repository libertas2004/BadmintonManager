package com.libertas.badmintonmanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class NotificationAdapter extends BaseAdapter {

    private final Context context;
    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationRead(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notifications,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_notification, parent, false);
        }

        Notification notification = notifications.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvNotificationTitle);
        TextView tvMessage = convertView.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = convertView.findViewById(R.id.tvNotificationTime);
        Button btnClose = convertView.findViewById(R.id.btnClose);
        View viewUnread = convertView.findViewById(R.id.viewUnreadIndicator);

        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());
        tvTime.setText(notification.getTimestamp());

        // Show unread indicator
        if (!notification.isRead()) {
            viewUnread.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            viewUnread.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE);
        }

        btnClose.setOnClickListener(v -> {
            if (!notification.isRead()) {
                listener.onNotificationRead(notification);
            }
        });

        return convertView;
    }
}