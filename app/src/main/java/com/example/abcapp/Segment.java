package com.example.abcapp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class Segment {
    private LatLng startPoint;
    private LatLng endPoint;
    private String trafficCondition; // good, ok, bad
    private String directions;
    private PolylineOptions polyOptions;
    private Polyline polyline;
    private boolean shown;

    public Segment(PolylineOptions polyOptions, String directions) {
        this.polyOptions = polyOptions;
        this.startPoint = polyline.getPoints().get(0);
        this.endPoint = polyline.getPoints().get(polyline.getPoints().size()-1);
        this.directions = directions;
//        updateTraffic();
    }

    public Segment(String encodedPoly, String directions) {
        List<LatLng> points = PolyUtil.decode(encodedPoly);
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.addAll(points);
        polyOptions.width(5);
        this.polyOptions = polyOptions;
        this.startPoint = points.get(0);
        this.endPoint = points.get(points.size()-1);
        this.directions = directions;
        this.shown = false;
//        updateTraffic();
    }

    // show the polyline on the map
    public void showSegment(GoogleMap mMap) {
        if (this.polyline != null) {
            polyline.remove();
        }
        this.polyline = mMap.addPolyline(this.polyOptions);
        this.shown = true;
    }

    // temporarily hide the polyline
    public void hideSegment() {
        if (this.polyline != null) {
            this.polyline.setVisible(false);
        }
        this.shown = false;
    }

    // permanently remove the polyline
    public void removeSegment() {
        if (this.polyline != null) {
            this.polyline.remove();
            this.polyline = null;
        }
        this.shown = false;
    }

    /* mutators */
    public void updateTraffic() {
        //TODO: call traffic api from data.gov.sg then colour the polyline accordingly
    }
    /* mutators */

    /* accessors */
    public PolylineOptions getPolyOptions() {
        return polyOptions;
    }

    public Polyline getPolyline() {
        return polyline;
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

    public boolean isShown() {
        return shown;
    }
    /* accessors */
}
