package com.example.abcapp.Notif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.abcapp.Carpark;
import com.example.abcapp.R;

import java.util.ArrayList;

public class CarparkAdapter extends ArrayAdapter<Carpark> {
    public CarparkAdapter(Context context, ArrayList<Carpark> carparks) {
        super(context,  0, carparks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Carpark carpark = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search, parent, false);
        }
        TextView name = convertView.findViewById(R.id.search_name);
        name.setText(carpark.getName());
        return convertView;
    }
}

