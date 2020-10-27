package com.example.abcapp.Carparks;

import android.content.Context;

import com.example.abcapp.APICaller;
import com.example.abcapp.MapController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CarparkRecommender {
    // API caller
    private static APICaller caller;

    // make a CarparkList
    private static CarparkList carparkList;

    public CarparkRecommender(APICaller caller, Context context) throws IOException, JSONException {
        CarparkRecommender.caller = caller;
        CarparkRecommender.carparkList = new CarparkList(context);
    }

    // method to update the availability/capacity of each carpark in the carparks ArrayList
    public static void updateCarparks(GoogleMap mMap) throws Exception {
        // first make the API call to get information about the carpark availabilities (vacancies)
        JSONArray carparkAvails = caller.getCarparkAvailability();

        // iterate through teh carparkAvails
        for (int i=0; i<carparkAvails.length(); i++) {
            String carparkNo = carparkAvails.getJSONObject(i).getString("carpark_number");

            // of the 3 types of parking lots, C, Y, H, we only consider type C which is the standard white parking lot
            // Since the other 2 types are not always accessible
            int totalCapacity = 0;
            int lotsAvail = 0;
            JSONArray carparkInfo = carparkAvails.getJSONObject(i).getJSONArray("carpark_info");
            for (int j=0; j<carparkInfo.length(); j++) {
                if (carparkInfo.getJSONObject(j).getString("lot_type").equals("C")) {
                    totalCapacity = carparkInfo.getJSONObject(j).getInt("total_lots");
                    lotsAvail = carparkInfo.getJSONObject(j).getInt("lots_available");
                }
            }
            try {
                if (CarparkRecommender.carparkList.getCarpark(carparkNo) == null) {
                    continue;
                } else {
                    CarparkRecommender.carparkList.getCarpark(carparkNo).updateAvailability(lotsAvail, totalCapacity, mMap);
                }
            } catch (Exception e) {
                System.out.println("|| exception in updating carparks ||");
                System.out.println(carparkNo);
                System.out.println(CarparkRecommender.carparkList.getCarpark(carparkNo));
                e.printStackTrace();
                System.out.println("|| exception in updating carparks ||");
            }

        }
    }

    // method to find nearby carparks around a certain position
    public ArrayList<String> findNearbyCarparks(LatLng pos) {
        ArrayList<String> nearbyCarparks = new ArrayList<String>();

        // iterate through all carparks to find carparks that are within 200m
        for (Carpark currCarpark: CarparkList.getCarparks().values()) {
            double dist = findDist(pos, currCarpark.getCoordinates());

            // only record carparks that are within 500m and have a different carparkNo from the chosen carpark
            if (dist <= 500 && !currCarpark.getCarparkNo().equals(MapController.getChosenCarpark())) {
                System.out.println(dist);
                nearbyCarparks.add(currCarpark.getCarparkNo());
            }
        }
        return nearbyCarparks;
    }

    // method to recommend carparks around a location based on weather location and distance to the given location
    // returns an ArrayList of carparkNo
    public ArrayList<String> recommendCarparks(LatLng pos,  HashMap<String, Object> weatherCondition) {
        List<String> recommended = findNearbyCarparks(pos);

        // interpret the weatherConditions given and determine the chance of raining
        boolean chanceToRain = true; // by default assume rain
        try {
            Double weatherNow = (Double) weatherCondition.get("now");
            String weatherForecast = ((String) weatherCondition.get("forecast")).toLowerCase();
            chanceToRain = (weatherNow > 0 || weatherForecast.contains("shower") || weatherForecast.contains("cloudy"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // filter out surface carparks (non-sheltered) if chanceToRain is true
        if (chanceToRain) {
            Iterator<String> recommendedIter = recommended.iterator();
            while (recommendedIter.hasNext()) {
                String carparkNo = recommendedIter.next();
                String type = CarparkList.getCarpark(carparkNo).getCarparkType();
                if (type.equals("SURFACE CAR PARK")) {
                    recommendedIter.remove();
                }
            }
        }

        // filter out carparks that are >90% full
        Iterator<String> recommendedIter = recommended.iterator();
        while (recommendedIter.hasNext()) {
            String carparkNo = recommendedIter.next();
            float capacity = (float) CarparkList.getCarpark(carparkNo).getCapacity();
            float availability = (float) CarparkList.getCarpark(carparkNo).getAvailability();
            float availableCapacity = availability/capacity;
            if (availableCapacity < 0.1) {
                recommendedIter.remove();
            }
        }

        return (ArrayList) recommended;
    }

    // method to calculate distance between 2 points
    private double findDist(LatLng pt1, LatLng pt2) {
        // convert LatLng objects into radians for latitude and longitude
        double lat1 = Math.toRadians(pt1.latitude);
        double lng1 = Math.toRadians(pt1.longitude);
        double lat2 = Math.toRadians(pt2.latitude);
        double lng2 = Math.toRadians(pt2.longitude);

        // Apply Haversine formula
        double latDiff = lat2 - lat1;
        double lngDiff = lng2 - lng1;
        double a = Math.pow(Math.sin(latDiff/2), 2) + Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(lngDiff/2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return 6371000 * c;
    }
}
