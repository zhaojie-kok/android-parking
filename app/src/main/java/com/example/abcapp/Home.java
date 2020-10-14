package com.example.abcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.abcapp.Notif.NotifActivity;

public class Home extends AppCompatActivity {

    private ImageButton startButton; // Start button on Home page
    private ImageButton notifButton; // Notif button on Home page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Start button
        startButton = findViewById(R.id.startBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

        // Notif button
//        notifButton = findViewById(R.id.notifBtn);
//        notifButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openNotifActivity();
//            }
//        });
    }

    // Method to change activity to MapsActivity
    public void openMapActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }

    // Method to change activity to NotifActivity
    public void openNotifActivity() {
        Intent intent = new Intent(this, NotifActivity.class);
        startActivity(intent);

    }
}