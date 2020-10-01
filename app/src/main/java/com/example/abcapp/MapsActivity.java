package com.example.abcapp;

import org.json.JSONObject;
import org.json.JSONArray;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;

import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // for getting location
    private ImageButton locationButton;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean trackingLocation = false;
    private Location prevLoc;

    // for menu drawer
    ImageButton menuButton;
    private DrawerLayout drawer;
    private NavigationView navView;

    // for weather pop up
    ImageButton weatherButton;
    Button weatherClose;
    private PopupWindow weather_popup;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView currWeather, predWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // set up map and location relevant objects
        // Obtain the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // make fused location client, requests and callbacks
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Toast.makeText(MapsActivity.this, locationResult.toString(), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(MapsActivity.this, "no loc", Toast.LENGTH_SHORT).show();
                }
                // find a valid result in the returned location results to use
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        prevLoc = location;
                    }
                }
            }
        };

        // make popup window for weather
        weather_popup = new PopupWindow();

        // make menu layout and views
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        navView = (NavigationView) findViewById(R.id.navView);

        // make buttons
        weatherButton = findViewById(R.id.weatherButton);
        menuButton = findViewById(R.id.menuButton);
        locationButton = findViewById(R.id.locationButton);

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createWeatherPopup();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "find loc", Toast.LENGTH_SHORT).show();
                getLocation(true);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // request permissions for user location
        int fine_loc_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fine_loc_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // enable map gestures like zoom and sliding
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // request for location updates and shift map camera
        getLocation(true);
//        Toast.makeText(this, prevLoc.toString(), Toast.LENGTH_SHORT).show();
    }

    // getting location from fusedLocationClient
    public void getLocation(final boolean moveCam) {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (!trackingLocation) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            trackingLocation = true;
            getLocation(moveCam);
        } else {
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        prevLoc = location;
                        if (moveCam) {
                            LatLng prevLatlng = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(prevLatlng));
                        } else {
                            Toast.makeText(MapsActivity.this, "unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        trackingLocation = false;
                        Toast.makeText(MapsActivity.this, "unable to retrieve location2", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // method to display weather popup
    public void createWeatherPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View weatherPopup = getLayoutInflater().inflate(R.layout.weather_popup, null);

        // instantiate the weather texts
        currWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoNow);
        predWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoForecast);

        // instantiate the close button
        weatherClose = (Button) weatherPopup.findViewById(R.id.closePopup);
        weatherPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // display the pop up
        dialogBuilder.setView(weatherPopup);
        dialog = dialogBuilder.create();
        dialog.show();
    }
}