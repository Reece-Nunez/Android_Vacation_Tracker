package com.example.myapplication.Broadcasts;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String vacationTitle = intent.getStringExtra("VACATION_TITLE");
        boolean isStarting = determineIfStarting(intent);

        createNotification(context, vacationTitle, isStarting);
    }

    private boolean determineIfStarting(Intent intent) {
        return intent.getBooleanExtra("STARTING", false);
    }

    private void createNotification(Context context, String vacationTitle, boolean isStarting) {
        String message = isStarting ? "Vacation is starting" : "Vacation is ending";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "VACATION_CHANNEL")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(vacationTitle)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1
            );
            return;
        }
        notificationManager.notify(1, builder.build());

    }
}
