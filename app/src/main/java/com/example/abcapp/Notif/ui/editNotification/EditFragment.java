package com.example.abcapp.Notif.ui.editNotification;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.abcapp.Notif.Notification;
import com.example.abcapp.Notif.ReminderBroadcast;
import com.example.abcapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EditFragment extends Fragment {
    EditText name;
    Notification notification;
    Switch s;
    private int cYear, cMonth, cDay, cDayOfWeek, cHour, cMinute;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_edit_notif, container, false);
        notification = (Notification) getArguments().getSerializable("notif");

        // Set name
        name = v.findViewById(R.id.name);
        name.setText(notification.getName());
        name.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set switch (on if enabled, off if not)
        s = v.findViewById(R.id.notif_switch);
        s.setChecked(notification.isEnabled());

        // If switch is clicked, update notification
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notification.toggleEnabled();
            }
        });

        // Get calendar information
        Calendar c = notification.getCalendar();
        cYear = c.get(Calendar.YEAR);
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);
        cDayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        cHour = c.get(Calendar.HOUR_OF_DAY);
        cMinute = c.get(Calendar.MINUTE);

        // Set dateTxt picker
        final TextView dateTxt = v.findViewById(R.id.date);
        dateTxt.setText(getDateString(cDayOfWeek, cDay, cMonth, cYear));
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Date
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                GregorianCalendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                                dateTxt.setText(getDateString(cal.get(Calendar.DAY_OF_WEEK), dayOfMonth, monthOfYear, year));
                                cYear = year;
                                cMonth = monthOfYear;
                                cDay = dayOfMonth;
                            }
                        }, cYear, cMonth, cDay);
                datePickerDialog.show();
            }
        });

        // Set time picker
        TimePicker timePicker = v.findViewById(R.id.timePicker);
        timePicker.setHour(cHour);
        timePicker.setMinute(cMinute);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                cHour = i;
                cMinute = i1;
            }
        });

        // Set buttons/click-ables
        Button save_btn = v.findViewById(R.id.btn_save);
        Button cancel_btn = v.findViewById(R.id.btn_cancel);
        final ImageButton delete_btn = v.findViewById(R.id.btn_delete);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.location_name:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("notif", notification);
                        Navigation.findNavController(v).navigate(R.id.action_editFragment_to_carpark_fragment);
                        break;
                    case R.id.btn_delete:
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE){
                                    deleteNotification();
                                    deleteAlarm();
                                    Navigation.findNavController(v).navigate(R.id.action_editFragment_to_navigation_home);
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Are you sure you want to delete this?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        break;
                    case R.id.btn_save:
                        if(!saveNotification(notification.getName())){
                            break;
                        };
                    default:
                        Navigation.findNavController(v).navigate(R.id.action_editFragment_to_navigation_home);
                }
            }
        };

        save_btn.setOnClickListener(onClickListener);
        cancel_btn.setOnClickListener(onClickListener);
        delete_btn.setOnClickListener(onClickListener);
        TextView location_name = v.findViewById(R.id.location_name);
        location_name.setOnClickListener(onClickListener);

        return v;
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
                y;
    }

    private void deleteNotification(){
        String fileName = notification.getName();
        File file = new File(getActivity().getApplicationContext().getFilesDir(), fileName);
        file.delete();
    }

    private void deleteAlarm(){
        boolean alarmUp = (PendingIntent.getBroadcast(getContext(), notification.getId(),
                new Intent(getActivity(), ReminderBroadcast.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp){
            Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
            intent.putExtra("notif_id", notification.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), notification.getId(), intent, 0);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private void setAlarm(){
        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
        intent.putExtra("notif_id", notification.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), notification.getId(), intent,0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                notification.getCalendar().getTimeInMillis(),
                pendingIntent);
    }

    private boolean saveNotification(String fileName){
        String nfileName = name.getText().toString();
        deleteAlarm();

        notification.setCalendar(new GregorianCalendar(cYear, cMonth,
                cDay, cHour, cMinute));

        File nfile = new File(getActivity().getApplicationContext().getFilesDir(), nfileName);
        File file = new File(getActivity().getApplicationContext().getFilesDir(), fileName);

        // If no title, default to "Untitled"
        if (nfileName.equals("")) {
            nfileName = "Untitled";
        }
        // If file name is changed, rename the file
        if (!fileName.equals(nfileName)){
            if (!nfile.exists()) {
                file.renameTo(nfile);
                notification.setName(nfileName);
            }
            else {
                Toast.makeText(getActivity(), "Cannot save notification - another notification exists with that name", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        try {
            FileOutputStream fo = new FileOutputStream(nfile, false);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(notification);
            os.close();
            fo.close();

            setAlarm();

            Toast.makeText(getActivity(), "Notification saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(getActivity(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
