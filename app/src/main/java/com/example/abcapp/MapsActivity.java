package com.example.abcapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.abcapp.Carparks.Carpark;
import com.example.abcapp.Carparks.CarparkList;
import com.example.abcapp.Carparks.CarparkRecommender;
import com.example.abcapp.Notif.NotifActivity;
import com.example.abcapp.Routes.Route;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Map controller
    private MapController mapController;

    // Carpark Recommender
    private CarparkRecommender carparkRecommender;

    // Boundary classes and helpers
    private static GoogleMap mMap;
    private RequestQueue requestQueue;
    private APICaller caller;

    // objects and attributes meant for items to be checked periodically e.g: weather, traffic, carpark
    private Handler handler;
    private int checkingInterval = 5000; //1000*60*5; // set to check every 5 mins
    private Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                // add all periodic checking methods here
                getLocation(false);
                mapController.updateWeather();
                mapController.updateTraffic();
                carparkRecommender.updateCarparks(mMap);
                if (carparksShown) {
                    toggleCarparks(true);
                }
            } catch (Exception e) {
                System.out.println("|| error in periodic updates ||");
                e.printStackTrace();
                System.out.println("|| error in periodic updates ||");
            } finally {
                handler.postDelayed(statusChecker, checkingInterval);
            }
        }
    };

    // for user address input
    private EditText startText;
    private EditText endText;
    private ImageButton showStart;
    private ImageButton showEnd;
    private static ABCMarker startMarker;
    private static ABCMarker endMarker;
    private static ABCMarker carparkMarker;

    // for getting routes
    private ImageButton searchRoute;
    private Route currentRoute;
    public static ArrayList<Route> potentialRoutes;

    // for getting location
    private ImageButton locationButton;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean trackingLocation = false;
    private Location prevLoc;

    // for choosing a carpark/showing carparks
    private Button chooseCarpark;
    private boolean carparksShown = false;

    // for menu drawer
    ImageButton menuButton;
    private DrawerLayout drawer;
    private NavigationView drawerMenu;
    private Switch carparkToggle;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /* make request queue and API caller for http API calls */
        requestQueue = Volley.newRequestQueue(this);
        caller = new APICaller(requestQueue);
        /* make request queue and API caller for http API calls */

        // set up the map controller and carpark recommender asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapController = new MapController(mMap, MapsActivity.this, caller);
                    carparkRecommender = new CarparkRecommender(caller, MapsActivity.this);
                } catch (Exception e) {
                    System.out.println("|| error in instantiating Map Controller ||");
                    e.printStackTrace();
                    System.out.println("|| error in instantiating Map Controller ||");
                }
            }
        }).start();


        // get the dimensions of the device screen for general use
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        displayHeight = getResources().getDisplayMetrics().heightPixels;

        // set up Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // set up map and location relevant objects
        // Obtain the SupportMapFragment
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                    Toast.makeText(MapsActivity.this, "location unavailable, using last know location", Toast.LENGTH_SHORT).show();
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
        drawerMenu = (NavigationView) findViewById(R.id.drawerMenu);

        // set functionality for the switch/toggle
        MenuItem carparkToggleItem = drawerMenu.getMenu().findItem(R.id.carparkToggle);
        carparkToggle = carparkToggleItem.getActionView().findViewById(R.id.switch_item);
        carparkToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                carparksShown = isChecked;
                toggleCarparks(isChecked);
            }
        });

        // set functionality for the drawerMenu
        drawerMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                System.out.println(id);
                switch (id) {
                    case R.id.menuHome:
                        returnToHomeScreen();
                        break;
                    case R.id.menuNotif:
                        goToNotifications();
                        break;
                    case R.id.menuSettings:
                        goToSettings();
                        break;
                    case R.id.carparkToggle:
                        carparkToggle.setChecked(!carparkToggle.isChecked());
                }

                return false;
            }
        });
        /* make menu layout and views */


        /* make text input boxes */
        /* create the search bar for the origin text */
        // instantiate the search bar and set fields to request, country, hint, and icon respectively
        AutocompleteSupportFragment originAutoCompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.originText);
        originAutoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        originAutoCompleteFragment.setCountry("SG");
        originAutoCompleteFragment.setHint("Start Location");
        ImageView originIcon = (ImageView) ((LinearLayout) originAutoCompleteFragment.getView()).getChildAt(0);
        originIcon.setImageDrawable(getResources().getDrawable(R.drawable.origin_search_logo));

        // get the text body of the search bar
        startText = (EditText) originAutoCompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);

        // set functionality for when user selects a suggestion
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
            }

            @Override
            public void onError(Status status) {
                System.out.println("|| error from origin autocomplete ||");
                System.out.println(status.getStatusMessage());
                System.out.println("|| error from origin autocomplete ||");
                Toast.makeText(MapsActivity.this,  "Network Error, please try again later", Toast.LENGTH_SHORT).show();
            }
        });
        /* create the search bar for the origin text */

        /* create the search bar for the destination text */
        // instantiate the search bar and set fields to request, country, hint, and icon respectively
        AutocompleteSupportFragment destAutoCompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.destText);
        destAutoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        destAutoCompleteFragment.setCountry("SG");
        destAutoCompleteFragment.setHint("End Location");
        ImageView destIcon = (ImageView) ((LinearLayout) destAutoCompleteFragment.getView()).getChildAt(0);
        destIcon.setImageDrawable((getResources().getDrawable(R.drawable.dest_search_logo)));

        // get the text body of the search bar
        endText = (EditText) destAutoCompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);

        // set functionality for when user selects a suggestion
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
                } else {
                    Toast.makeText(MapsActivity.this, "unable to retrieve location", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                System.out.println("|| error from origin autocomplete ||");
                System.out.println(status.getStatusMessage());
                System.out.println("|| error from origin autocomplete ||");
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
        chooseCarpark = findViewById(R.id.chooseCarpark);

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

        // the centering button for the start location
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

        // the centering button for the destination location
        showEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show error message if the end marker doesn't exist
                if (endMarker == null || endMarker.getMarker() == null) {
                    Toast.makeText(MapsActivity.this, "Please search for a location first", Toast.LENGTH_SHORT).show();
                } else {
                    endMarker.showMarker(mMap);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endMarker.getLatLng(), 15.0f));
                }
            }
        });

        // the button for choosing a carpark
        chooseCarpark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show error message to prompt user to get an end location if the user has not selected an end location yet
                if (endMarker == null) {
                    Toast.makeText(MapsActivity.this, "Set an End Location first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // otherwise call look for carparks near the end location and let the user choose
                final HashMap<String, Object> weatherCondition = mapController.getWeatherAt(endMarker.getLatLng());
                final ArrayList<String> recommendations = carparkRecommender.recommendCarparks(endMarker.getLatLng(), weatherCondition);
                promptCarparkChoice(recommendations, weatherCondition);
            }
        });
        /* make simple buttons */

        /* the route searching button */
        // first instantiate the currentRoute, the potentialRoutes, then the searchRoute button
        currentRoute = null;
        potentialRoutes = new ArrayList<Route>();
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
                Toast.makeText(MapsActivity.this, "finding route", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // look for routes and get latest traffic conditions
                            if (carparkMarker == null) {
                                MapsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MapsActivity.this, "Finding route without carpark", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                mapController.findRoutes(startMarker.getLatLng(), endMarker.getLatLng());
                            } else {
                                mapController.findRoutes(startMarker.getLatLng(), carparkMarker.getLatLng(), endMarker.getLatLng());
                            }

                            mapController.updateTraffic();
                        } catch (Exception e) {
                            System.out.println("|| error in finding route ||");
                            e.printStackTrace();
                            System.out.println("|| error in finding route ||");
                        }

                        // first access the routes that were retrieved
                        final ArrayList<Route> foundRoutes = mapController.getRoutes();

                        // Show error message if no routes were found
                        if (foundRoutes == null || foundRoutes.size() == 0) {
                            // show the error on the UI thread
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, "Unable to find a route. Please try again later", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        // once the routes have been found, get latest traffic conditions then return to the UI thread to create the popup
                        try {

                        } catch (Exception e) {
                            System.out.println("|| error in finding route ||");
                            e.printStackTrace();
                            System.out.println("|| error in finding route ||");
                        }
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // create a pop up for user to choose a route if a route was found
                                createRoutesPopup(foundRoutes);
                            }
                        });
                    }
                }).start();
