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
        
        // Always show the sync notification when the worker starts, as requested by USER
        NotificationHelper.showGeneralNotification(getApplicationContext(), "App Sync", "Elegance data synchronized successfully.");

        if (user == null) {
            return Result.success();
        }

        String uid = user.getUid();
        android.content.SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("order_status_prefs", Context.MODE_PRIVATE);

        try {
            // 1. Sync Order Statuses
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> orderTask = 
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("orders")
                    .whereEqualTo("userId", uid)
                    .get();

            com.google.firebase.firestore.QuerySnapshot orderSnapshot = com.google.android.gms.tasks.Tasks.await(orderTask);

            for (com.google.firebase.firestore.DocumentSnapshot doc : orderSnapshot) {
                String orderId = doc.getId();
                String currentStatus = doc.getString("status");
                if (currentStatus == null) continue;
                String lastStatus = prefs.getString("status_" + orderId, null);
                if (lastStatus != null && !lastStatus.equals(currentStatus)) {
                    String message = "Your order #" + orderId + " is now " + currentStatus.toLowerCase() + ".";
                    NotificationHelper.showOrderNotification(getApplicationContext(), "Order Update", message, orderId, null);
                }
                prefs.edit().putString("status_" + orderId, currentStatus).apply();
            }

            // 2. Sync Categories
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> categoryTask = 
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("categories")
                    .get();

            com.google.firebase.firestore.QuerySnapshot categorySnapshot = com.google.android.gms.tasks.Tasks.await(categoryTask);
            int currentCategoryCount = categorySnapshot.size();
            int lastCategoryCount = prefs.getInt("category_count", 0);

            if (currentCategoryCount > lastCategoryCount && lastCategoryCount != 0) {
                NotificationHelper.showGeneralNotification(getApplicationContext(), 
                        "New Collection Available!", 
                        "Check out our latest categories and trends in Elegance.");
            }
            prefs.edit().putInt("category_count", currentCategoryCount).apply();

        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            android.util.Log.e("BackgroundSyncWorker", "Error during background sync", e);
            scheduleNextSync(); // Still schedule next one even on failure
            return Result.retry();
        }

        // 3. Self-Reschedule for 10 minutes
        scheduleNextSync();

        return Result.success();
    }

    private void scheduleNextSync() {
        androidx.work.OneTimeWorkRequest nextRequest = new androidx.work.OneTimeWorkRequest.Builder(BackgroundSyncWorker.class)
                .setInitialDelay(10, java.util.concurrent.TimeUnit.MINUTES)
                .build();
        androidx.work.WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                "CategorySyncWork", 
                androidx.work.ExistingWorkPolicy.REPLACE, 
                nextRequest);
    }
}
