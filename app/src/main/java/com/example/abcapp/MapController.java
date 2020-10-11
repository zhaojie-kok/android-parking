package com.example.abcapp;

import android.content.Context;
import android.widget.Toast;

import com.example.abcapp.Routes.Route;
import com.example.abcapp.Routes.Segment;
import com.example.abcapp.Routes.Traffic;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MapController {
    // API caller
    private APICaller caller;

    // attributes linked to map activities
    private Context context;
    private static GoogleMap mMap;

    // Route and traffic related attributes
    private static Traffic trafficInfo;
    private static ArrayList<Route> routes;
    private static ArrayList<Polyline> polylines;
    private int chosenRoute;

    // Weather attribute
    private Weather weather;

    // Constructor
    public MapController(GoogleMap mMap, Context context, APICaller caller) throws InterruptedException, JSONException {
        // load the APICaller
        this.caller = caller;

        // load the map activity attributes
        MapController.mMap = mMap;
        this.context = context;

        // initialise the routes and polylines
        routes = new ArrayList<Route>();
        polylines = new ArrayList<Polyline>();

        // initialise the traffic attributes
        MapController.trafficInfo = new Traffic();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateTraffic();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // update traffic conditions (to be run asynchronously)
    public void updateTraffic() throws InterruptedException, JSONException {
        // get the updatedTrafficInfo by making API call then only update if a result was obtained
        JSONArray updatedTrafficInfo = caller.updateTraffic();
        if (updatedTrafficInfo != null) {
            MapController.trafficInfo.update(caller.updateTraffic());
        }

        // if no traffic info is available then set all segments to be blue and return
        if (MapController.trafficInfo == null) {
            for (Route route: routes) {
                ArrayList<Segment> segments = route.segments;
                for (Segment segment: segments) {
                    segment.polyOptions.color(R.color.quantum_googblue);
                }
            }
            return;
        }

        // update the routes with the new traffic info
        for (Route route: routes) {
            ArrayList<Segment> segments = route.segments;

            // update each segment of the route
            for (Segment segment: segments) {
                String roadCondition = null;
                LatLng startPoint = segment.getStartPoint();
                boolean matched = false;
                int i;

                // find the road which the segment's start point lies in
                // NOTE: this is a cheap heuristic, understandably a segment may cover multiple roads
                // but finding all roads along a segment is expensive
                for (i=0; i<MapController.trafficInfo.roadBounds.size(); i++) {
                    LatLngBounds bounds = MapController.trafficInfo.roadBounds.get(i);
                    if (bounds.contains(startPoint)) {
                        matched = true;
                        break;
                    }
                }

                // in the event where a stretch of road containing the segment startpoint was found
                if (matched) {
                    // update the segment's traffic condition according to the traffic info
                    JSONObject currInfo = MapController.trafficInfo.getInfo(i);
                    char roadType = currInfo.getString("RoadCategory").charAt(0);
                    int speedBand = currInfo.getInt("SpeedBand");

                    // based on the roadType, decide if the speedBand is too slow
                    int roadTypeInt;
                    // using an if statement as switch case is causing bugs
                    if (roadType == 'A') { // expressway
                        roadTypeInt = 8;
                    } else if (roadType == 'B') { // major arterial roads
                        roadTypeInt = 7;
                    } else if(roadType == 'C' || roadType == 'D') { // arterial roads
                        roadTypeInt = 6;
                    } else { // small/slip/uncategorised roads
                        roadTypeInt = 5;
                    }

                    int speedDifference = roadTypeInt - speedBand;
                    if (speedDifference>0 && speedDifference<=2) {
                        roadCondition = "ok";
                    } else if (speedDifference>2) {
                        roadCondition = "bad";
                    } else {
                        roadCondition = "good";
                    }
                }

                if (roadCondition == "good") {
                    segment.polyOptions.color(R.color.quantum_googgreen);
                } else if (roadCondition == "ok") {
                    segment.polyOptions.color(R.color.quantum_orange);
                } else if (roadCondition == "bad") {
                    segment.polyOptions.color(R.color.quantum_googred);
                } else { // no information available
                    segment.polyOptions.color(R.color.quantum_googblue);
                }
            }
        }

        System.out.println(trafficInfo.getAllInfo().toString()); // this line is only for debugging
    }

    // set the list of routes
    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    // method to find a route between 2 given points
    public void findRoutes(LatLng startPt, LatLng endPt) throws Exception {
        JSONArray routesFound = caller.getRoutes(startPt, endPt);
        JSONObject currRoute = null;

        // create an empty arraylist to hold the new routes
        ArrayList<Route> newRoutes = new ArrayList<Route>();

        for (int i=0; i<routesFound.length(); i++) {
            currRoute = routesFound.getJSONObject(i);
            newRoutes.add(new Route(currRoute));
        }

        // overwrite the existing routes with the newRoutes
        setRoutes(newRoutes);
    }
    // method to access the routes
    public ArrayList<Route> getRoutes() {
        return routes;
    }

    // method to set the chosenRoute
    public void chooseRoute(int choice) throws Exception {
        if (!(choice>=0 && choice<this.routes.size())) {
            System.out.println(choice);
            throw new Exception("choice out of index");
        }
        chosenRoute = choice;
    }

    // accessor for the chosenRoute
    public int getChosenRoute() {
        return this.chosenRoute;
    }

    // method to show a route on the map
    public void showRoute(int choice, GoogleMap mMap) {
        // first remove any route on the map
        for (Polyline polyline: polylines) {
            polyline.remove();
        }
        polylines.clear();

        // display the route of choice on the map
        Route choiceRoute = this.routes.get(choice);
        for (Segment segment: choiceRoute.segments) {
            polylines.add(mMap.addPolyline(segment.polyOptions));
        }
    }

    // method to get current weather conditions
    public HashMap<String, Object> getWeatherNow() {
        return weather.getWeatherNow();
    }

    // method to get the forecasted weather conditions
    public HashMap<String, Object> getWeatherForecast() {
        return weather.getWeatherForecast();
    }
}
