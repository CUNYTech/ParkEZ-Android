package com.herokuapp.parkez.parkezfinal.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.models.GPSTracker;
import com.herokuapp.parkez.parkezfinal.models.User;
import com.herokuapp.parkez.parkezfinal.web.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private double lat;
    private double lng;
    private List<LatLng> locations;
    private OkHttpClient client;
    protected SharedPreferences sharedpreferences;// Shared preference variable
    private static final String USER_PREFS = "USER PREFS";
    protected static final String UID = "Uid";
    protected static final String ClientID = "Client";
    protected static final String TOKEN = "Token";
    protected static final String EXPIRY = "Expiry";
    protected static final String NAME = "Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locations = new ArrayList<>();
        this.client = WebUtils.getClient();
        sharedpreferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // create gpstracker object
        gpsTracker = new GPSTracker(MapsActivity.this);


        // get current gps coordinates
        lat = gpsTracker.getLatitude();
        lng = gpsTracker.getLongitude();

        Log.d("GPS", "" + Double.toString(lat) + " " + Double.toString(lng));

        // add a marker to current location and move the camera
        LatLng currentLoc = new LatLng(lat, lng);
        mMap.addCircle(new CircleOptions().center(currentLoc)).setRadius(30.0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

        // Add a marker in heraldSquare and move the camera
        /*
        LatLng heraldSquare = new LatLng(40.7496439, -73.9876706);
        mMap.addMarker(new MarkerOptions().position(heraldSquare));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(heraldSquare, 15));
        */

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                locations.add(point); // store the current location
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("latitude", (Object) point.latitude);
                    jsonObject.put("longitude", (Object) point.longitude);
                    jsonObject.put("status", "free");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Request request = WebUtils.addTokenAuthHeaders("parking_locations", getUser())
                        .post(WebUtils.getBody(WebUtils.JSON, jsonObject.toString())).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Are you connected to the network?", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("[report spot]", "Something went wrong: ", e);
                    }


                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LatLng point = locations.get(locations.size() - 1);
                                    mMap.addMarker(new MarkerOptions().position(point)); // add a marker
                                    Log.d("Reported spot", "" + Double.toString(point.latitude) + " " + Double.toString(point.latitude)); // debuggy the thingy
                                }
                            });

                        } else {
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });


            }
        });
    }

    private User getUser() {
        String uid = sharedpreferences.getString(UID, "");
        String token = sharedpreferences.getString(TOKEN, "");
        String expiry = sharedpreferences.getString(EXPIRY, "");
        String clientId = sharedpreferences.getString(ClientID, "");
        String name = sharedpreferences.getString(NAME, "");
        if (uid.isEmpty() || token.isEmpty() || expiry.isEmpty() || clientId.isEmpty()) {
            return null;
        } else {
            return new User(uid, token, clientId, expiry, name);
        }
    }

}
