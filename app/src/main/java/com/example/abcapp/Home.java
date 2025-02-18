package com.example.abcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class Home extends AppCompatActivity {

    private ImageButton startButton; // Start button on Home page
    private ImageButton notifButton; // Notif button on Home page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//         Start button
        startButton = (ImageButton) findViewById(R.id.startButton);
        System.out.println();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

//         Notif button
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
}