package com.nexora.elegance.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.nexora.elegance.utils.NotificationHelper;

/**
 * BackgroundSyncWorker is a periodic worker that ensures the app's local data
 * is synchronized with the Firestore backend.
 */
public class BackgroundSyncWorker extends Worker {
    public BackgroundSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Result.success();
        }

        String uid = user.getUid();
        android.content.SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("order_status_prefs", Context.MODE_PRIVATE);

        try {
            // Fetch orders from Firestore synchronously
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task = 
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("orders")
                    .whereEqualTo("userId", uid)
                    .get();

            com.google.firebase.firestore.QuerySnapshot querySnapshot = com.google.android.gms.tasks.Tasks.await(task);

            for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                String orderId = doc.getId();
                String currentStatus = doc.getString("status");
                
                if (currentStatus == null) continue;

                String lastStatus = prefs.getString("status_" + orderId, null);

                // If status has changed, show notification
                if (lastStatus != null && !lastStatus.equals(currentStatus)) {
                    String message = "Your order #" + orderId + " is now " + currentStatus.toLowerCase() + ".";
                    NotificationHelper.showOrderNotification(getApplicationContext(), 
                            "Order Update", message, orderId, null);
                }

                // Save the current status for next comparison
                prefs.edit().putString("status_" + orderId, currentStatus).apply();
            }
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            android.util.Log.e("BackgroundSyncWorker", "Error fetching orders", e);
            return Result.retry();
        }

        return Result.success();
    }
}
