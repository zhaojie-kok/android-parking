package com.example.abcapp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

public class ABCMarker {
    private boolean shown = false;
    private Marker marker = null;
    private MarkerOptions markerOptions; //  use a MarkerOptions to store most info
    private String description = null;
    private JSONObject jsonInfo = null;

    public ABCMarker(MarkerOptions m, String desc, JSONObject json) {
        this.markerOptions = m;
        this.description = desc;
        this.jsonInfo = json;
    }

    public void showMarker(GoogleMap mMap) {
        if (this.marker != null) {
            this.marker.remove(); // first remove the existing marker on the map just in case
        }
        this.marker = mMap.addMarker(this.markerOptions);
        this.shown = true;
    }

    // temporarily hide the marker
    public void hideMarker() {
        this.marker.setVisible(false);
        this.shown = false;
    }

    // permanently delete the marker
    public void removeMarker() {
        this.marker.remove();
        this.marker = null;
        this.shown = false;
    }

    /* mutators */
    // create a new marker
    public void setMarker(MarkerOptions m, GoogleMap mMap) {
        if (this.marker != null) {
            this.marker.remove(); // first remove the existing marker on the map
        }
        this.markerOptions = m;
        this.marker = mMap.addMarker(this.markerOptions);
        this.shown = true;
    }

    // change the json
    public void setJSON (JSONObject json) {
        this.jsonInfo = json;
    }

    // change the description
    public void setDescription (String desc) {
        this.description = desc;
    }
    /* mutators*/

    /* accessors */
    // get the marker
    public Marker getMarker() {
        return this.marker;
    }

    // get marker options
    public MarkerOptions getMarkerOptions() {
        return this.markerOptions;
    }

    // get LatLng
    public LatLng getLatLng() {
        return this.marker.getPosition();
    }

    // get description
    public String getDescription() {
        return this.description;
    }

    // get the JSON
    public JSONObject getJSON() {
        return this.jsonInfo;
    }
    /* accessors */

    public boolean isShown() {
        return this.shown;
    }
}
