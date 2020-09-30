package com.example.abcapp.Notif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.abcapp.R;

import java.util.ArrayList;

public class NotifAdapter extends ArrayAdapter<String> {
    public NotifAdapter(Context context, ArrayList<String> notifications) {
        super(context,  0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String location = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search, parent, false);
        }
        TextView icon = convertView.findViewById(R.id.item_icon);
        TextView name = convertView.findViewById(R.id.item_name);
        if (position == 0){
            icon.setText(String.valueOf(location.charAt(0)));
        }
        else if (location.charAt(0) != getItem(position-1).charAt(0)){
            icon.setText(String.valueOf(location.charAt(0)));
        }
        else{
            icon.setText("");
        }
        name.setText(location);
        return convertView;
    }
}

