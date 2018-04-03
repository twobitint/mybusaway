package com.twobitworkshop.mybusaway;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity
{
    protected Button btnHit;
    protected ProgressDialog pd;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;

    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationRequest = new LocationRequest();
        mRequestingLocationUpdates = true;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                }
            }
        };

        startLocationUpdates();

        if (checkPermission()) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location l) {
                            // Got last known location. In some rare situations this can be null.
                            if (l != null) {
                                // Logic to handle location object
                                location = l;
                            }
                        }
                    });
        }

        btnHit = (Button) findViewById(R.id.btnHit);
        btnHit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    String url = "https://api.pugetsound.onebusaway.org/api/where/stops-for-location.json";
                    url += "?key=" + getString(R.string.oba_api_key);
                    url += "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
                    new JsonTask().execute(url);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public boolean checkPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private class JsonTask extends AsyncTask<String, String, String>
    {
        protected void onPreExecute()
        {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params)
        {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                Log.d("MBA", url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            try {
                JSONObject stopsResponse = new JSONObject(result);
                JSONObject stopsData = stopsResponse.getJSONObject("data");
                JSONArray stops = stopsData.getJSONArray("list");

                PriorityQueue<BusStop> stopQueue = new PriorityQueue<>(10, new Comparator<BusStop>() {
                    @Override
                    public int compare(BusStop busStop, BusStop t1) {
                        return Double.compare(busStop.distanceFromLocation, t1.distanceFromLocation);
                    }
                });

                for (int i = 0; i < stops.length(); i++) {
                    JSONObject stop = stops.getJSONObject(i);
                    BusStop bs = new BusStop(stop);
                    stopQueue.add(bs);
                }

                Log.d("Bus", stopQueue.peek().name);

            } catch (JSONException e) {
                largeLog("JSON Error: ", e.getMessage());
                e.printStackTrace();
            }


            //txtJson.setText(result);
        }

        public void largeLog(String tag, String content) {
            if (content.length() > 4000) {
                Log.d(tag, content.substring(0, 4000));
                largeLog(tag, content.substring(4000));
            } else {
                Log.d(tag, content);
            }
        }
    }

    private class BusStop
    {
        public String code;
        public String direction;
        public String id;
        public double latitude;
        public double longitude;
        public int type;
        public String name;
        public String wheelChairBoarding;
        public ArrayList<String> routes;

        public double distanceFromLocation;

        public BusStop(JSONObject stop) throws JSONException
        {
            routes = new ArrayList<>();

            JSONArray routeIds = stop.getJSONArray("routeIds");
            for (int i = 0; i < routeIds.length(); i++) {
                routes.add(routeIds.getString(i));
            }

            code = stop.getString("code");
            direction = stop.getString("direction");
            id = stop.getString("id");
            latitude = stop.getDouble("lat");
            longitude = stop.getDouble("lon");
            type = stop.getInt("locationType");
            name = stop.getString("name");
            wheelChairBoarding = stop.getString("wheelchairBoarding");

            calculateDistance();
        }

        public void calculateDistance()
        {
            distanceFromLocation = Math.sqrt(
                    Math.pow((location.getLatitude() - latitude), 2) +
                            Math.pow((location.getLongitude() - longitude), 2)
            );
        }
    }
}
