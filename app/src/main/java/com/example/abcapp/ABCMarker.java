package com.example.abcapp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import org.json.JSONObject;

public class ABCMarker {
    private boolean shown = false;
    private Marker marker = null;
    private MarkerOptions markerOptions; //  use a MarkerOptions to store most info
    private String description = null;
    private Place place = null;

    public ABCMarker(MarkerOptions m, String desc, Place p) {
        this.markerOptions = m;
        this.description = desc;
        this.place = p;
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
        if (this.marker != null) {
            this.marker.setVisible(false);
        }
        this.shown = false;
    }

    // permanently delete the marker
    public void removeMarker() {
        if (this.marker != null) {
            this.marker.remove();
            this.marker = null;
        }
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

    // change the Place object
    public void setPlace (Place p) {
        this.place = p;
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
        return this.markerOptions.getPosition();
    }

    // get description
    public String getDescription() {
        return this.description;
    }

    // get the Place object
    public Place getPlace() {
        return this.place;
    }
    /* accessors */

    public boolean isShown() {
        return this.shown;
    }
}
