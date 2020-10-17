package com.example.abcapp.Notif.ui.notificationList;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.abcapp.Notif.NotifAdapter;
import com.example.abcapp.Notif.Notification;
import com.example.abcapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ListFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_notification);
        final View v = inflater.inflate(R.layout.fragment_list_notif, container, false);

        prepareNotifications();

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = 200;
                while (notificationsIDList.contains(id)){
                    id += 1;
                }
                Calendar now = Calendar.getInstance();
                Notification created = new Notification("Untitled "+
                        new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(now.getTime()), id, now);
                Bundle bundle = new Bundle();
                bundle.putSerializable("notif", created);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_editFragment, bundle);
            }
        });

        NotifAdapter adapter = new NotifAdapter(getActivity(), notificationsList);
        ListView listView = v.findViewById(R.id.notif_listview);
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

        return v;
/*

        // Set location
        final TextView l = v.findViewById(R.id.location);
        l.setText(NotifActivity.carpark);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to choose carpark
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_location);
            }
        });

        // Switch for the reminder
        final Switch r = v.findViewById(R.id.reminder);
        // Check if an alarm is already set
        PendingIntent checkIntent = PendingIntent.getBroadcast(getContext(), 200,
                new Intent(getActivity(), ReminderBroadcast.class),
                PendingIntent.FLAG_NO_CREATE);

        boolean alarmUp = (checkIntent != null);

        if (alarmUp)
        {
            r.setChecked(true);
            l.setText(checkIntent.toString());
        }

        // if clicked, open TimePickerDialog
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (r.isChecked()){
                    TimePickerDialog reminderTime;
                    final Calendar currentTime = Calendar.getInstance();
                    // by default, set TimePickerDialog to current time
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int min = currentTime.get(Calendar.MINUTE);
                    reminderTime = new TimePickerDialog(getActivity(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            c.set(Calendar.MINUTE, minute);
                            c.set(Calendar.SECOND, 0);
                            if (c.before(currentTime)){
                                c.add(Calendar.DATE, 1);
                            }

                    Toast.makeText(getActivity(), "Reminder set", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 200, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            c.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent);

                        }
                    }, hour, min, true);
                    // if cancelled, turn off reminder
                    reminderTime.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            r.setChecked(false);
                        }
                    });
                    reminderTime.show();
                }
                else {
                    Toast.makeText(getActivity(), "Reminder cancelled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 200, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        });
        */
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
                Notification notification = loadNotif(theFile);
                notificationsList.add(notification);
                notificationsIDList.add(notification.getId());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private Notification loadNotif(File theFile) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(theFile);
        ObjectInputStream oi = new ObjectInputStream(fi);

        Notification notification = (Notification) oi.readObject();
        oi.close();
        fi.close();
        return notification;
    }

    protected ArrayList<Notification> notificationsList = new ArrayList<Notification>();
    protected ArrayList<Integer> notificationsIDList = new ArrayList<Integer>();
}