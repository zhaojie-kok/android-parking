package com.example.abcapp.Notif.ui.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.abcapp.Notif.NotifActivity;
import com.example.abcapp.Notif.ReminderBroadcast;
import com.example.abcapp.R;
import java.util.Calendar;


public class NotifFragment extends Fragment {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_home);
        final View v = inflater.inflate(R.layout.fragment_notif, container, false);
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
        return v;
    }
}
