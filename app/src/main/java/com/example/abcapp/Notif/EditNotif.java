package com.example.abcapp.Notif;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import com.example.abcapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EditNotif extends Fragment {
    EditText name;
    String original_name;
    Notification notification;
    Switch s;
    private int cYear, cMonth, cDay, cDayOfWeek, cHour, cMinute;
    private Calendar arrival;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_edit_notif, container, false);
        notification = (Notification) getArguments().getSerializable("notif");
        original_name = notification.getName();

        // Set name
        name = v.findViewById(R.id.name);
        name.setText(original_name);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                notification.setName(editable.toString());
            }
        });

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


        // Set date-time picker for arrival
        final View dialogView = View.inflate(getActivity(), R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

        final TextView arrival_time = v.findViewById(R.id.arrival_input);
        final TextView arrival_date = v.findViewById(R.id.arrival_input2);
        final TextView arrival_calculated = v.findViewById(R.id.arrival_calculated);
        arrival = notification.getArrival();
        if (arrival != null){
            String str_arrival_time = String.format("%02d", arrival.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", arrival.get(Calendar.MINUTE));
            arrival_time.setText(str_arrival_time);
            arrival_date.setText(formatDateString(arrival.get(Calendar.DAY_OF_WEEK),
                    arrival.get(Calendar.DAY_OF_MONTH),
                    arrival.get(Calendar.MONTH),
                    arrival.get(Calendar.YEAR)));
            updateRate(arrival_calculated);
        }

        // Set dateTxt picker
        final TextView dateTxt = v.findViewById(R.id.date);
        dateTxt.setText(formatDateString(cDayOfWeek, cDay, cMonth, cYear));
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
                                dateTxt.setText(formatDateString(cal.get(Calendar.DAY_OF_WEEK), dayOfMonth, monthOfYear, year));
                                cYear = year;
                                cMonth = monthOfYear;
                                cDay = dayOfMonth;
                                notification.setCalendar(cal);

                                updateRate(arrival_calculated);
                            }
                        }, cYear, cMonth, cDay);
                datePickerDialog.show();
            }
        });

        // Set time picker
        final TimePicker timePicker = v.findViewById(R.id.timePicker);
        timePicker.setHour(cHour);
        timePicker.setMinute(cMinute);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                Calendar cal = notification.getCalendar();
                cal.set(Calendar.HOUR_OF_DAY, i);
                cal.set(Calendar.MINUTE, i1);
                notification.setCalendar(cal);
                cHour = i;
                cMinute = i1;
                updateRate(arrival_calculated);
            }
        });

        // Set carpark name if any
        if (notification.getCarpark()!=null){
            TextView carparkTextView = v.findViewById(R.id.location_input);
            carparkTextView.setText(notification.getCarparkName());
        }

        // Set buttons/click-ables
        Button save_btn = v.findViewById(R.id.btn_save);
        Button cancel_btn = v.findViewById(R.id.btn_cancel);
        final ImageButton delete_btn = v.findViewById(R.id.btn_delete);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.arrival_layout:
                        final DatePicker arrival_datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                        final TimePicker arrival_timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                        arrival_timePicker.setHour(arrival.get(Calendar.HOUR_OF_DAY));
                        arrival_timePicker.setMinute(arrival.get(Calendar.MINUTE));

                        arrival_datePicker.updateDate(arrival.get(Calendar.YEAR),
                                arrival.get(Calendar.MONTH),
                                arrival.get(Calendar.DAY_OF_MONTH));

                        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                arrival = new GregorianCalendar(arrival_datePicker.getYear(),
                                        arrival_datePicker.getMonth(),
                                        arrival_datePicker.getDayOfMonth(),
                                        arrival_timePicker.getCurrentHour(),
                                        arrival_timePicker.getCurrentMinute());

                                notification.setArrival(arrival);

                                String str_arrival_time = String.format("%02d", arrival.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", arrival.get(Calendar.MINUTE));
                                arrival_time.setText(str_arrival_time);
                                arrival_date.setText(formatDateString(arrival.get(arrival.DAY_OF_WEEK),
                                        arrival.get(Calendar.DAY_OF_MONTH),
                                        arrival.get(Calendar.MONTH),
                                        arrival.get(Calendar.YEAR)));

                                updateRate(arrival_calculated);

                                alertDialog.dismiss();
                            }});
                        dialogView.findViewById(R.id.date_time_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                arrival = null;
                                notification.setArrival(null);
                                arrival_time.setText("None");
                                arrival_date.setText("");
                                updateRate(arrival_calculated);

                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.setView(dialogView);
                        alertDialog.show();
                        break;
                    case R.id.location_layout:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("notif", notification);
                        Navigation.findNavController(v).navigate(R.id.action_editFragment_to_carpark_fragment, bundle);
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
                        if(!saveNotification(original_name)){
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
        v.findViewById(R.id.location_layout).setOnClickListener(onClickListener);
        v.findViewById(R.id.arrival_layout).setOnClickListener(onClickListener);

        return v;
    }

    private void updateRate(TextView arrival_calculated){
        if (notification.getCarpark() == null || arrival == null){
            arrival_calculated.setText("");
            return;
        }
        long difference = notification.getCalendar().getTimeInMillis() - arrival.getTimeInMillis();
        if (difference <= 0){
            arrival_calculated.setText("");
            return;
        }
        String diff_h = "";
        double diff_h_d = difference/3600000;
        if (diff_h_d != 0){
            diff_h = (int) diff_h_d + " h ";
        }
        String diff_m = "";
        double diff_m_d = (difference%3600000)/60000;
        if (diff_m_d != 0){
            diff_m = (int) diff_m_d + " min ";
        }

        double diff_cost_d = Math.ceil(difference/(float)1800000) * notification.getCarpark().getRate();
        String diff_cost = String.format("(~ $%.2f)", diff_cost_d);

        String diff_text = "\n" + diff_h + diff_m + diff_cost;

        arrival_calculated.setText(diff_text);
    }

    private String formatDateString(int dW, int d, int m, int y){
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
        String fileName = original_name;
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
            intent.putExtra("notif_name", original_name);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), notification.getId(), intent, 0);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private void setAlarm(){
        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
        intent.putExtra("notif_id", notification.getId());
        intent.putExtra("notif_name", notification.getName());
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
        notification.setArrival(arrival);

        File nfile = new File(getActivity().getApplicationContext().getFilesDir(), nfileName);
        File file = new File(getActivity().getApplicationContext().getFilesDir(), fileName);

        // If no title, default to "Untitled"
        if (nfileName.equals("") && original_name != "") {
            nfileName = original_name;
        }
        else{
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

            if (notification.isEnabled()){
                setAlarm();
            }
            else{
                deleteAlarm();
            }

            Toast.makeText(getActivity(), "Notification saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(getActivity(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
