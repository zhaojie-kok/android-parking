package com.example.abcapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Weather {
    // keep record of the JSONs for use where needed
    private JSONObject weatherJSONNow;
    private JSONObject weatherJSONForecast;
    // NEA has different coordinates for its current and forecast weather data thus need 2 hashmaps
    private HashMap <String, LatLng> areaCoordsForecast = null;
    private HashMap <String, LatLng> areaCoordsNow = null;
    private HashMap<String, Object> weatherNow = null; // using objects since both datetime and Strings will be stored
    private HashMap<String, Object> weatherForecast = null;
    private APIcaller caller = new APIcaller();

    // constructor with API call performed by this class (wrong, only for testing)
    @RequiresApi(api = Build.VERSION_CODES.O)
    Weather() throws Exception {
        JSONObject resNow = caller.getWeatherNow(LocalDateTime.now());
        JSONObject resForecast = caller.getWeatherForecast(LocalDateTime.now());

        // record the coordinates
        if (resNow != null) {
            weatherJSONNow = resNow;
            JSONArray infoArr = weatherJSONNow.getJSONObject("metadata").getJSONArray("stations");
            String id;
            Double rainfall;
            JSONObject coords;

            // store the coordinates of each station as specified by data.gov.sg
            // these coordinates will be used in subsequent calculations
            for (int i = 0; i < infoArr.length(); i++) {
                id = infoArr.getJSONObject(i).getString("device_id");
                coords = infoArr.getJSONObject(i).getJSONObject("location");
                areaCoordsNow.put(id,
                        new LatLng(coords.getDouble("latitude"),
                                coords.getDouble("longitude")
                        )
                );
            }
        }

        if (resForecast != null) {
            weatherJSONForecast = resForecast;
            JSONArray infoArr = weatherJSONForecast.getJSONArray("area_metadata");
            String name, condition;
            JSONObject coords;

            // store the coordinates of each area in Singapore as specified by data.gov.sg
            // these coordinates will be used in subsequent calculations
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("name");
                coords = infoArr.getJSONObject(i).getJSONObject("label_location");
                areaCoordsForecast.put(name,
                        new LatLng(coords.getDouble("latitude"),
                                coords.getDouble("longitude")
                        )
                );
            }
        }


        this.updateWeatherNow(resNow);
        this.updateWeatherForecast(resForecast);
    }


    // constructor with API calls performed by controller class
    @RequiresApi(api = Build.VERSION_CODES.O)
    Weather(JSONObject resNow, JSONObject resForecast) throws Exception{

        // only record successful api calls

        // record the current weather conditions
        if (resNow != null) {
            weatherJSONNow = resNow;
            JSONArray infoArr = weatherJSONNow.getJSONObject("metadata").getJSONArray("stations");
            String id;
            Double rainfall;
            JSONObject coords;

            // store the coordinates of each station as specified by data.gov.sg
            // these coordinates will be used in subsequent calculations
            for (int i=0; i < infoArr.length(); i++) {
                id = infoArr.getJSONObject(i).getString("device_id");
                coords = infoArr.getJSONObject(i).getJSONObject("location");
                areaCoordsNow.put(id,
                        new LatLng(coords.getDouble("latitude"),
                                coords.getDouble("longitude")
                        )
                );
            }

            // next store the current weather conditions from the JSON
            infoArr = weatherJSONNow.getJSONArray("items").getJSONObject(0).getJSONArray("readings");
            for (int i = 0; i < infoArr.length(); i++) {
                id = infoArr.getJSONObject(i).getString("station_id");
                rainfall = infoArr.getJSONObject(i).getDouble("value");
                weatherNow.put(id, rainfall);
            }
            // parse and then record the timestamp of the reading
            String timeString = weatherJSONNow.getJSONArray("items").getJSONObject(0).getString("timestamp");
            LocalDateTime timeStamp = LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            weatherNow.put("timestamp", timeStamp);
        }

        // record the forecasted weather conditions
        if (resForecast != null) {
            weatherJSONForecast = resForecast;
            JSONArray infoArr = weatherJSONForecast.getJSONArray("area_metadata");
            String name, condition;
            JSONObject coords;

            // store the coordinates of each area in Singapore as specified by data.gov.sg
            // these coordinates will be used in subsequent calculations
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("name");
                coords = infoArr.getJSONObject(i).getJSONObject("label_location");
                areaCoordsForecast.put(name,
                        new LatLng(coords.getDouble("latitude"),
                                coords.getDouble("longitude")
                        )
                );
            }

            // next store the weather conditions from the JSON
            infoArr = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts");
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("area");
                condition = infoArr.getJSONObject(i).getString("forecast");
                weatherForecast.put(name, condition);
            }
            // parse and then record the valid period of the result
            JSONObject validPeriod = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONObject("valid_period");
            LocalDateTime start = LocalDateTime.parse(validPeriod.getString("start"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            LocalDateTime end = LocalDateTime.parse(validPeriod.getString("end"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            weatherForecast.put("start", start);
            weatherForecast.put("end", end);
        }
    }

    // update the current weather conditions
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int updateWeatherNow(JSONObject resNow) throws Exception{
        if (resNow != null) {
            weatherJSONNow = resNow;
            String id;
            Double rainfall;

            // next store the current weather conditions from the JSON
            JSONArray infoArr = weatherJSONNow.getJSONArray("items").getJSONObject(0).getJSONArray("readings");
            for (int i = 0; i < infoArr.length(); i++) {
                id = infoArr.getJSONObject(i).getString("station_id");
                rainfall = infoArr.getJSONObject(i).getDouble("value");
                weatherNow.put(id, rainfall);
            }
            // parse and then record the timestamp of the reading
            String timeString = weatherJSONNow.getJSONArray("items").getJSONObject(0).getString("timestamp");
            LocalDateTime timeStamp = LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            weatherNow.put("timestamp", timeStamp);

            return 1;
        } else {
            return -1;
        }
    }

    // update the forecasted weather conditions
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int updateWeatherForecast(JSONObject resForecast) throws Exception {
        if (resForecast != null) {
            weatherJSONForecast = resForecast;
            String name, condition;

            // next store the weather conditions from the JSON
            JSONArray infoArr = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts");
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("area");
                condition = infoArr.getJSONObject(i).getString("forecast");
                weatherForecast.put(name, condition);
            }
            // parse and then record the valid period of the result
            JSONObject validPeriod = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONObject("valid_period");
            LocalDateTime start = LocalDateTime.parse(validPeriod.getString("start"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            LocalDateTime end = LocalDateTime.parse(validPeriod.getString("end"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
            weatherForecast.put("start", start);
            weatherForecast.put("end", end);

            return 1;
        } else {
            return -1;
        }
    }
}
