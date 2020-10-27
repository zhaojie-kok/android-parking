package com.example.abcapp.Notif;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.abcapp.MapsActivity;
import com.example.abcapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class NotificationActivity extends AppCompatActivity {

    protected ArrayList<Notification> notificationsList = new ArrayList<Notification>();
    protected ArrayList<Integer> notificationsIDList = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        final NotificationManager notificationManager = new NotificationManager(this.getApplicationContext(), this);

        prepareNotificationsList();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Notifications");     //setting the title
        toolbar.setNavigationIcon(R.drawable.icon_backarrow);   // set navigation icon (back)

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotificationActivity.this, MapsActivity.class));
            }
        });

        // Create new notification
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = 200;
                while (notificationsIDList.contains(id)){
                    id += 1;
                }
                Notification created = notificationManager.createNotification(id);
                navigateToEditActivity(created);
            }
        });

        ListView listView = findViewById(R.id.notif_listview);
        displayNotifications(listView);

    }

    private void displayNotifications(ListView listView){
        NotificationAdapter adapter = new NotificationAdapter(this, notificationsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Notification selected = (Notification) parent.getItemAtPosition(position);
                navigateToEditActivity(selected);
            }
        });
    }

    private void prepareNotificationsList(){
        File directory = this.getApplicationContext().getFilesDir();
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

    private void navigateToEditActivity(Notification toEdit){
        Intent intent = new Intent(NotificationActivity.this, EditActivity.class);
        intent.putExtra("notif", toEdit);
        startActivity(intent);
    }
}
