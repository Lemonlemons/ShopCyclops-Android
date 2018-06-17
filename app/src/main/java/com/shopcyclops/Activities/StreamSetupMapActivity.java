package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.R;
import com.shopcyclops.Utils.GPSTracker;

/**
 * Created by Andrew on 10/5/2015.
 */
public class StreamSetupMapActivity extends Activity {

    private GoogleMap googleMap;
    GPSTracker gps;
    Button selectBtn;
    MarkerOptions userMarkerOptions;
    Marker userMarker;
    LatLng deliverypoint;
    CircleProgressBar progress;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamsetupmap);
        selectBtn = (Button)findViewById(R.id.btnDeliveryMapSelectPoint);
        progress = (CircleProgressBar)findViewById(R.id.bufferingDeliveryMapProgress);

        prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StreamSetupMapActivity.this, ShoppingActivity.class);
                i.putExtra(CONSTANTS.CURRENT_STREAM_TITLE, getIntent().getStringExtra(CONSTANTS.CURRENT_STREAM_TITLE));
                i.putExtra(CONSTANTS.CURRENT_STREAM_DESCRIPTION, getIntent().getStringExtra(CONSTANTS.CURRENT_STREAM_DESCRIPTION));
                i.putExtra(CONSTANTS.CURRENT_STREAM_STORE, getIntent().getStringExtra(CONSTANTS.CURRENT_STREAM_STORE));
                i.putExtra(CONSTANTS.CURRENT_STREAM_ID, getIntent().getIntExtra(CONSTANTS.CURRENT_STREAM_ID, 0));
                i.putExtra(CONSTANTS.CURRENT_STREAM_HOME_POINT_LAT, deliverypoint.latitude);
                i.putExtra(CONSTANTS.CURRENT_STREAM_HOME_POINT_LAT, deliverypoint.longitude);
                startActivity(i);
            }
        });
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            gps = new GPSTracker(this);
            if (gps.canGetLocation()) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                // latitude and longitude
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                deliverypoint = new LatLng(latitude, longitude);
                // create marker
                userMarkerOptions = new MarkerOptions().position(deliverypoint).title("My Delivery Point");

                // adding marker
                userMarker = googleMap.addMarker(userMarkerOptions);
                CameraPosition cameraPosition = new CameraPosition.Builder().target( new LatLng(latitude, longitude)).zoom(14).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng latLng) {
                        userMarker.remove();
                        deliverypoint = latLng;
                        userMarkerOptions.position(deliverypoint);
                        userMarker = googleMap.addMarker(userMarkerOptions);
                    }
                });
            }
            else {
                Toast.makeText(getApplicationContext(), "Please turn on GPS!", Toast.LENGTH_SHORT).show();
            }

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
