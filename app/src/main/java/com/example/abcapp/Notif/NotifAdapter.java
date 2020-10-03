package com.example.abcapp.Notif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.abcapp.R;

import java.util.ArrayList;
import java.util.Calendar;

public class NotifAdapter extends ArrayAdapter<Notification> {
    public NotifAdapter(Context context, ArrayList<Notification> notifications) {
        super(context,  0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Notification notification = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notif, parent, false);
        }

        Switch s = convertView.findViewById(R.id.notif_switch);
        s.setChecked(notification.isEnabled());
        TextView t = convertView.findViewById(R.id.notif_name);
        t.setText(notification.getName());
        Calendar calendar = notification.getCalendar();

        return convertView;
    }
}

