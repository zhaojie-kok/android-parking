package com.example.abcapp;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton weatherButton = findViewById(R.id.weatherButton);
        ImageButton menuButton = findViewById(R.id.menuButton);

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "weatherButton click", Toast.LENGTH_SHORT).show();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "menuButton click", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int fine_loc_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fine_loc_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        System.out.println("height=" + height);

//        mMap.setPadding(0, height-400, 0, 0);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        Location prevLoc = prevKnownLocation();
        LatLng prevLatLng = null;
        if (prevLoc != null) {
            prevLatLng = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
        } else {
            Toast.makeText(MapsActivity.this, "location information unavailable,\nusing default location", Toast.LENGTH_SHORT);
            prevLatLng = new LatLng(1.290270, 103.851959);
        }
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevLatLng, 15.0f));
    }

    @SuppressLint("MissingPermission")
    public Location prevKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long gpsLocRecent = 0;
        long networkLocRecent = 0;

        gpsLocRecent = (gpsLoc != null) ? gpsLoc.getTime() : Long.MIN_VALUE;
        networkLocRecent = (null != networkLoc) ? networkLoc.getTime() : Long.MIN_VALUE;
         // return the location of the more recently available location
        if (gpsLocRecent>networkLocRecent) {
            return gpsLoc;
        } else if (networkLoc != null){
            return networkLoc;
        } else {
            return null;
        }
    }
}