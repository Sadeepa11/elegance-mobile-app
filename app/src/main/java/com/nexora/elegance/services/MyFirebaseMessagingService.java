package com.nexora.elegance.services;

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
            String itemsJson = remoteMessage.getData().get("items");
            
            if ("category_sync".equals(remoteMessage.getData().get("type"))) {
                if (title == null) title = "New Collection Available!";
                if (body == null) body = "Check out our latest categories and trends.";
                NotificationHelper.showGeneralNotification(this, title, body);
            } else {
                if (title == null) title = "Order Status Update";
                if (body == null) body = "Your order status has been updated.";
                NotificationHelper.showOrderNotification(this, title, body, orderId, itemsJson);
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
        
        // Save token to Firestore if user is logged in
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update token", e));
        }
    }
}
