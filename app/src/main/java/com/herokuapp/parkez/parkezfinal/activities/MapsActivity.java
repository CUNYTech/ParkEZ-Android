package com.herokuapp.parkez.parkezfinal.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.herokuapp.parkez.parkezfinal.models.GPSTracker;
import com.herokuapp.parkez.parkezfinal.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private double lat;
    private double lng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_port);
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
        mMap.addMarker(new MarkerOptions().position(currentLoc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

        // Add a marker in heraldSquare and move the camera
        /*
        LatLng heraldSquare = new LatLng(40.7496439, -73.9876706);
        mMap.addMarker(new MarkerOptions().position(heraldSquare));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(heraldSquare, 15));
        */
    }
}
