package com.example.abcapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Boundary classes and helpers
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private APICaller caller;

    // Entity Classes
    private Weather weather;

    // for user address input
    private EditText startText;
    private EditText endText;
    private Button clearStart;
    private Button clearEnd;
    private ImageButton showStart;
    private ImageButton showEnd;
    private ABCMarker startMarker;
    private ABCMarker endMarker;

    // for getting routes
    private ImageButton searchRoute;

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

    // for weatherbtn pop up
    ImageButton weatherButton;
    Button weatherClose;
    private PopupWindow weather_popup;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView currWeather, predWeather;

    // general purpose display metrics
    int displayWidth;
    int displayHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        displayHeight = getResources().getDisplayMetrics().heightPixels;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // set up Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // set up map and location relevant objects
        // Obtain the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* make request queue and API caller for http API calls */
        requestQueue = Volley.newRequestQueue(this);
        caller = new APICaller(requestQueue);
        /* make request queue and API caller for http API calls */

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

        /* make popup window for weatherbtn */
        weather_popup = new PopupWindow();

        /* make menu layout and views */
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        navView = (NavigationView) findViewById(R.id.navView);
        /* make menu layout and views */


        /* make text input boxes */
        /* create the search bar for the origin text */
        AutocompleteSupportFragment originAutoCompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.originText);

        originAutoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        originAutoCompleteFragment.setCountry("SG");
        originAutoCompleteFragment.setHint("Start Location");
        ImageView originIcon = (ImageView) ((LinearLayout) originAutoCompleteFragment.getView()).getChildAt(0);
        originIcon.setImageDrawable(getResources().getDrawable(R.drawable.origin_search_logo));
        startText = (EditText) originAutoCompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        originAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng pos = place.getLatLng();
                String name = place.getName();
                if (pos != null) {
                    // first remove existing marker if it exists
                    if (startMarker != null) {
                        startMarker.removeMarker();
                    }

                    startMarker = new ABCMarker(
                            new MarkerOptions()
                                    .position(pos)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)),
                            startText.getText().toString(), place);
                    startMarker.showMarker(mMap);

                    // focus the display on only the start point if endpoint doesnt exist or is not shown
                    if (endMarker == null || (endMarker != null && !endMarker.isShown())) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0f));
                    } else {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(startMarker.getLatLng());
                        builder.include(endMarker.getLatLng());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), (int) (displayWidth * 0.10)));
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "unable to retrieve location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MapsActivity.this,  name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                System.out.println("error from origin autocomplete");
                System.out.println(status.getStatusMessage());
                System.out.println(status.getStatusCode());
                System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                Toast.makeText(MapsActivity.this,  "Network Error, please try again later", Toast.LENGTH_SHORT).show();
            }
        });
        /* create the search bar for the origin text */

        /* create the search bar for the destination text */
        AutocompleteSupportFragment destAutoCompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.destText);

        destAutoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        destAutoCompleteFragment.setCountry("SG");
        destAutoCompleteFragment.setHint("End Location");
        ImageView destIcon = (ImageView) ((LinearLayout) destAutoCompleteFragment.getView()).getChildAt(0);
        destIcon.setImageDrawable((getResources().getDrawable(R.drawable.dest_search_logo)));
        endText = (EditText) destAutoCompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        destAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng pos = place.getLatLng();
                String name = place.getName();
                if (pos != null) {
                    if (endMarker != null) {
                        endMarker.removeMarker();
                    }
                    endMarker = new ABCMarker(
                            new MarkerOptions()
                                    .position(pos)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)),
                            endText.getText().toString(), place);
                    endMarker.showMarker(mMap);

                    // focus the display on only the end point if start point doesnt exist or is not shown
                    if (startMarker == null || (startMarker != null && !startMarker.isShown())) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0f));
                    } else {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(startMarker.getLatLng());
                        builder.include(endMarker.getLatLng());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), (int) (displayWidth * 0.10)));
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                System.out.println("error from origin autocomplete");
                System.out.println(status.getStatusMessage());
                System.out.println(status.getStatusCode());
                System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                Toast.makeText(MapsActivity.this,  "Network Error, please try again later", Toast.LENGTH_SHORT).show();
            }
        });
        /* create the search bar for the destination text */
        /* make text input boxes */


        /* make simple buttons */
        // instantiating the buttons
        weatherButton = findViewById(R.id.weatherButton);
        menuButton = findViewById(R.id.menuButton);
        locationButton = findViewById(R.id.locationButton);
        showStart = findViewById(R.id.showStart);
        showEnd = findViewById(R.id.showEnd);

        // adding functionality
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

        // the search button for the start location
        showStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show error message if the start marker doesnt exist
                if (startMarker == null || startMarker.getMarker() == null) {
                    Toast.makeText(MapsActivity.this, "Please search for a location first", Toast.LENGTH_SHORT).show();
                } else {
                    startMarker.showMarker(mMap);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startMarker.getLatLng(), 15.0f));
                }
            }
        });

        // the search button for the destination location
        showEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show error message if the end marker doesnt exist
                if (endMarker == null || endMarker.getMarker() == null) {
                    Toast.makeText(MapsActivity.this, "Please search for a location first", Toast.LENGTH_SHORT).show();
                } else {
                    endMarker.showMarker(mMap);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endMarker.getLatLng(), 15.0f));
                }
            }
        });
        /* make simple buttons */

        /* the route searching button */
        searchRoute = findViewById(R.id.searchRoute);

        searchRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // first check if there exists a start and end location
                if (startMarker == null || endMarker == null) {
                    Toast.makeText(MapsActivity.this, "Please set both Start and End locations", Toast.LENGTH_SHORT).show();
                    return;
                }

                // call google directions api asynchronously
                caller.getRoutes(startMarker.getLatLng(), endMarker.getLatLng(), mMap, requestQueue, MapsActivity.this);
            }
        });
        /* the route searching button */
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

    // method to display weatherbtn popup
    public void createWeatherPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View weatherPopup = getLayoutInflater().inflate(R.layout.weather_popup, null, false);

        // instantiate the weatherbtn texts
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
}