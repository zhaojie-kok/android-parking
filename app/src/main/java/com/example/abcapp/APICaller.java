package com.example.abcapp;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Stack;

public class APICaller {
    // boolean flag for general use wherever needed
    public RequestQueue requestQueue = null;
    private boolean flag = false;
    private final static int numTries = 5;
    private final static String gKey = "AIzaSyCemoI4we1HrZ2z4TE8FvcEQYscriomdxs"; // key for testing, change to manifest key in deployment

    public APICaller(RequestQueue rq) {
        this.requestQueue = rq;

        // enable synchronous HTTP calls
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    // Asynchronous method to call APIs (template method for async calls, not actually used)
    public final void httpGetAsync(final String address, String store) throws Exception {
        // record response
        final StringBuilder result = new StringBuilder();

        // add a http request onto the request queue
        StringRequest stringRequest = new StringRequest(Request.Method.GET, address, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                result.append(response);
                System.out.println(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Request Failed");
                System.out.println(address);
            }
        });

        this.requestQueue.add(stringRequest);
    }

    // Synchronous method to call APIs
    public final String httpGet(final String address) throws Exception {
        String currLine;

        // record response
        final StringBuilder response = new StringBuilder();

        // set up connection
        final URL url = new URL(address);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // throw connection into an inputstream and then read the stream
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while ((currLine = reader.readLine()) != null) {
            response.append(currLine);
        }

        return response.toString();
    }


