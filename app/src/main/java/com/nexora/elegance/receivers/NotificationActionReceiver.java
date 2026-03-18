package com.nexora.elegance.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * NotificationActionReceiver handles actions from system notifications,
 * such as "Mark as Read" (dismissing the notification).
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationAction";
    public static final String ACTION_MARK_AS_READ = "com.nexora.elegance.ACTION_MARK_AS_READ";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if (ACTION_MARK_AS_READ.equals(intent.getAction())) {
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            if (notificationId != -1) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(notificationId);
                    Log.d(TAG, "Notification " + notificationId + " dismissed.");
                }
            }
        }
    }
}
