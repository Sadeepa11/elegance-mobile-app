package com.nexora.elegance.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.nexora.elegance.MainActivity;
import com.nexora.elegance.R;

/**
 * NotificationHelper provides static methods to display system notifications.
 * It handles notification channel creation for Android Oreo (API 26) and above.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "EleganceChannel";
    private static final String CHANNEL_NAME = "Order Updates";

    public static void showNotification(Context context, String title, String message) {
        showOrderNotification(context, title, message, null, null);
    }

    public static void showOrderNotification(Context context, String title, String message, String orderId, String itemsJson) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notifications for order status changes");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }

            int notificationId = (int) System.currentTimeMillis();

            // Mark as Read Action
            Intent markAsReadIntent = new Intent(context, com.nexora.elegance.receivers.NotificationActionReceiver.class);
            markAsReadIntent.setAction(com.nexora.elegance.receivers.NotificationActionReceiver.ACTION_MARK_AS_READ);
            markAsReadIntent.putExtra(com.nexora.elegance.receivers.NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
            
            int broadcastFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                broadcastFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent markAsReadPendingIntent = PendingIntent.getBroadcast(context, notificationId + 2, markAsReadIntent, broadcastFlags);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setColor(android.graphics.Color.parseColor("#FF4D6D"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .addAction(0, "Mark as Read", markAsReadPendingIntent);

            if (orderId != null) {
                // View Action - Only for orders
                Intent viewIntent = new Intent(context, com.nexora.elegance.ui.orders.OrderDetailsActivity.class);
                viewIntent.putExtra("orderId", orderId);
                viewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }
                PendingIntent viewPendingIntent = PendingIntent.getActivity(context, notificationId + 1, viewIntent, flags);
                
                builder.setContentIntent(viewPendingIntent);
                builder.addAction(0, "View", viewPendingIntent);
            } else {
                // Category Sync or General Notification - No specific activity
                Intent mainIntent = new Intent(context, MainActivity.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }
                PendingIntent mainPendingIntent = PendingIntent.getActivity(context, notificationId, mainIntent, flags);
                builder.setContentIntent(mainPendingIntent);
            }

            notificationManager.notify(notificationId, builder.build());
        }
    }
}