    // method to obtain routes and display them on the map
    public void getRoutes(LatLng startPt, LatLng endPt, GoogleMap mMap, RequestQueue rq, final Context context) {
        final StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=");
        url.append(startPt.latitude + "," + startPt.longitude);
        url.append("&destination=");
        url.append(endPt.latitude + "," + endPt.longitude);
        url.append("&key=" + gKey);

        // add a new request onto the request queue, using the built url
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "No available routes", Toast.LENGTH_SHORT).show();
            }
        });

        this.requestQueue.add(stringRequest);
    }

    // method to get routes given a start and end point
    // TODO: incomplete
    public JSONArray getRoutes(final String start, final String end) throws Exception {
        JSONObject jsonRes = null;
        final JSONObject startCoord = getCoords(start);
        final JSONObject endCoord = getCoords(end);

        // use stringbuilder cause neater to work with
        final StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=");
        url.append(startCoord.get("lat") + "," + startCoord.get("lng"));
        url.append("&destination=");
        url.append(endCoord.get("lat") + "," + endCoord.get("lng"));
        url.append("&key=" + gKey);

        // get API response, up to 5 times
        for (int i = 0; i<numTries; i++) {
            try {
                String response = httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check the status if there was a response
            if (jsonRes != null) {
                final String status = (String) jsonRes.get("status");
                System.out.println(status);
                // set flag as true if status is OK
                flag = status.equals("OK");
            }

            // exit loop
            if (flag) {
                // reset flag to false
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                jsonRes = null;
            }
        }

        // extract the routes from the API json
        // TODO: refine the extraction
        if (jsonRes != null) {
            final JSONArray routes = jsonRes.getJSONArray("routes");
            return routes;
        } else {
            return null;
        }
    }

    // method to get coordinates from name/description of a place
    public JSONObject getCoords(String name) throws Exception {
        JSONObject jsonRes = null;

        // format the name of the location then build url for API call
        name = name.replaceAll("\\s+", "+");
        final StringBuilder url = new StringBuilder(("https://maps.googleapis.com/maps/api/geocode/json?address="));
        url.append("Singapore,+" + name); // add Singapore to the given name to restrict locations to singapore only
        url.append("&key=" + gKey);

        for (int i = 0; i < numTries; i++) {
            // surround API call in a try catch
            try {
                String response = httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                System.out.println(url.toString());
                e.printStackTrace();;
            }

            // check the status if there was a response
            if (jsonRes != null) {
                final String status = (String) jsonRes.get("status");
                // set flag as true if status is OK
                flag = status.equals("OK");
            }

            // exit loop
            if (flag) {
                // reset flag to false
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.toString());
                jsonRes = null;
            }
        }

        // return null if no results were found
        if (jsonRes == null) {
            return null;
        }

        // filter coordinates from the results
        final JSONArray results = (JSONArray) jsonRes.get("results");
        final JSONObject currRes = (JSONObject) results.get(0); // assume first result is best match
        // TODO: add other useful stuff like name and place_id

        JSONObject output = currRes.getJSONObject("geometry").getJSONObject("location");
        output.put("place_id", currRes.getString("place_id"));
        output.put("short_name", currRes.getString("formatted_address"));

        return output;
    }

    // method to call weather forecast API
    // TODO: make async
    public JSONObject getWeatherForecast(LocalDateTime now) throws Exception {
        String response = null;
        JSONObject jsonRes = null;

        // format the datetime for API call
        String nowStr = now.toString();
        nowStr = nowStr.substring(0, nowStr.indexOf('.'));
        nowStr = nowStr.replace(":", "%3A");

        // first build the URL
        final StringBuilder url = new StringBuilder("https://api.data.gov.sg/v1/environment/24-hour-weather-forecast?date_time=");
        url.append(nowStr);

        // call API
        for (int i=0; i<numTries; i++) {
            // surround API call in a try catch
            try {
                response = httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check the status if there was a response
            if (jsonRes != null) {
                // set flag as true if no error message was given
                flag = !(jsonRes.has("message"));
            }

            // exit loop
            if (flag) {
                // reset flag to false
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.getString("message"));
                jsonRes = null;
            }
        }

        return jsonRes;
    }

    // method to get current weather conditions
    // TODO: make async
    public JSONObject getWeatherNow(LocalDateTime now) throws Exception {
        JSONObject jsonRes = null;

        // format the datetime for API call
        String nowStr = now.toString();
        nowStr = nowStr.substring(0, nowStr.indexOf('.'));
        nowStr = nowStr.replace(":", "%3A");

        // first build the URL
        final StringBuilder url = new StringBuilder("https://api.data.gov.sg/v1/environment/rainfall?date_time=");
        url.append(nowStr);

        // call API
        for (int i = 0; i < numTries; i++) {
            // surround API call in a try catch
            try {
                String response = httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check the status if there was a response
            if (jsonRes != null) {
                // set flag as true if no error message was given
                flag = !(jsonRes.has("message"));
            }

            // exit loop
            if (flag) {
                // reset flag to false
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.getString("message"));
                jsonRes = null;
            }
        }

        return jsonRes;
    }

    // method to get carpark availability from URA carparks
    // TODO: make async
    public JSONArray checkCarparks(LocalDateTime now) throws Exception {
        JSONObject jsonRes = null;
        JSONArray carparkInfo = null;

        // format the datetime for API call
        String nowStr = now.toString();
        nowStr = nowStr.substring(0, nowStr.indexOf('.'));
        nowStr = nowStr.replace(":", "%3A");

        // first build the URL
        final StringBuilder url = new StringBuilder("https://api.data.gov.sg/v1/environment/rainfall?date_time=");
        url.append(nowStr);

        // call API
        for (int i = 0; i < numTries; i++) {
            // surround API call in a try catch
            try {
                String response = httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check the status if there was a response
            if (jsonRes != null) {
                // set flag as true if no error message was given
                flag = !(jsonRes.has("message"));
            }

            // exit loop
            if (flag) {
                // reset flag to false
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.getString("message"));
                jsonRes = null;
            }
        }

        // filter the carpark vancancy information
        if (jsonRes != null) {
            carparkInfo = jsonRes.getJSONArray("items").getJSONObject(0).getJSONArray("carpark_data");
        }

        return carparkInfo;
    }
}