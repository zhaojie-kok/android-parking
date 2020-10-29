package com.example.abcapp.Notif;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.abcapp.Carparks.Carpark;
import com.example.abcapp.Carparks.CarparkList;
import com.example.abcapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;


public class EditActivity extends AppCompatActivity {

    private String original_name;
    private Notification notification;
    private Dialog dialog;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_editor);
        final int cYear, cMonth, cDay, cDayOfWeek;

        final View v = this.findViewById(android.R.id.content);
        notification = (Notification) getIntent().getExtras().getSerializable("notif");
        original_name = notification.getName();

        // Set name
        EditText name = v.findViewById(R.id.name);
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
        Switch s = v.findViewById(R.id.notif_switch);
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

        // Set date-time picker for arrival
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        final TextView arrival_time = v.findViewById(R.id.arrival_input);
        final TextView arrival_date = v.findViewById(R.id.arrival_input2);
        final TextView arrival_calculated = v.findViewById(R.id.arrival_calculated);
        Calendar arrival = notification.getArrival();
        if (arrival != null){
            String str_arrival_time = String.format("%02d", arrival.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", arrival.get(Calendar.MINUTE));
            arrival_time.setText(str_arrival_time);
            arrival_date.setText(NotificationManager.formatDateString(arrival.get(Calendar.DAY_OF_WEEK),
                    arrival.get(Calendar.DAY_OF_MONTH),
                    arrival.get(Calendar.MONTH),
                    arrival.get(Calendar.YEAR)));
            updateRate(arrival_calculated);
        }

        // Set dateTxt picker
        final TextView dateTxt = v.findViewById(R.id.date);
        dateTxt.setText(NotificationManager.formatDateString(cDayOfWeek, cDay, cMonth, cYear));
        dateTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Get Current Date
                        DatePickerDialog datePickerDialog = new DatePickerDialog(EditActivity.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        GregorianCalendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                                        dateTxt.setText(NotificationManager.formatDateString(cal.get(Calendar.DAY_OF_WEEK), dayOfMonth, monthOfYear, year));
                                        notification.setCalendar(cal);

                                        updateRate(arrival_calculated);
                                    }
                                }, cYear, cMonth, cDay);
                datePickerDialog.show();
            }
        });

        // Set time picker
        final TimePicker timePicker = v.findViewById(R.id.timePicker);
        timePicker.setHour(notification.getCalendar().get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(notification.getCalendar().get(Calendar.MINUTE));

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                Calendar cal = notification.getCalendar();
                cal.set(Calendar.HOUR_OF_DAY, i);
                cal.set(Calendar.MINUTE, i1);
                notification.setCalendar(cal);
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

                        if (notification.getArrival() == null){
                            notification.setArrival(Calendar.getInstance());
                        }
                        Calendar arrival = notification.getArrival();

                        arrival_timePicker.setHour(arrival.get(Calendar.HOUR_OF_DAY));
                        arrival_timePicker.setMinute(arrival.get(Calendar.MINUTE));

                        arrival_datePicker.updateDate(arrival.get(Calendar.YEAR),
                                arrival.get(Calendar.MONTH),
                                arrival.get(Calendar.DAY_OF_MONTH));

                        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Calendar arrival = new GregorianCalendar(arrival_datePicker.getYear(),
                                        arrival_datePicker.getMonth(),
                                        arrival_datePicker.getDayOfMonth(),
                                        arrival_timePicker.getCurrentHour(),
                                        arrival_timePicker.getCurrentMinute());

                                notification.setArrival(arrival);

                                String str_arrival_time = String.format("%02d", arrival.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", arrival.get(Calendar.MINUTE));
                                arrival_time.setText(str_arrival_time);
                                arrival_date.setText(NotificationManager.formatDateString(arrival.get(arrival.DAY_OF_WEEK),
                                        arrival.get(Calendar.DAY_OF_MONTH),
                                        arrival.get(Calendar.MONTH),
                                        arrival.get(Calendar.YEAR)));

                                updateRate(arrival_calculated);

                                alertDialog.dismiss();
                            }});
                        dialogView.findViewById(R.id.date_time_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                        TextView carparkTextView = v.findViewById(R.id.location_input);
                        showSelectCarparkPopup(carparkTextView);
                        break;
                    case R.id.btn_delete:
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE){
                                    NotificationManager.deleteAlarm(notification);
                                    NotificationManager.deleteNotification(original_name);
                                    navigateToNotificationActivity();
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                        builder.setMessage("Are you sure you want to delete this?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        break;
                    case R.id.btn_save:
                        if(!NotificationManager.saveNotification(notification, original_name)){
                            break;
                        };
                    default:
                        navigateToNotificationActivity();
                }
            }
        };

        save_btn.setOnClickListener(onClickListener);
        cancel_btn.setOnClickListener(onClickListener);
        delete_btn.setOnClickListener(onClickListener);
        v.findViewById(R.id.location_layout).setOnClickListener(onClickListener);
        v.findViewById(R.id.arrival_layout).setOnClickListener(onClickListener);
    }

    private void updateRate(TextView rateTextView){
        if (notification.getCarpark() == null || notification.getArrival() == null){
            rateTextView.setText("");
            return;
        }
        long difference = notification.getCalendar().getTimeInMillis() - notification.getArrival().getTimeInMillis();
        if (difference <= 0){
            rateTextView.setText("");
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

        rateTextView.setText(diff_text);
    }

    private void showSelectCarparkPopup(final TextView carparkTextView) {
        final CarparkAdapter adapter = new CarparkAdapter(EditActivity.this, NotificationManager.getCarparkList());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        final View v = getLayoutInflater().inflate(R.layout.select_carpark_popup, null, false);

        final SearchView searchView = v.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        ListView listView = v.findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                searchView.clearFocus();
                Carpark selected = (Carpark) parent.getItemAtPosition(position);
                notification.setCarpark(selected);
                carparkTextView.setText(notification.getCarparkName());
                dialog.dismiss();
            }
        });

        // display the pop up
        dialogBuilder.setView(v);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void navigateToNotificationActivity(){
        Intent intent = new Intent(EditActivity.this, NotificationActivity.class);
        startActivity(intent);
    }
}
