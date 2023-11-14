package com.example.myapplication.Broadcasts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class AlertReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "vacation_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlertReceiver", "Alarm received");

        createNotificationChannel(context);

        String vacationTitle = intent.getStringExtra("VACATION_TITLE");
        boolean isStarting = intent.getBooleanExtra("STARTING", false);
        String message = isStarting ? "Your vacation '" + vacationTitle + "' is starting!" : "Your vacation '" + vacationTitle + "' is ending!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)  // Replace with your notification icon
                .setContentTitle("Vacation Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(vacationTitle.hashCode(), builder.build());

    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vacation Notification Channel";
            String description = "Channel for Vacation Alarms";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
