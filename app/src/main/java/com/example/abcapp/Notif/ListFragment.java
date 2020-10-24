package com.example.abcapp.Notif;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.abcapp.MapsActivity;
import com.example.abcapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ListFragment extends Fragment {
    protected ArrayList<Notification> notificationsList = new ArrayList<Notification>();
    protected ArrayList<Integer> notificationsIDList = new ArrayList<Integer>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_notification);
        final View v = inflater.inflate(R.layout.fragment_list_notif, container, false);

        prepareNotifications();

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle("Notifications");     //setting the title
        toolbar.setNavigationIcon(R.drawable.icon_backarrow);   // set navigation icon (back)

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(), MapsActivity.class));
            }
        });

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = 200;
                while (notificationsIDList.contains(id)){
                    id += 1;
                }
                Notification created = NotificationManager.createNotification(id);
                Bundle bundle = new Bundle();
                bundle.putSerializable("notif", created);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_editFragment, bundle);
            }
        });


        ListView listView = v.findViewById(R.id.notif_listview);
        displayNotifications(listView);

        return v;
    }

    private void displayNotifications(ListView listView){
        NotifAdapter adapter = new NotifAdapter(getActivity(), notificationsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Notification selected = (Notification) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("notifID", notificationsIDList);
                bundle.putSerializable("notif", selected);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_editFragment, bundle);
            }
        });
    }

    private void prepareNotifications(){
        File directory = getActivity().getApplicationContext().getFilesDir();
        File[] files = directory.listFiles();
        File theFile;

        notificationsIDList.clear();
        notificationsList.clear();

        for (int f = 0; f < files.length; f++) {
            theFile = files[f];
            try{
                Notification notification = NotificationManager.loadNotif(theFile);
                notificationsList.add(notification);
                notificationsIDList.add(notification.getId());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}