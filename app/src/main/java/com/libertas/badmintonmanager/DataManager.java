package com.libertas.badmintonmanager;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "BadmintonManagerData";
    private static final String KEY_USERS = "users";
    private static final String KEY_BOOKINGS = "bookings";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private SharedPreferences prefs;
    private Gson gson;

    public DataManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        if (getUsers().isEmpty()) {
            List<User> defaultUsers = new ArrayList<>();
            defaultUsers.add(new User("admin", "admin123", "admin", "Admin", "0904620940"));
            defaultUsers.add(new User("user", "user123", "user", "Khách hàng", "0123456789"));
            saveUsers(defaultUsers);
        }
    }

    // User Management
    public List<User> getUsers() {
        String json = prefs.getString(KEY_USERS, "[]");
        Type type = new TypeToken<List<User>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void saveUsers(List<User> users) {
        String json = gson.toJson(users);
        prefs.edit().putString(KEY_USERS, json).apply();
    }

    public User getUser(String username, String password) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        List<User> users = getUsers();
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                return false; // Username already exists
            }
        }
        users.add(user);
        saveUsers(users);
        return true;
    }

    // Booking Management
    public List<Booking> getBookings() {
        String json = prefs.getString(KEY_BOOKINGS, "[]");
        Type type = new TypeToken<List<Booking>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void saveBookings(List<Booking> bookings) {
        String json = gson.toJson(bookings);
        prefs.edit().putString(KEY_BOOKINGS, json).apply();
    }

    public void addBooking(Booking booking) {
        List<Booking> bookings = getBookings();
        bookings.add(booking);
        saveBookings(bookings);
    }

    public void deleteBooking(String bookingId) {
        List<Booking> bookings = getBookings();
        bookings.removeIf(b -> b.getId().equals(bookingId));
        saveBookings(bookings);
    }

    public void updateBooking(Booking updatedBooking) {
        List<Booking> bookings = getBookings();
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getId().equals(updatedBooking.getId())) {
                bookings.set(i, updatedBooking);
                break;
            }
        }
        saveBookings(bookings);
    }

    public List<Booking> getBookingsByUser(String username) {
        List<Booking> allBookings = getBookings();
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (booking.getCustomerName().equals(username)) {
                userBookings.add(booking);
            }
        }
        return userBookings;
    }

    // Notification Management
    public List<Notification> getNotifications(String username) {
        String json = prefs.getString(KEY_NOTIFICATIONS + "_" + username, "[]");
        Type type = new TypeToken<List<Notification>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void saveNotifications(String username, List<Notification> notifications) {
        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS + "_" + username, json).apply();
    }

    public void addNotification(String username, Notification notification) {
        List<Notification> notifications = getNotifications(username);
        notifications.add(0, notification);
        saveNotifications(username, notifications);
    }

    public int getUnreadNotificationCount(String username) {
        List<Notification> notifications = getNotifications(username);
        int count = 0;
        for (Notification notif : notifications) {
            if (!notif.isRead()) count++;
        }
        return count;
    }

    public void markNotificationAsRead(String username, String notificationId) {
        List<Notification> notifications = getNotifications(username);
        for (Notification notif : notifications) {
            if (notif.getId().equals(notificationId)) {
                notif.setRead(true);
                break;
            }
        }
        saveNotifications(username, notifications);
    }
}