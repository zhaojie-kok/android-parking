package com.example.abcapp;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.time.LocalDateTime;

public class APIcaller {
    // boolean flag for general use wherever needed
    private boolean flag = false;
    private final static int numTries = 5;
    private final static String gKey = "AIzaSyCemoI4we1HrZ2z4TE8FvcEQYscriomdxs";

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
        // create boolean flag for everything
        final APIcaller caller = new APIcaller();
        JSONObject jsonRes = null;

        // format the name of the location then build url for API call
        name = name.replaceAll("\\s+", "+");
        final StringBuilder url = new StringBuilder(("https://maps.googleapis.com/maps/api/geocode/json?address="));
        url.append("Singapore,+" + name);
        url.append("&key=" + gKey);

        for (int i = 0; i < numTries; i++) {
            // surround API call in a try catch
            try {
                String response = caller.httpGet(url.toString());
                jsonRes = new JSONObject(response);
            } catch (Exception e) {
                e.printStackTrace();;
            }

            // check the status if there was a response
            if (jsonRes != null) {
                final String status = (String) jsonRes.get("status");
                // System.out.println(status);
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

        if (jsonRes == null) {
            return null;
        }

        // find coordinates from the results
        final JSONArray results = (JSONArray) jsonRes.get("results");
        final JSONObject currRes = (JSONObject) results.get(0); // assume first result is best match
        // TODO: add other useful stuff like name and place_id

        return currRes.getJSONObject("geometry").getJSONObject("location");
    }

    // method to call weather forecast API
    public JSONObject getWeatherForecast(LocalDateTime now) throws Exception {
        String response;
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
    public JSONObject getWeatherNow(LocalDateTime now) throws Exception {
        String response;
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

    // method to get carpark availaility from URA carparks
    public JSONArray checkCarparks(LocalDateTime now) throws Exception {
        String response;
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

        // filter the carpark vancancy information
        if (jsonRes != null) {
            carparkInfo = jsonRes.getJSONArray("items").getJSONObject(0).getJSONArray("carpark_data");
        }

        return carparkInfo;
    }
}