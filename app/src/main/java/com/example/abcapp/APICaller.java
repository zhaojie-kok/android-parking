package com.example.abcapp;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.abcapp.Routes.Route;
import com.example.abcapp.Routes.Traffic;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonStreamParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APICaller {
    // boolean flag for general use wherever needed
    public static RequestQueue requestQueue = null;
    private boolean flag = false;
    private final static int numTries = 5;
    private final static String gKey = "AIzaSyCemoI4we1HrZ2z4TE8FvcEQYscriomdxs"; // key for testing, change to manifest key in deployment
    private final static String ltaKey = "9KPN2QRjSp6u57qOSD1iOQ==";

    public APICaller(RequestQueue rq) {
        APICaller.requestQueue = rq;

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
    public final String httpGet(final String address, HashMap<String, String> headers) throws Exception {
        String currLine;

        // record response
        final StringBuilder response = new StringBuilder();

        // set up connection
        final URL url = new URL(address);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // set up the headers if any
        if (headers != null) {
            for (Map.Entry<String, String> item: headers.entrySet()) {
                connection.setRequestProperty(item.getKey(), item.getValue());
            }
//            System.out.println(headers.toString());
        }

        // set request method to GET
        connection.setRequestMethod("GET");

        // throw connection into an inputstream and then read the stream
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while ((currLine = reader.readLine()) != null) {
            response.append(currLine);
        }

        return response.toString();
    }

    /* asynchronous methods */
    // method to update traffic conditions
    public void updateTraffic(final Traffic trafficInfo) {
        final String url = "http://datamall2.mytransport.sg/ltaodataservice/TrafficSpeedBandsv2";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    JSONArray jsonTraffic = jsonRes.getJSONArray("value");
                    trafficInfo.update(jsonTraffic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("|| error on getting traffic info ||");
                System.out.println(error.toString());
                System.out.println("|| error on getting traffic info ||");
            }
        })

        // override the get headers method of the string request to add our own headers
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("AccountKey", "9KPN2QRjSp6u57qOSD1iOQ==");
                params.put("accept", "application/json");
                return params;
            }
        };

        this.requestQueue.add(stringRequest);
    }
    /* asynchronous methods */

    /* synchronous methods */
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
                String response = httpGet(url.toString(), null);
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                System.out.println(url.toString());
                e.printStackTrace();;
            }

            // check the status if there was a response
            if (jsonRes != null) {
                final String status = jsonRes.getString("status");
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

        JSONObject output = currRes.getJSONObject("geometry").getJSONObject("location");
        output.put("place_id", currRes.getString("place_id"));
        output.put("short_name", currRes.getString("formatted_address"));

        return output;
    }

    // get traffic information by API calls to LTA dataMall
    public JSONArray updateTraffic() throws JSONException {
        JSONObject jsonRes = null;
        String response = null;
        final String url = "http://datamall2.mytransport.sg/ltaodataservice/TrafficSpeedBandsv2";
        HashMap<String, String> headers = new HashMap<String, String>();

        // build the headers needed for lta API call
        headers.put("AccountKey", "9KPN2QRjSp6u57qOSD1iOQ==");
        headers.put("accept", "application/json");

        for (int i = 0; i < numTries; i++) {
            // surround the API in a try catch
            try {
                response = httpGet(url, headers);
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check if the response was proper
            if (response != null) {
                flag = jsonRes.has("value");
            }

            // filter out the result then exit loop
            if (flag) {
                flag = false;
                break;
            } else {
                System.out.println(response);
                jsonRes = null;
            }
        }

        // return null if nothing was found
        if (jsonRes == null) {
            return null;
        }

        // filter out the traffic conditions from the results
        return jsonRes.getJSONArray("value");
    }

    // find routes between 2 points
    public JSONArray getRoutes(LatLng startPt, LatLng endPt) throws Exception {
        JSONArray routes = null;
        JSONObject jsonRes = null;
        String response = null;

        // construct the url used to make the API call
        final StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=");
        url.append(startPt.latitude + "," + startPt.longitude);
        url.append("&destination=");
        url.append(endPt.latitude + "," + endPt.longitude);
        url.append("&key=" + gKey);

        // Call API
        for (int i=0; i<numTries; i++) {
            // surround API call in a try catch
            try {
                response = httpGet(url.toString(), null);
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                System.out.println("|| route API exception ||");
                e.printStackTrace();
                System.out.println("|| route API exception ||");
            }

            // check if there was a response
            if (jsonRes != null) {
                final String status = jsonRes.getString("status");
                // set flag as true if status is OK
                flag = status.equals("OK");
            }

            // exit loop
            if (flag) {
                flag  = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.toString());
                jsonRes = null;
            }
        }

        if (jsonRes == null) {
            return null;
        }

        // if a valid result was obtained, extract only the routes
        routes = jsonRes.getJSONArray("routes");

        return routes;
    }

    // find a walkingroute between 2 points
    public JSONObject getWalkingRoute(LatLng startPt, LatLng endPt) throws JSONException {
        JSONObject walkingRoute = null;
        JSONObject jsonRes = null;
        String response = null;

        // construct the url used to make the API call
        final StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=");
        url.append(startPt.latitude + "," + startPt.longitude);
        url.append("&destination=");
        url.append(endPt.latitude + "," + endPt.longitude);
        url.append("&mode=walking");
        url.append("&key=" + gKey);

        // Call API
        for (int i=0; i<numTries; i++) {
            // surround API call in a try catch
            try {
                response = httpGet(url.toString(), null);
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                System.out.println("|| walking route API exception ||");
                e.printStackTrace();
                System.out.println("|| walking route API exception ||");
            }

            // check if there was a response
            if (jsonRes != null) {
                final String status = jsonRes.getString("status");
                // set flag as true if status is OK
                flag = status.equals("OK");
            }

            // exit loop
            if (flag) {
                flag  = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.toString());
                jsonRes = null;
            }
        }

        if (jsonRes == null) {
            return null;
        }

        // if a valid result was obtained, extract only the routes
        walkingRoute = jsonRes.getJSONArray("routes").getJSONObject(0);

        return walkingRoute;
    }

    // method to call weatherbtn forecast API
    public JSONObject getWeatherForecast(Date now) throws Exception {
        String response = null;
        JSONObject jsonRes = null;

        // first build the URL
        final StringBuilder url;
        // if the date is null then get current data
        if (now != null) {
            // format the datetime for API call if it is not null
            String nowStr = now.toString();
            nowStr = nowStr.substring(0, nowStr.indexOf('.'));
            nowStr = nowStr.replace(":", "%3A");
            url = new StringBuilder("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast?date_time=");
            url.append(nowStr);
        } else {
            url = new StringBuilder("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast");
        }


        // call API
        for (int i=0; i<numTries; i++) {
            // surround API call in a try catch
            try {
                response = httpGet(url.toString(), null);
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

    // method to get current weatherbtn conditions
    public JSONObject getWeatherNow(Date now) throws Exception {
        JSONObject jsonRes = null;

        // first build the URL
        final StringBuilder url;
        // if the date is null then get current data
        if (now != null) {
            // format the datetime for API call
            String nowStr = now.toString();
            nowStr = nowStr.substring(0, nowStr.indexOf('.'));
            nowStr = nowStr.replace(":", "%3A");
            url = new StringBuilder("https://api.data.gov.sg/v1/environment/rainfall?date_time=");
            url.append(nowStr);
        } else {
            url = new StringBuilder("https://api.data.gov.sg/v1/environment/rainfall");
        }

        // call API
        for (int i = 0; i < numTries; i++) {
            // surround API call in a try catch
            try {
                String response = httpGet(url.toString(), null);
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

    // method to get information about the capacity of each carpark
    public JSONArray getCarparkAvailability() throws Exception {
        String response;
        JSONObject jsonRes = null;
        JSONArray carparkAvail = null;

        // build the url
        String url = "https://api.data.gov.sg/v1/transport/carpark-availability";

        // make API call
        for (int i=0; i<numTries; i++) {
            try {
                response = httpGet(url, null);
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check if there was a response
            if (jsonRes != null) {
                // set flag as true if the response contains a field items
                flag = jsonRes.has("items");
            }

            // exit loop
            if (flag) {
                flag = false;
                break;
            } else {
                // don't keep non-ok responses
                System.out.println(jsonRes.getString("message"));
                jsonRes = null;
            }
        }

        // return null if no valid result was obtained
        if (jsonRes == null) {
            return null;
        }

        carparkAvail = jsonRes.getJSONArray("items").getJSONObject(0).getJSONArray("carpark_data");
        return carparkAvail;
    }
    /* synchronous methods */

}