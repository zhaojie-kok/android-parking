package com.example.abcapp.Notif;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abcapp.Carparks.Carpark;
import com.example.abcapp.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        super(context,  0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Notification notification = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notif, parent, false);
        }

        final Switch s = convertView.findViewById(R.id.notif_switch);
        s.setChecked(notification.isEnabled());
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NotificationManager.checkNotificationCal(notification)){
                    notification.toggleEnabled();
                    NotificationManager.saveNotification(notification, notification.getName());
                }
                s.setChecked(notification.isEnabled());
            }
        });
        TextView t_name = convertView.findViewById(R.id.notif_name);
        t_name.setText(notification.getName());

        Calendar calendar = notification.getCalendar();
        String time = "" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
        String date = NotificationManager.formatDateString(calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR));
        TextView t_time = convertView.findViewById(R.id.notif_time);
        TextView t_date = convertView.findViewById(R.id.notif_date);

        t_date.setText(date);
        t_time.setText(time);

        return convertView;
    }
}

