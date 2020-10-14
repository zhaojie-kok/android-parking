package com.example.abcapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Weather {
    // keep record of the JSONs for use where needed
    private JSONObject weatherJSONNow;
    private JSONObject weatherJSONForecast;
    // NEA has different coordinates for its current and forecast weatherbtn data thus need 2 hashmaps
    private HashMap<String, LatLng> areaCoordsForecast ;
    private HashMap<String, LatLng> areaCoordsNow;
    private HashMap<String, Object> weatherNow; // using objects since both datetime and Strings will be stored
    private HashMap<String, Object> weatherForecast;


    // constructor
    Weather(JSONObject resNow, JSONObject resForecast) throws Exception{
        // initialise the hashmaps
        areaCoordsForecast = new HashMap<String, LatLng>();
        areaCoordsNow = new HashMap<String, LatLng>();
        weatherNow = new HashMap<String, Object>();
        weatherForecast = new HashMap<String, Object>();

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

            // next store the current weatherbtn conditions from the JSON
            infoArr = weatherJSONNow.getJSONArray("items").getJSONObject(0).getJSONArray("readings");
            for (int i = 0; i < infoArr.length(); i++) {
                id = infoArr.getJSONObject(i).getString("station_id");
                rainfall = infoArr.getJSONObject(i).getDouble("value");
                weatherNow.put(id, rainfall);
            }
            // parse and then record the timestamp of the reading
            String timeString = weatherJSONNow.getJSONArray("items").getJSONObject(0).getString("timestamp");
            Date timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse(timeString);
            weatherNow.put("timestamp", timeStamp);
        }

        // record the forecasted weather conditions
        if (resForecast != null) {
            weatherJSONForecast = resForecast;
            System.out.println(weatherJSONForecast.toString());
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

            // next store the weatherbtn conditions from the JSON
            infoArr = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts");
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("area");
                condition = infoArr.getJSONObject(i).getString("forecast");
                weatherForecast.put(name, condition);
            }
            // parse and then record the valid period of the result
            JSONObject validPeriod = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONObject("valid_period");
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            Date start = dateParser.parse(validPeriod.getString("start"));
            Date end = dateParser.parse(validPeriod.getString("end"));
            weatherForecast.put("start", start);
            weatherForecast.put("end", end);
        }
    }


    /* accessors */
    // accessor for the current weather conditions
    public HashMap<String, Object> getWeatherNow() {
        return this.weatherNow;
    }

    // accessor for forecasted weather conditions
    public HashMap<String, Object> getWeatherForecast() {
        return this.weatherForecast;
    }

    // accessor for coordinates for current weather conditions
    public HashMap <String, LatLng> getAreaCoordsNow() {
        return this.areaCoordsNow;
    }

    // accessor for coordinates for forecasted weather conditions

    public HashMap<String, LatLng> getAreaCoordsForecast() {
        return areaCoordsForecast;
    }
    /* accessors */


    /* mutators */
    // update the current weather conditions
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
            Date timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse(timeString);
            weatherNow.put("timestamp", timeStamp);

            return 1;
        } else {
            return -1;
        }
    }

    // update the forecasted weather conditions
    public int updateWeatherForecast(JSONObject resForecast) throws Exception {
        if (resForecast != null) {
            weatherJSONForecast = resForecast;
            String name, condition;

            // next store the weatherbtn conditions from the JSON
            JSONArray infoArr = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts");
            for (int i = 0; i < infoArr.length(); i++) {
                name = infoArr.getJSONObject(i).getString("area");
                condition = infoArr.getJSONObject(i).getString("forecast");
                weatherForecast.put(name, condition);
            }
            // parse and then record the valid period of the result
            JSONObject validPeriod = weatherJSONForecast.getJSONArray("items").getJSONObject(0).getJSONObject("valid_period");
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            Date start = dateParser.parse(validPeriod.getString("start"));
            Date end = dateParser.parse(validPeriod.getString("end"));
            weatherForecast.put("start", start);
            weatherForecast.put("end", end);

            return 1;
        } else {
            return -1;
        }
    }
    /* mutators */

}
