package com.herokuapp.parkez.parkezfinal.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.herokuapp.parkez.parkezfinal.BuildConfig;
import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.models.GPSTracker;
import com.herokuapp.parkez.parkezfinal.models.ParkingLocation;
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
    private List<LatLng> parkingSpots = new ArrayList<>();
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
        final Request.Builder requestBuilder = WebUtils.addTokenAuthHeaders("parking_locations", getUser());
        mMap = googleMap;
        // create gpstracker object
        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);


        // get current gps coordinates
        double lat = gpsTracker.getLatitude();
        double lng = gpsTracker.getLongitude();

        Log.d("GPS", "" + Double.toString(lat) + " " + Double.toString(lng));

        // add a marker to current location and move the camera
        LatLng currentLoc = new LatLng(lat, lng);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Request request = getRequestForPoint(requestBuilder, latLng);
                reportSpot(request, latLng);
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Request request = getRequestForPoint(requestBuilder, latLng);
                reportSpot(request, latLng);
            }
        });

        //TODO: these are the necessary listeners
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "Marker clicked! " + marker.getPosition().latitude + "," + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
                return true;
            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (BuildConfig.DEBUG)
                    Log.i("Marker drag", "start");
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.i("Marker drag", marker.getPosition().toString());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.i("Marker drag", "end");
                // find the marker with this latitude and update it
                Log.i("Marker drag", marker.getPosition().toString());
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

    private String getJSONForRequest(LatLng point) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", (Object) point.latitude);
            jsonObject.put("longitude", (Object) point.longitude);
            jsonObject.put("status", "free");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Request getRequestForPoint(Request.Builder builder, LatLng point) {
        Request request = builder.post(WebUtils.getBody(WebUtils.JSON, getJSONForRequest(point))).build();
        return request;
    }

    private void reportSpot(Request request, final LatLng point) {
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

                            mMap.addMarker(new MarkerOptions().position(point).draggable(true)); // add a marker
                            Toast.makeText(getApplicationContext(), "Thank you for helping your community Park EZ!", Toast.LENGTH_LONG).show();
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
                response.body().close();
            }
        });
    }

    /**
     * Get the available parking spots
     *
     * @param request
     * @param point
     * @return
     */
    private List<ParkingLocation> getAvailableSpacesNear(Request request, LatLng point) {
        List<ParkingLocation> parkingLocations = new ArrayList<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        return parkingLocations;
    }

    private void checkIn(Marker marker) {
        marker.setVisible(false);
        // set the status to occupied for this location.
    }

}
