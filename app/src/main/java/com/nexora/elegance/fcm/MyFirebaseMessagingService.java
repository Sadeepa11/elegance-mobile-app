package com.nexora.elegance.fcm;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import com.nexora.elegance.utils.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * MyFirebaseMessagingService handles receiving push notifications from Firebase Cloud Messaging (FCM).
 * It also handles the generation and refreshing of the registration token.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        // Log the message receipt
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);
            
            // Show notification using our helper
            NotificationHelper.showNotification(this, title, body);
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String orderId = remoteMessage.getData().get("orderId");
            
            if (title != null && body != null) {
                NotificationHelper.showNotification(this, title, body);
            }

            // Send local broadcast to refresh UI if needed
            Intent intent = new Intent("com.nexora.elegance.ORDER_UPDATED");
            if (orderId != null) {
                intent.putExtra("orderId", orderId);
            }
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // Here you would typically send the token to your server to track the device
    }
}
