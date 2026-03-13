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
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notifications for order status changes");
                channel.enableLights(true);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);

            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
