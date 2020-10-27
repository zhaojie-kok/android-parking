package com.example.abcapp.Routes;

import android.graphics.Color;

import com.example.abcapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class Segment {
    private LatLng startPoint;
    private LatLng endPoint;
    public String trafficCondition; // good, ok, bad
    private String directions;
    private double speed;
    public PolylineOptions polyOptions;

    public Segment(PolylineOptions polyOptions, String directions, double speed) {
        this.polyOptions = polyOptions;
        this.startPoint = polyOptions.getPoints().get(0);
        this.endPoint = polyOptions.getPoints().get(polyOptions.getPoints().size()-1);
        this.directions = directions;
        this.speed = speed;
    }

    public Segment(String encodedPoly, String directions, double speed) {
        // decode the encodedPoly and build the polyOptions for creating the polyline
        List<LatLng> points = PolyUtil.decode(encodedPoly);
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.addAll(points);
        polyOptions.color(Color.BLUE);
//        polyOptions.color(0x0000FF);
        polyOptions.width(10);
        this.polyOptions = polyOptions;
        this.startPoint = points.get(0);
        this.endPoint = points.get(points.size()-1);
        this.directions = directions;
        this.speed = speed;
    }

    /* mutators */
    // mutator for segment polyline color
    public void setColor(int color) {
        this.polyOptions = polyOptions.color(color);
    }

    public void setTrafficCondition(String trafficCondition) {
        this.trafficCondition = trafficCondition;
    }
    /* mutators */

    /* accessors */
    public PolylineOptions getPolyOptions() {
        return polyOptions;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public String getDirections() {
        return this.directions;
    }

    public String getTrafficCondition() {
        return trafficCondition;
    }

    public double getSpeed() {
        return this.speed;
    }
    /* accessors */
}
