package com.twobitworkshop.mybusaway;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
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

public class MainActivity extends AppCompatActivity
{
    protected Button btnHit;
    protected ProgressDialog pd;

    protected double latitude;
    protected double longitude;

    private LocationManager locationManager;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = 0;
        longitude = 0;
        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        btnHit = (Button) findViewById(R.id.btnHit);

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://api.pugetsound.onebusaway.org/api/where/stops-for-location.json";
                url += "?key=" + R.string.oba_api_key;
                url += "&lat=" + latitude + "&lon=" + longitude;
                new JsonTask().execute(url);
            }
        });
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
}
