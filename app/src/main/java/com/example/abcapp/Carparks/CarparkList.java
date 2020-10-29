package com.example.abcapp.Carparks;

import android.content.Context;

import com.example.abcapp.MapController;
import com.example.abcapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class CarparkList {
    // a Context object for referencing files
    private Context context;

    // An ArrayList to store the carparks
    private static HashMap<String, Carpark> carparks;

    public CarparkList(Context context) throws IOException, JSONException {
        this.context = context;

        createCarparks();
    }

    // method to create an ArrayList of carparks by reading csv
    public void createCarparks() throws IOException, JSONException {
        // instantiate the carparks ArrayList
        CarparkList.carparks = new HashMap<String, Carpark>();

        ArrayList carparkInfo = readCSV(R.raw.carpark_info, "\t");

        // use the first line of the csv as the fields
        String[] fields = (String[]) carparkInfo.get(0);

        for (int i=1; i<carparkInfo.size(); i++) {
            // create a JSONObject to feed to the Carpark constructor
            JSONObject carparkJSON = new JSONObject();

            // iterate through each line, convert the relevant info to JSON
            String[] currLine = (String[]) carparkInfo.get(i);
            String carparkNo = null;
            for (int j=0; j<fields.length; j++) {
                String field = fields[j].replaceAll("\"", "");

                // keep track of the carpark_no which can be used to identify each carpark
                if (field.equals("car_park_no")) {
                    carparkNo = currLine[j].replaceAll("\"", "");
                }
                carparkJSON.put(field, currLine[j].replaceAll("\"", ""));
            }

            CarparkList.carparks.put(carparkNo, new Carpark(carparkJSON));
        }
    }

    // method to read a CSV
    private ArrayList readCSV(int resourceId, String separator) throws IOException {
        ArrayList<String[]> output = new ArrayList<String[]>();

        // create an InputStream and BufferedReader to read the file
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("UTF-8"))
        );

        // keep track of the current line
        String currLine = null;
        String[] currEntries = null;
        while ((currLine = reader.readLine()) != null) {
            // first separate each line by commas
            currEntries = currLine.split(separator);

            // throw the data into the output ArrayList
            output.add(currEntries);
        }

        return output;
    }

    // method to get the size of the carparkList
    public static int size() {
        return CarparkList.carparks.size();
    }

    // accessor method to access a particular carpark
    public static Carpark getCarpark(String carparkNo) {
        return CarparkList.carparks.get(carparkNo);
    }

    // accessor to get all carparks
    public static HashMap<String, Carpark> getCarparks() {
        return CarparkList.carparks;
    }
}
