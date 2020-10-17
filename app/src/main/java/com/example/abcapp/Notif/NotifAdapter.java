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

public class NotifAdapter extends ArrayAdapter<Notification> {
    public NotifAdapter(Context context, ArrayList<Notification> notifications) {
        super(context,  0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Notification notification = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notif, parent, false);
        }

        Switch s = convertView.findViewById(R.id.notif_switch);
        s.setChecked(notification.isEnabled());
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    notification.toggleEnabled();
                    toggleAlarm(notification);
                    File file = new File(getContext().getApplicationContext().getFilesDir(), notification.getName());
                    FileOutputStream fo = new FileOutputStream(file, false);
                    ObjectOutputStream os = new ObjectOutputStream(fo);
                    os.writeObject(notification);
                    os.close();
                    fo.close();
                    Toast.makeText(getContext(), "Notification saved!", Toast.LENGTH_SHORT).show();
                } catch (Throwable t) {
                    Toast.makeText(getContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        TextView t_name = convertView.findViewById(R.id.notif_name);
        t_name.setText(notification.getName());

        Calendar calendar = notification.getCalendar();
        String time = "" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
        String date = getDateString(calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR));
        TextView t_time = convertView.findViewById(R.id.notif_time);

        s.setText(date);
        t_time.setText(time);

        return convertView;
    }

    private void toggleAlarm(Notification notification){
        boolean alarmUp = (PendingIntent.getBroadcast(getContext(), notification.getId(),
                new Intent(getContext(), ReminderBroadcast.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp){
            Intent intent = new Intent(getContext(), ReminderBroadcast.class);
            intent.putExtra("notif_id", notification.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), notification.getId(), intent, 0);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else{
            Intent intent = new Intent(getContext(), ReminderBroadcast.class);
            intent.putExtra("notif_id", notification.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), notification.getId(), intent,0);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    notification.getCalendar().getTimeInMillis(),
                    pendingIntent);
        }
    }

    private String getDateString(int dW, int d, int m, int y){
        String extra = "";
        Calendar now = Calendar.getInstance();
        if (d == now.get(Calendar.DAY_OF_MONTH) &&
                m == now.get(Calendar.MONTH) &&
                y == now.get(Calendar.YEAR)){
            extra = "Today - ";
        }
        else {
            now.add(Calendar.DATE, 1);
            if (d == now.get(Calendar.DAY_OF_MONTH) &&
                    m == now.get(Calendar.MONTH) &&
                    y == now.get(Calendar.YEAR)){
                extra = "Tomorrow - ";
            }
        }
        extra += new String[]{"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"}[dW - 1];

        return extra + " " + d + " " +
                new String[]{"Jan ", "Feb ", "Mar ", "Apr ", "May ", "Jun ",
                        "Jul ", "Aug ", "Oct ", "Nov ", "Dec "}[m - 1] +
                "\'" + y%100;
    }
}

