package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class CarparkConstructor {
    public static Carpark construct(JSONObject carparkJSON) throws JSONException {
        Carpark carpark = new Carpark(
                carparkJSON.getString("car_park_type"),
                new LatLng(carparkJSON.getDouble("x_coord"), carparkJSON.getDouble("y_coord")),
                carparkJSON.getString("address"),
                carparkJSON.getString("car_park_no"));

        return carpark;
    }
}
