package com.example.abcapp.Notif;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcapp.R;


public class NotifActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        NotificationManager notificationManager = new NotificationManager(this.getApplicationContext(), this);
    }
}