//                caller.getRoutes(startMarker.getLatLng(), endMarker.getLatLng(), mMap, potentialRoutes, MapsActivity.this);
            }
        });
        /* the route searching button */

        /* make the handler for periodically repeating methods and start the periodic methods*/
        // NOTE: this needs to be last since the mapController needs to be instantiated first
        handler = new Handler();
        runPeriodicCheck();
        /* make the handler for periodically repeating methods and start the periodic methods*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // terminate all periodic checks here
        terminatePeriodicCheck();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapController.mMap = mMap;

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

        // instantiate the weatherbtn texts
        currWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoNow);
        predWeather = (TextView) weatherPopup.findViewById(R.id.weatherInfoForecast);

        // update the text shown in the textviews
        if (prevLoc != null) {
            HashMap<String, Object> weatherConditions = mapController.getWeatherAt(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()));

            // create the strings then display them
            String currString = "rainfall at current location:" + weatherConditions.get("now").toString();
            String forecastString = "Weather Forecast: " + weatherConditions.get("forecast").toString();
            currWeather.setText(currString);
            predWeather.setText(forecastString);
        } else {
            // if current location doesn't exist, then get a new location and prompt user to try again
            getLocation(false);
            Toast.makeText(MapsActivity.this, "Unable to find location, please try again", Toast.LENGTH_SHORT).show();
            return;
        }

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

    // method to display the routes found
    public void createRoutesPopup(ArrayList<Route> foundRoutes) {
        // instantiate the builder for the popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the title
        builder.setTitle("Select a Route");

        // create a list of summaries for each route
        String[] routeSummaries = new String[foundRoutes.size()];
        StringBuilder currentSummary;
        Route currRoute = null;
        for (int i=0; i<foundRoutes.size(); i++) {
            currRoute = foundRoutes.get(i);

            // create the summary using relevant information from the route
            currentSummary = new StringBuilder("Via: ");
            currentSummary.append(currRoute.getSummary());
            currentSummary.append("(" + currRoute.getTotalTime()/60 + "mins)");

            // add the complete summary to the routeSummaries
            routeSummaries[i] = (currentSummary.toString());
        }

        // set the choices and functionality for choice
        builder.setItems(routeSummaries, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    mapController.chooseRoute(i);
                    mapController.showRoute(i, mMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println(Arrays.toString(routeSummaries));
        // display the pop up
        builder.show();
    }

    // method to prompt the user to choose a carpark
    private void promptCarparkChoice(final ArrayList<String> recommendations, HashMap<String, Object> weatherCondition) {
        // create a dialog based on the recommendations
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Carparks Available near End point");
        // configure based on whether any carparks are available
        // if none are available inform user
        if (recommendations == null || recommendations.size() < 1) {
            Toast.makeText(MapsActivity.this, "Unable to find a suitable carpark", Toast.LENGTH_SHORT).show();
        } else {
            // iterate through each recommendation an retrieve relevant details
            String[] carparkDetails = new String[recommendations.size()];
            StringBuilder currentDetails;
            Carpark currentCarpark;
            int currentAvailability = 0;
            for (int i=0; i<recommendations.size(); i++) {
                String recommendation = recommendations.get(i);
                currentCarpark = CarparkList.getCarpark(recommendation);
                currentAvailability = currentCarpark.getAvailability();

                // build up the currentDetails
                currentDetails = new StringBuilder("Carpark: ");
                currentDetails.append(currentCarpark.getAddress());
                currentDetails.append(", Type: ");
                currentDetails.append(currentCarpark.getCarparkType());
                currentDetails.append(", Lots Available: ");
                if (currentAvailability > 0) {
                    currentDetails.append(currentAvailability);
                } else {
                    currentDetails.append(" unknown");
                }

                // add to the carparkDetails
                carparkDetails[i] = currentDetails.toString();
            }

            // add the carparkDetails as options
            builder.setItems(carparkDetails, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        // first remove existing carpark choice before getting a new one
                        if (carparkMarker != null) {
                            carparkMarker.removeMarker();
                        }
                        MapController.chooseCarpark(recommendations.get(i));
                        MapsActivity.carparkMarker = CarparkList.getCarpark(recommendations.get(i)).getAbcMarker();
                        System.out.println(carparkMarker.getLatLng());
                        chooseCarpark.setText(CarparkList.getCarpark(recommendations.get(i)).getAddress());
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MapsActivity.carparkMarker.showMarker(mMap);
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("|| Error choosing carpark ||");
                        e.printStackTrace();
                        System.out.println("|| Error choosing carpark ||");
                    }
                }
            });
            // display the pop up
            builder.show();
        }

    }

    // method to check weather conditions periodically
    public void runPeriodicCheck() {
        statusChecker.run();
    }

    // method to terminate weather checking
    public void terminatePeriodicCheck() {
        // remove the periodic checks
        handler.removeCallbacks(statusChecker);
    }

    // method to return to home screen
    private void returnToHomeScreen() {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // method to go to Notifications page
    private void goToNotifications() {
        Intent intent = new Intent(this, NotifActivity.class);
        startActivity(intent);
    }

    // method to go to Settings page
    private void goToSettings() {
        // TODO: Make a settings page
        return;
    }

    // method to toggle if carparks are shown
    private void toggleCarparks(boolean state) {
        if (state) {
            mapController.showNearbyCarparks(
                    new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()),
                    carparkRecommender,
                    mMap);
        } else {
            mapController.hideNearbyCarparks();
        }
    }
}