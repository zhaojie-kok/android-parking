package com.example.abcapp.Notif;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.abcapp.R;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // Create an Intent for the activity you want to start
        Intent openIntent = new Intent(context, NotificationActivity.class);

        int id = intent.getIntExtra("notif_id", 0);
        String name = intent.getStringExtra("notif_name");

        PendingIntent reminderIntent = PendingIntent.getActivity(context, id, openIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "userChannel")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(name)
                .setContentText("Sent from ABC app")
                .setContentIntent(reminderIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());

    }
}
