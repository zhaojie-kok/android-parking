package com.example.abcapp.Routes;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Route {
    public ArrayList<Segment> segments;
    private JSONObject result;
    private String summary;
    private int totalTime;
    private double totalDist;
    private LatLngBounds mapBounds;

    public Route(JSONObject jsonRes) throws JSONException {
        // first store the json result
        this.result = jsonRes;

        // extract summary from the json
        this.summary = jsonRes.getString("summary");

        // extract the map boundaries from the json
        JSONObject northEast = jsonRes.getJSONObject("bounds").getJSONObject("northeast");
        JSONObject southWest = jsonRes.getJSONObject("bounds").getJSONObject("southwest");
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(northEast.getDouble("lat"), northEast.getDouble("lng")));
        builder.include(new LatLng(southWest.getDouble("lat"), southWest.getDouble("lng")));
        this.mapBounds = builder.build();

        // extract total time, steps, and distance from the json
        this.totalDist = 0;
        this.totalTime = 0;
        this.segments = new ArrayList<Segment>();
        String encodedPolyline, Instructions;
        Segment currSegment;
        JSONObject currLeg;
        JSONArray legs = jsonRes.getJSONArray("legs");
        JSONObject currStep;

        // iterate through and extract information from each leg
        for (int i=0; i<legs.length(); i++) {
            currLeg = legs.getJSONObject(i);

            // extract time and add to the total
            this.totalTime += currLeg.getJSONObject("duration").getInt("value");

            // extract distance and add to the total
            this.totalDist += currLeg.getJSONObject("distance").getInt("value");

            // extract and record the steps in the current leg
            for (int j=0; j<currLeg.getJSONArray("steps").length(); j++) {
                currStep = currLeg.getJSONArray("steps").getJSONObject(j);

                // extract the driving instructions from the currStep and remove html tags
                Instructions = currStep.getString("html_instructions");
                Instructions = android.text.Html.fromHtml(Instructions).toString();

                // extract the encoded polyline from currStep
                encodedPolyline = currStep.getJSONObject("polyline").getString("points");

                // create a new segment from the extracted information
                currSegment = new Segment(encodedPolyline, Instructions);
                segments.add(currSegment);
            }
        }
    }

    // method to change the color of the segments
    public void setColor(int newColor) {
        for (Segment segment: segments) {
            segment.setColor(newColor);
        }
    }

    // get the directions
    public ArrayList<String> getDirections() {
        ArrayList<String> directions = new ArrayList<String>();
        for (Segment segment: segments) {
            directions.add(segment.getDirections());
        }
        return directions;
    }

    // get the summary for the route
    public String getSummary() {
        return this.summary;
    }

    // get the total travel time for the route
    public int getTotalTime() {
        return this.totalTime;
    }

    // get the total distance for the route
    public double getTotalDist() {
        return this.totalDist;
    }
}
