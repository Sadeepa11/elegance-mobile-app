package com.nexora.elegance.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.nexora.elegance.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        if (title == null)
            title = "Elegance Update";
        String message = intent.getStringExtra("message");
        if (message == null)
            message = "New fashion trends arrived!";

        NotificationHelper.showNotification(context, title, message);
    }
}
