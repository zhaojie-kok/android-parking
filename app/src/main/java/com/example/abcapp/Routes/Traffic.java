package com.example.abcapp.Routes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Traffic {
    private static JSONArray trafficData;
    public ArrayList<LatLngBounds> roads;

    public Traffic() {}

    public Traffic(JSONArray trafficData) throws JSONException {
        update(trafficData);
    }

    public void update(JSONArray trafficData) throws JSONException {
        // don't do anything if the given trafficData is null (use previous data rather than overwrite with null
        if (trafficData == null) {
            return;
        }

        Traffic.trafficData = trafficData;

        // only create the roadBounds if it is empty (i.e when constructing the class)
        if (roads == null) {
            roads = new ArrayList<LatLngBounds>();
        }

        JSONObject currRoad;
        String[] coordinates;

        for (int i=0; i<trafficData.length(); i++) {
            // get information about each road (in the from of a json object)
            currRoad = trafficData.getJSONObject(i);

            // use a LatLngBounds object to get a bounding box around each road (since LTA data splits roads into straight sections)
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            coordinates = currRoad.getString("Location").split(" ");

            // throw the start and end points of the road into the builder
            builder.include(new LatLng(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1])));
            builder.include(new LatLng(Float.parseFloat(coordinates[2]), Float.parseFloat(coordinates[3])));

            // build the latlngbounds for the current road
            LatLngBounds currBounds = builder.build();

            // add the new bounds into the roadBounds arraylist if it is not yet inside, or is different from the existing one
            if (i >= roads.size()) {
                roads.add(currBounds);
            } else if (!roads.get(i).equals(currBounds)) {
                roads.set(i, currBounds);
            }
        }
    }

    public boolean isNull() {
        return (trafficData == null);
    }

    public JSONObject getInfo(int index) throws JSONException {
        return trafficData.getJSONObject(index);
    }

    public JSONArray getAllInfo() {
        return trafficData;
    }
}
