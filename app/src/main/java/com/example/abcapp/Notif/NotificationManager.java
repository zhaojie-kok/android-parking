package com.example.abcapp.Notif;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationManager {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static Activity activity;

    public NotificationManager(Context context, Activity activity){
        NotificationManager.context = context;
        NotificationManager.activity = activity;
        createNotificationChannel();
    }

    protected static void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "UserNotificationsChannel";
            String description = "Channel for user-set notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("userChannel", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    protected static Notification loadNotif(File theFile) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(theFile);
        ObjectInputStream oi = new ObjectInputStream(fi);

        Notification notification = (Notification) oi.readObject();
        oi.close();
        fi.close();
        return notification;
    }

    protected static Notification createNotification(int id){
        Calendar now = Calendar.getInstance();
        Notification created = new Notification("Untitled "+
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(now.getTime()), id, now);
        return created;
    }

    protected static void deleteNotification(String fileName){
        File file = new File(context.getFilesDir(), fileName);
        file.delete();
    }

    protected static boolean saveNotification(Notification notification,
                                              String original_name
                                              ){
        deleteAlarm(notification);

        // If no title, default to "Untitled" or original name, and update notification name
        String nfileName = notification.getName();
        if (nfileName.equals("") && original_name != "") {
            nfileName = original_name;
            notification.setName(nfileName);
        }
        else if (nfileName.equals("")){
            nfileName = "Untitled";
            notification.setName(nfileName);
        }

        // Get the old and new files in files dir
        File nfile = new File(context.getFilesDir(), nfileName);
        File file = new File(context.getFilesDir(), original_name);

        // If file name is changed, rename the file
        if (!original_name.equals(nfileName)){
            if (!nfile.exists()) {
                file.renameTo(nfile);
            }
            else {
                Toast.makeText(activity, "Cannot save notification - another notification exists with that name", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        try {
            FileOutputStream fo = new FileOutputStream(nfile, false);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(notification);
            os.close();
            fo.close();

            if (notification.isEnabled()){
                NotificationManager.setAlarm(notification);
            }
            else{
                NotificationManager.deleteAlarm(notification);
            }

            Toast.makeText(activity, "Notification saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(activity, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected static void deleteAlarm(Notification notification){
        boolean alarmUp = (PendingIntent.getBroadcast(activity, notification.getId(),
                new Intent(activity, ReminderBroadcast.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp){
            Intent intent = new Intent(activity, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, notification.getId(), intent, 0);
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    protected static void setAlarm(Notification notification){
        Intent intent = new Intent(activity, ReminderBroadcast.class);
        intent.putExtra("notif_id", notification.getId());
        intent.putExtra("notif_name", notification.getName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, notification.getId(), intent,0);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                notification.getCalendar().getTimeInMillis(),
                pendingIntent);
    }
}
