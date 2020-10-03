package com.example.abcapp;

import org.json.JSONException;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Boundary classes and helpers
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private APICaller caller;

    // Entity Classes
    private Weather weather;

    // for user address input
    private EditText originText;
    private EditText destText;
    private String startText;
    private String endText;
    private LatLng startPt = null;
    private LatLng endPt = null;
    private ImageButton startSearch;
    private ImageButton destSearch;
    private ABCMarker startMarker;
    private ABCMarker endMarker;

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

        /* make request queue and API caller for http API calls */
        requestQueue = Volley.newRequestQueue(this);
        caller = new APICaller(requestQueue);

        /* make fused location client, requests and callbacks */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // TODO: remove this before deployment
                    // Toast.makeText(MapsActivity.this, locationResult.toString(), Toast.LENGTH_SHORT).show();
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
        /* make fused location client, requests and callbacks */

        /* make popup window for weather */
        weather_popup = new PopupWindow();

        /* make menu layout and views */
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        navView = (NavigationView) findViewById(R.id.navView);
        /* make menu layout and views */

        /* make text input boxes */
        originText = findViewById(R.id.originText);
        destText = findViewById(R.id.destText);

        // allow entire text to be selected when the text box is selected
        originText.setSelectAllOnFocus(true);
        destText.setSelectAllOnFocus(true);
        /* make text input boxes */

        /* make simple buttons */
        startSearch = findViewById(R.id.originSearch);
        destSearch = findViewById(R.id.destSearch);
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
                Toast.makeText(MapsActivity.this, "finding location", Toast.LENGTH_SHORT).show();
                getLocation(true);
            }
        });
        /* make buttons */


        /* functionality for complex buttons */
        // the search button for the start location
        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonRes = null;
                LatLng newStartPt = null;
                startText = originText.getText().toString();
                try {
                    jsonRes = caller.getCoords(startText);
                    // store the result in newStartPt instead of startPt in case the result is invalid
                    newStartPt = new LatLng(jsonRes.getDouble("lat"), jsonRes.getDouble("lng"));
                } catch (Exception e) {
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    e.printStackTrace();
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    Toast.makeText(MapsActivity.this, "invalid start location", Toast.LENGTH_SHORT).show();
                    return; // exit if API call was unsuccessful
                }

                if (newStartPt != null) {
                    startPt = newStartPt;
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(startPt);
                    try {
                        // use the user's input as the title for the marker and the actual address as description
                        markerOptions.title(startText);
                        startText = jsonRes.getString("formatted_address");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startMarker = new ABCMarker(markerOptions, startText, jsonRes);
                    startMarker.showMarker(mMap);

                    // adjust the camera
                    if (endMarker != null && endMarker.isShown()) {
                        // shift the camera to display the start and end points
                        LatLngBounds mapBounds = findLatLngBounds(startPt, endPt);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startMarker.getLatLng(), 15.0f));
                    }
                } else {
                    // display error message if the new start point is null
                    Toast.makeText(MapsActivity.this, "invalid location", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // the search button for the destination location
        destSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonRes = null;
                LatLng newDestPt = null;
                endText = destText.getText().toString();
                try {
                    jsonRes = caller.getCoords(endText);
                    // store result in newDestPt instead of destPt in case result is invalid
                    newDestPt = new LatLng(jsonRes.getDouble("lat"), jsonRes.getDouble("lng"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, "invalid end location", Toast.LENGTH_SHORT).show();
                    return; // exit if API call was unsuccessful
                }

                if (newDestPt != null) {
                    endPt = newDestPt;
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(endPt);
                    try {
                        // use the user's input as the title for the marker and the actual address as description
                        markerOptions.title(endText);
                        endText = jsonRes.getString("formatted_address");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    endMarker = new ABCMarker(markerOptions, endText, jsonRes);
                    endMarker.showMarker(mMap);

                    // adjust the camera
                    if (startMarker != null && startMarker.isShown()) {
                        // shift the camera to display the start and end points
                        LatLngBounds mapBounds = findLatLngBounds(startPt, endPt);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endMarker.getLatLng(), 15.0f));
                    }
                } else {
                    // display error message if the new start point is null
                    Toast.makeText(MapsActivity.this, "invalid end location", Toast.LENGTH_SHORT).show();
                }

            }
        });
        /* functionality for complex buttons */
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
        // disable intrusive features such as tool bar and the location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);

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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(prevLatlng, 15.0f));
                        }
                    } else {
                        trackingLocation = false;
                        Toast.makeText(MapsActivity.this, "unable to retrieve location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // method to display weather popup
    public void createWeatherPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View weatherPopup = getLayoutInflater().inflate(R.layout.weather_popup, null, false);

        // instantiate the weather texts
        currWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoNow);
        predWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoForecast);

        // instantiate the close button
        weatherClose = (Button) weatherPopup.findViewById(R.id.closePopup);
        weatherPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "closing weather info", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // display the pop up
        dialogBuilder.setView(weatherPopup);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    // helper function to fine NorthEast and SouthWest Corners of 2 points formatted as LatLngBounds
    public LatLngBounds findLatLngBounds(LatLng pt1, LatLng pt2) {
        double northMost = Math.max(pt1.latitude, pt2.latitude);
        double southMost = Math.min(pt1.latitude, pt2.latitude);
        double eastMost = Math.max(pt1.longitude, pt2.longitude);
        double westMost = Math.max(pt1.longitude, pt2.longitude);

        LatLng NorthEast = new LatLng(northMost, eastMost);
        LatLng SouthWest = new LatLng(southMost, westMost);

        return new LatLngBounds(SouthWest, NorthEast);
    }
}