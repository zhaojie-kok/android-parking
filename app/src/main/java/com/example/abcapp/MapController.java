package com.example.abcapp;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.example.abcapp.Carparks.CarparkList;
import com.example.abcapp.Carparks.CarparkRecommender;
import com.example.abcapp.Routes.Route;
import com.example.abcapp.Routes.Segment;
import com.example.abcapp.Routes.Traffic;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapController {
    // API caller
    private APICaller caller;

    // attributes linked to map activities
    private Context context;
    public static GoogleMap mMap;

    // Route and traffic related attributes
    private static Traffic trafficInfo;
    private static ArrayList<Route> routes;
    private static ArrayList<Polyline> polylines;
    private static Route walkingRoute;
    private int chosenRoute = -1;

    // Weather attribute
    private static Weather weather;

    // Carpark attributes
    private static String chosenCarpark;
    private static ABCMarker chosenCarparkMarker;
    private HashMap<String, ABCMarker> shownCarparks;

    // Constructor
    public MapController(final GoogleMap mMap, Context context, APICaller caller) throws InterruptedException, JSONException, IOException {
        // load the APICaller
        this.caller = caller;

        // load the map activity attributes
        MapController.mMap = mMap;
        this.context = context;

        // initialise the routes and polylines
        routes = new ArrayList<Route>();
        polylines = new ArrayList<Polyline>();

        // initialise the traffic, and carpark attributes
        // NOTE: weather cannot be initialised here since weather requires the JSONObjects from the API call to be constructed
        MapController.trafficInfo = new Traffic();
        shownCarparks = new HashMap<String, ABCMarker>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateTraffic();
                    updateWeather();
                    CarparkRecommender.updateCarparks(mMap);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // method to update all info at one go
    public void updateInfo() throws Exception{
        updateTraffic();
        updateWeather();
    }

    /* routes and traffic methods */
    // update traffic conditions (to be run asynchronously)
    public void updateTraffic() throws InterruptedException, JSONException {
        // get the updatedTrafficInfo by making API call then only update if a result was obtained
        JSONArray updatedTrafficInfo = caller.updateTraffic();
        if (updatedTrafficInfo != null) {
            MapController.trafficInfo.update(updatedTrafficInfo);
        }

        // if no traffic info is available then set all segments to be blue and return
        if (MapController.trafficInfo == null) {
            for (Route route: routes) {
                ArrayList<Segment> segments = route.segments;
                for (Segment segment: segments) {
                    segment.polyOptions.color(Color.BLUE);
                }
            }
            return;
        }

        // update the non-walking routes with the new traffic info
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
                for (i=0; i<MapController.trafficInfo.roads.size(); i++) {
                    LatLngBounds bounds = MapController.trafficInfo.roads.get(i);
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
                        roadCondition = "congested";
                    } else {
                        roadCondition = "good";
                    }
                }

                if (roadCondition == "good") {
                    segment.setColor(Color.GREEN);
                    segment.setTrafficCondition(roadCondition);
                } else if (roadCondition == "ok") {
                    segment.setColor(Color.YELLOW);
                    segment.setTrafficCondition(roadCondition);
                } else if (roadCondition == "congested") {
                    segment.setColor(Color.RED);
                    segment.setTrafficCondition(roadCondition);
                } else {
                    // use projected speed for road conditions
                    double speed = segment.getSpeed();

                    if (speed < 15) {
                        segment.setColor(Color.RED);
                        segment.setTrafficCondition("congested");
                    } else if (speed < 30) {
                        segment.setColor(Color.YELLOW);
                        segment.setTrafficCondition("ok");
                    } else if (speed > 60) {
                        segment.setColor(Color.GREEN);
                        segment.setTrafficCondition("good");
                    } else {
                        segment.setColor(Color.BLUE);
                        segment.setTrafficCondition("unknown");
                    }
                }
            }
        }
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

    // method to find route from start to mid point by driving, then walking to the end point
    public void findRoutes(LatLng startPt, LatLng carpark, LatLng endPt) throws Exception {
        JSONArray routesFound = caller.getRoutes(startPt, carpark);
        JSONObject currRoute = null;

        // create an empty arraylist to hold the new routes
        ArrayList<Route> newRoutes = new ArrayList<Route>();

        for (int i=0; i<routesFound.length(); i++) {
            currRoute = routesFound.getJSONObject(i);
            newRoutes.add(new Route(currRoute));
        }

        // overwrite the existing routes with the newRoutes
        setRoutes(newRoutes);

        // next find a way to walk from the carpark to the end point
        JSONObject walkingRoute = caller.getWalkingRoute(carpark, endPt);
        this.walkingRoute = new Route(walkingRoute);
        this.walkingRoute.setColor(Color.CYAN);
    }

    // method to access the routes
    public ArrayList<Route> getRoutes() {
        return routes;
    }

    // method to get the directions for a route
    public ArrayList<String> getDirections(int routeIndex) {
        ArrayList<String> directions = MapController.routes.get(routeIndex).getDirections();
        if (MapController.walkingRoute != null) {
            for (String direction: MapController.walkingRoute.getDirections()) {
                directions.add(direction);
            }
        }

        return directions;
    }

    // method to set the chosenRoute
    public void chooseRoute(int choice) throws Exception {
        if (!(choice>=0 && choice<this.routes.size())) {
            System.out.println(choice);
            throw new Exception("choice out of index");
        }
        chosenRoute = choice;
    }

    // accessor for the chosenRoute's index in the list ArrayList of routes
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
        Polyline newPoly;
        String trafficCondition;
        for (Segment segment: choiceRoute.segments) {
            newPoly = mMap.addPolyline(segment.polyOptions);
            newPoly.setTag("Driving Route: " + segment.getTrafficCondition());
            newPoly.setClickable(true);
            polylines.add(newPoly);
        }

        // display the walkingRoute if it exists
        if (this.walkingRoute != null) {
            for (Segment segment: this.walkingRoute.segments) {
                newPoly = mMap.addPolyline(segment.polyOptions);
                newPoly.setTag("Walking Route");
                newPoly.setClickable(true);
                polylines.add(newPoly);
            }
        }
    }

    // find the segment along a route nearest to a point
    public int findNearestSegment(Location prevLoc) {
        if (prevLoc == null) {
            return -1;
        }

        Route selectedRoute = routes.get(this.chosenRoute);
        double currDist = Double.MAX_VALUE;
        Segment segment = null;
        boolean res = false;
        for (int i=0; i<selectedRoute.segments.size(); i++) {
            segment = selectedRoute.segments.get(i);
            res = PolyUtil.isLocationOnPath(
                    new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()),
                    segment.getPolyOptions().getPoints(),
                    true,
                    50);

            // return true if location is along the current segment
            if (res) {
                return i;
            }
        }

        return -1;
    }
    /* routes and traffic methods */


    /* weather methods */
    // method to update the weather conditions
    public void updateWeather() throws Exception {
        JSONObject weatherForecast = caller.getWeatherForecast(null);
        JSONObject weatherNow = caller.getWeatherNow(null);

        if (this.weather == null) {
            weather = new Weather(weatherNow, weatherForecast);
            return;
        } else {
            this.weather.updateWeatherNow(weatherNow);
            this.weather.updateWeatherForecast(weatherForecast);
        }
    }

    // method to get weather conditions at a certain location
    public HashMap<String, Object> getWeatherAt(LatLng location) {
        // instantiate a hashmap to return the results found
        HashMap<String, Object> weatherAt = new HashMap<String, Object>();

        // get the coordinates of the stations from NEA for their current and forecast weather conditions
        HashMap <String, LatLng> weatherNowCoords = this.weather.getAreaCoordsNow();
        HashMap <String, LatLng> weatherForecastCoords = this.weather.getAreaCoordsForecast();

        // variables to store the station id nearest to the given location
        double currDist;
        double nowDist = Double.MAX_VALUE;
        double forecastDist = Double.MAX_VALUE;
        String nearestNow = null;
        String nearestForecast = null;

        // first find the station id from weatherNowCoords that's closest to the given location
        for (HashMap.Entry<String, LatLng> station: weatherNowCoords.entrySet()) {
            currDist = findDist(location, station.getValue());
            if (currDist < nowDist) {
                nowDist = currDist;
                nearestNow = station.getKey();
            }
        }

        // do the same for the weatherForecastCoords
        for (HashMap.Entry<String, LatLng> station: weatherForecastCoords.entrySet()) {
            currDist = findDist(location, station.getValue());
            if (currDist < forecastDist) {
                forecastDist = currDist;
                nearestForecast = station.getKey();
            }
        }

        // record the results
        weatherAt.put("now", this.getWeatherNow().get(nearestNow));
        weatherAt.put("forecast", this.getWeatherForecast().get(nearestForecast));

        return weatherAt;
    }

    // method to get current weather conditions everywhere
    public HashMap<String, Object> getWeatherNow() {
        return weather.getWeatherNow();
    }

    // method to get the forecasted weather conditions everywhere
    public HashMap<String, Object> getWeatherForecast() {
        return weather.getWeatherForecast();
    }
    /* weather methods */


    /* carpark methods */
    // mutator method for chosenCarpark
    public static void chooseCarpark(String choice) {
        MapController.chosenCarpark = choice;
        Object[] tempObj = new Object[1];
        tempObj[0] = CarparkList.getCarpark(choice);
        MapController.chosenCarparkMarker = new ABCMarker(tempObj, ABCLocationFactory.CARPARK);
    }

    // accessor method for chosenCarpark
    public static String getChosenCarpark() {
        return MapController.chosenCarpark;
    }

    // method to show the chosen carpark marker
    public static void showChosenCarpark() {
        if (MapController.chosenCarparkMarker != null) {
            return;
        } else {
            MapController.chosenCarparkMarker.showMarker(MapController.mMap);
        }
    }

    // method to remove the chosen carpark marker
    public static void removeChosenCarpark() {
        if (MapController.chosenCarparkMarker == null) {
            return;
        } else {
            MapController.chosenCarparkMarker.removeMarker();
        }
    }

    // method to show nearby carparks around a certain position
    public void showNearbyCarparks(LatLng pos, CarparkRecommender carparkRecommender, GoogleMap mMap) {
        // first get a list of nearbyCarparks
        ArrayList<String> nearbyCarparks = carparkRecommender.findNearbyCarparks(pos);
        ArrayList<String> keysToRemove = new ArrayList<>();

        // next check which carparks from the existing shown carparks need to be removed
        for (Map.Entry<String, ABCMarker> shownCarpark: this.shownCarparks.entrySet()) {
            // if there exists a shown carpark that is not in the current nearby list, remove it
            if (!nearbyCarparks.contains(shownCarpark.getKey())) {
                shownCarpark.getValue().removeMarker();
                keysToRemove.add(shownCarpark.getKey());
            }
        }

        for (String key: keysToRemove){
            shownCarparks.remove(key);
        }

        // add nearby carparks that have not been shown into the shownCarparks ArrayList
        for (String nearbyCarpark: nearbyCarparks) {
            if (!this.shownCarparks.containsKey(nearbyCarpark)) {
                Object[] tempObj = new Object[1];
                tempObj[0] = CarparkList.getCarpark(nearbyCarpark);
                this.shownCarparks.put(nearbyCarpark, new ABCMarker(tempObj, ABCLocationFactory.CARPARK));
                this.shownCarparks.get(nearbyCarpark).showMarker(mMap);
            }
        }

        for (ABCMarker abcMarker: this.shownCarparks.values()) {
            abcMarker.showHiddenMarker();
        }
    }

    // method to hide the nearby carparks from the map
    public void hideNearbyCarparks() {
        for (ABCMarker abcMarker: this.shownCarparks.values()) {
            abcMarker.hideMarker();
        }
    }
    /* carpark methods */

    // method to find a location based on user input
    public LatLng findLocation(String placeId) {
        JSONObject res = null;
        try {
            res = this.caller.getCoords(placeId);
            double lat = res.getDouble("lat");
            double lng = res.getDouble("lng");
            return new LatLng(lat, lng);
        } catch (Exception e) {
            System.out.println("|| error in getting place coordinates ||");
            e.printStackTrace();
            System.out.println(res.toString());
            System.out.println("|| error in getting place coordinates ||");
            return null;
        }
    }


    // other methods
    // method to calculate distance between 2 points
    private float findDist(LatLng pt1, LatLng pt2) {
        // using the Haversine distance formula
        double earthRadius = 6371000; // in meters

        // applying the formula
        double diffLat = Math.toRadians(pt2.latitude - pt1.latitude);
        double diffLng = Math.toRadians(pt2.longitude - pt1.longitude);
        double a = Math.sin(diffLat/2) * Math.sin(diffLat/2) +
                Math.cos(Math.toRadians(pt1.latitude)) * Math.cos(Math.toRadians(pt2.latitude)) * Math.sin(diffLng/2) * Math.sin(diffLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist; // in meters
    }
}
