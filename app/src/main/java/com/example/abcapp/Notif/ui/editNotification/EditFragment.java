package com.example.abcapp.Notif.ui.editNotification;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.example.abcapp.Notif.NotifActivity;
import com.example.abcapp.Notif.Notification;
import com.example.abcapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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

        // Set save button
        Button save_btn = v.findViewById(R.id.btn_save);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveNotification(notification.getName());
                    Navigation.findNavController(v).navigate(R.id.action_editFragment_to_navigation_home);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set cancel button
        Button cancel_btn = v.findViewById(R.id.btn_cancel);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(v).navigate(R.id.action_editFragment_to_navigation_home);
            }
        });

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

    private void saveNotification(String fileName) throws IOException {
        String nfileName = name.getText().toString();

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
                return;
            }
        }
        try {
            FileOutputStream fo = new FileOutputStream(nfile, false);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(notification);
            os.close();
            fo.close();
            Toast.makeText(getActivity(), "Note saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(getActivity(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

    }
}
