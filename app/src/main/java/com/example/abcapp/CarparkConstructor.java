package com.example.abcapp;

import org.json.JSONException;
import org.json.JSONObject;

public class CarparkConstructor {
    public static Carpark construct(JSONObject carparkJSON) throws JSONException {
        Carpark carpark = new Carpark(
                carparkJSON.getString("car_park_type"),
                new double[]{carparkJSON.getDouble("x_coord"), carparkJSON.getDouble("y_coord")},
                carparkJSON.getString("address"),
                carparkJSON.getString("car_park_no"));

        return carpark;
    }
}
