package com.herokuapp.parkez.parkezfinal.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.herokuapp.parkez.parkezfinal.BuildConfig;
import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.models.GPSTracker;
import com.herokuapp.parkez.parkezfinal.models.ParkingLocation;
import com.herokuapp.parkez.parkezfinal.models.User;
import com.herokuapp.parkez.parkezfinal.web.utils.WebUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// import android.support.v4.app.ActivityCompat;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    protected static final String UID = "Uid";
    protected static final String ClientID = "Client";
    protected static final String TOKEN = "Token";
    protected static final String EXPIRY = "Expiry";
    protected static final String NAME = "Name";
    protected static final String CHECKED_IN = "checked_in";
    private static final String USER_PREFS = "USER PREFS";
    private final int SUCCESS_LOGOUT = 3;
    protected SharedPreferences sharedpreferences;// Shared preference variable
    private GoogleMap mMap;
    private Map<Marker, ParkingLocation> parkingLocationMap = new HashMap<>();
    private OkHttpClient client;
    private boolean checked_in = false;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.client = WebUtils.getClient();
        sharedpreferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        this.checked_in = sharedpreferences.getBoolean(CHECKED_IN, false);
        // set navigation drawer layout with maps fragment included
        setContentView(R.layout.activity_navigation);
        // allow toolbar as action
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.check_out).setVisible(checked_in);
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
        gpsTracker = new GPSTracker(MapsActivity.this);


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

        //TODO: consider having a button to refresh this and maybe make it "real time"
        getAvailableSpacesNear(getRequestToShowAvailableParkingSpotsNear(currentLoc));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                reportSpot(getRequestForPoint(requestBuilder, latLng, "free"), latLng);
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                reportSpot(getRequestForPoint(requestBuilder, latLng, "free"), latLng);
            }
        });

        //TODO: these are the necessary listeners
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checked_in) {
                            Toast.makeText(getApplicationContext(),
                                    "Already checked in to a spot, you can't be in two places at once!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                        alert.setTitle("ParkEZ parking spot check-in");
                        alert.setMessage("Would you like to check-in to this spot?");

                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                checkIn(marker);
                            }
                        });

                        alert.setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });

                        alert.show();
                    }
                });
                return true;
            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            ParkingLocation loc = null;

            @Override
            public void onMarkerDragStart(Marker marker) {
                if (BuildConfig.DEBUG)
                    Log.i("Marker drag", "start");
                loc = parkingLocationMap.get(marker);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.i("Marker drag", marker.getPosition().toString());
            }

            @Override
            public void onMarkerDragEnd(final Marker marker) {
                Log.i("Marker drag", "end");
                // find the marker with this latitude and update it
                Log.i("Marker drag", marker.getPosition().toString());
                loc.setLatitude(marker.getPosition().latitude);
                loc.setLongitude(marker.getPosition().longitude);
                Request.Builder requestBuilder = WebUtils.addTokenAuthHeaders(String.format("/parking_locations/%d", loc.getId()), getUser())
                        .patch(WebUtils.getBody(WebUtils.JSON, serialize(loc)));

                client.newCall(requestBuilder.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                        Log.e("[drag]", "Something went wrong: ", e);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if (!checkValidityOfSession(response)) return;
                        final String json = response.body().string();
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                            String.format(Locale.ENGLISH, "Successfully updated the location to %f, %f", marker.getPosition().latitude,
                                                    marker.getPosition().longitude), Toast.LENGTH_LONG).show();
                                    parkingLocationMap.put(marker, deserialize(json));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    Log.d("[drag]", "Unable to save");
                                }
                            }
                        });
                        response.body().close();
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

    private String getJSONForRequest(LatLng point, String status) {
        ParkingLocation p = new ParkingLocation(null, point.latitude, point.longitude, status);
        return serialize(p);
    }


    private Request getRequestToShowAvailableParkingSpotsNear(LatLng latLng) {
        final Request.Builder requestBuilder = WebUtils.addTokenAuthHeaders("/spots", getUser());
        return requestBuilder.post(WebUtils.getBody(WebUtils.JSON, getJSONForRequest(latLng, ""))).build(); // we ignore the status in this case -- it will not be parsed.
    }

    private void getAvailableSpacesNear(Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Are you connected to the network?", Toast.LENGTH_LONG).show();
                    }
                });
                Log.e("[find available spots]", "Something went wrong: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!checkValidityOfSession(response)) {
                    return;
                }
                List<ParkingLocation> availableSpots = getListOfSpots(response.body().string());
                if (availableSpots.isEmpty()) {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "There are currently no available spots :'(", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    showSpots(availableSpots);
                }
                response.body().close();
            }
        });
    }

    private List<ParkingLocation> getListOfSpots(String json) {
        // deserialize the list we got from the server
        Gson gson = new Gson();
        List<ParkingLocation> ret;
        Type parkingLocationListType = new TypeToken<Collection<ParkingLocation>>() {
        }.getType();
        Log.d("[spots json]", json);
        ret = gson.fromJson(json, parkingLocationListType);
        for (ParkingLocation parkingLocation : ret) {
            Log.d("[spots]", parkingLocation.toString());
        }
        return ret;
    }

    private void showSpots(final List<ParkingLocation> parkingLocations) {
        // loop through all the locations..
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ParkingLocation parkingLocation : parkingLocations) {
                    final Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(parkingLocation.getLatitude(), parkingLocation.getLongitude())).draggable(false));
                    parkingLocationMap.put(marker, parkingLocation);
                }
            }
        });
    }

    private Request getRequestForPoint(Request.Builder builder, LatLng point, String status) {
        return builder.post(WebUtils.getBody(WebUtils.JSON, getJSONForRequest(point, status))).build();
    }

    private boolean checkValidityOfSession(final Response response) {
        if (!WebUtils.isAuthenticationValidity(response)) {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sharedpreferences.edit().clear().apply();
                    Intent goMainIntent = new Intent(MapsActivity.this, MainActivity.class);
                    goMainIntent.setFlags(SUCCESS_LOGOUT);
                    startActivity(goMainIntent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Login session has expired.", Toast.LENGTH_LONG).show();
                }
            });// clear out login data
            response.body().close();
            return false;
        } else {
            return true;
        }
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
            public void onResponse(Call call, final Response response) throws IOException {
                if (!checkValidityOfSession(response)) {
                    return;
                } else if (response.isSuccessful()) {
                    final String json = response.body().string();
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ParkingLocation parkingLocation = deserialize(json);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(point).draggable(true)); // add a marker
                            parkingLocationMap.put(marker, parkingLocation);
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



    private void checkIn(final Marker marker) {

        final ParkingLocation parkingLocation = parkingLocationMap.get(marker);
        ;
        parkingLocation.setStatus("occupied");
        String json = serialize(parkingLocation);

        final Request.Builder reqBuilder = WebUtils.addTokenAuthHeaders(String.format(Locale.ENGLISH, "/parking_locations/%d", parkingLocation.getId()), getUser())
                .patch(WebUtils.getBody(WebUtils.JSON, json));
        client.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Are you connected to the network?", Toast.LENGTH_LONG).show();
                    }
                });
                Log.e("[check in]", "Something went wrong: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            marker.setVisible(false);
                            parkingLocationMap.put(marker, parkingLocation);
                            Toast.makeText(getApplicationContext(), "You have been checked in.", Toast.LENGTH_LONG).show();
                            MapsActivity.this.checked_in = true;
                            navigationView.getMenu().findItem(R.id.check_out).setVisible(true);
                            sharedpreferences.edit().putBoolean(CHECKED_IN, true).apply();
                        }
                    });

                } else if (checkValidityOfSession(response)) {
                    return;

                } else {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.e("[check in]", "Something went wrong: ");
                }
                response.body().close();
            }

        });


    }

    private String serialize(ParkingLocation parkingLocation) {
        Gson gson = new Gson();
        Type parkingLocationType = new TypeToken<ParkingLocation>() {
        }.getType();
        return gson.toJson(parkingLocation, parkingLocationType);
    }

    private ParkingLocation deserialize(String json) {
        Gson gson = new Gson();
        Type parkingLocationType = new TypeToken<ParkingLocation>() {
        }.getType();
        return gson.fromJson(json, parkingLocationType);
    }

    private void promptforLogout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        alert.setTitle("Do you want to Logout?");
        StringBuilder sb = new StringBuilder("Are you sure you want to logout? ");
        if (checked_in)
            sb.append("Doing so will automatically check you out from your parking spot.");
        alert.setMessage(sb.toString());

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                logout();
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

        alert.show();

    }
    private void logout() {
        checkOut(); // logging out will automatically check you out.
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
        if (id == R.id.action_refesh_spots) {
            double lat = gpsTracker.getLatitude();
            double lng = gpsTracker.getLongitude();
            mMap.clear();
            getAvailableSpacesNear(getRequestToShowAvailableParkingSpotsNear(new LatLng(lat, lng)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            promptforLogout();

        } else if (id == R.id.check_out) {
            promptCheckOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void promptCheckOut() {
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setTitle("Do you want to Check Out???");
                alert.setMessage("Are you sure you want to Check out?");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                      checkOut();
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            }
        });

    }

    private void checkOut() {
        final Request.Builder reqBuilder = WebUtils.addTokenAuthHeaders("/check_out", getUser())
                .delete();
        client.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Are you connected???", Toast.LENGTH_LONG).show();
                        Log.e("[check out]", "Something went wrong", e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!checkValidityOfSession(response)) {
                    return;
                } else if (response.isSuccessful()) {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Thank you for using ParkEZ, come again!!", Toast.LENGTH_LONG).show();
                            MapsActivity.this.checked_in = false;
                            ;
                            navigationView.getMenu().findItem(R.id.check_out).setVisible(false);
                            sharedpreferences.edit().putBoolean(CHECKED_IN, false).apply();
                        }
                    });
                } else {
                    Log.d("[check out]", "Something went wrong");
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                        }
                    });

                }
                response.body().close();
            }
        });
    }


}
