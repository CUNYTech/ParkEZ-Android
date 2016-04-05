package com.herokuapp.parkez.parkezfinal.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

// import android.support.v4.app.ActivityCompat;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

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
    private final int SUCCESS_LOGOUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.client = WebUtils.getClient();
        sharedpreferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        // set navigation drawer layout with maps fragment included
        setContentView(R.layout.activity_navigation);
        // allow toolbar as action
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onBackPressed() {
        /*
        The method finish() takes the user to the login page
        as it was the previous activity. In the previous page,
        it shows the user the login form again which disrupts the
        intuitive flow of the app.
        finish();
        */

        // if nav drawer is opened, close it
        // if not, exit app
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            // goes back to home screen overriding the navigation path
            // this is to ensure intuitive navigation as back press would
            // go back to login page asking user input the login info again
            Intent goBack = new Intent(Intent.ACTION_MAIN);
            goBack.addCategory(Intent.CATEGORY_HOME);
            goBack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goBack);
        }

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 16));
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
                Toast.makeText(getApplicationContext(), "Marker clicked! " + marker.getPosition().latitude + "," + marker.getPosition().longitude, Toast.LENGTH_SHORT).show();
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
        return builder.post(WebUtils.getBody(WebUtils.JSON, getJSONForRequest(point))).build();
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
                            Toast.makeText(getApplicationContext(), "Thank you for helping your community!", Toast.LENGTH_SHORT).show();
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

    private void logout(SharedPreferences preferences, User user) {
        final Request.Builder requestBuilder = WebUtils.addTokenAuthHeaders("/auth/sign_out", getUser()).delete();
        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Are you connected to the network?", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sharedpreferences.edit().clear().apply();
                            Toast.makeText(getApplicationContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();
                            Intent goMainIntent = new Intent(MapsActivity.this, MainActivity.class);
                            goMainIntent.setFlags(SUCCESS_LOGOUT);
                            startActivity(goMainIntent);
                            finish();
                        }
                    });
                } else {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Invalid credentials.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } // onResponse
        });
    } // user

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            logout(sharedpreferences, getUser());

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
