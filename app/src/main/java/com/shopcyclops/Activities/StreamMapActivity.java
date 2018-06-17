package com.shopcyclops.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.R;
import com.shopcyclops.Utils.GPSTracker;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 9/20/2015.
 */
public class StreamMapActivity extends Activity {

    // Google Map
    private GoogleMap googleMap;
    GPSTracker gps;
    Button selectBtn;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private ListView mDrawerList;
    private String allStreams;
    MarkerOptions userMarkerOptions;
    Marker userMarker;
    LatLng deliverypoint;
    CircleProgressBar progress;
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streammap);
        selectBtn = (Button)findViewById(R.id.btnSelectPoint);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.navList);
        mActivityTitle = getTitle().toString();
        progress = (CircleProgressBar)findViewById(R.id.bufferingProgress);

        prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        allStreams = prefs.getString(CONSTANTS.ALL_STREAMS, null);

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit()
                        .putFloat(CONSTANTS.CURRENT_DELIVERY_LAT, (float)deliverypoint.latitude)
                        .putFloat(CONSTANTS.CURRENT_DELIVERY_LNG, (float)deliverypoint.longitude)
                        .apply();
                Intent i = new Intent(StreamMapActivity.this, StreamMainActivity.class);
                startActivity(i);
                //StreamMapActivity.this.finish();
            }
        });

        setupDrawer();
        addDrawerItems();
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
                userMarkerOptions = new MarkerOptions().position(deliverypoint).title("My Location");

                // adding marker
                userMarker = googleMap.addMarker(userMarkerOptions);
                CameraPosition cameraPosition = new CameraPosition.Builder().target( new LatLng(latitude, longitude)).zoom(16).build();

                MarkerOptions streamMarker;
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                try {
                    JSONArray jsonArr = new JSONArray(allStreams);
                    JSONObject opper;
                    Stream transfer = new Stream();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        opper = jsonArr.getJSONObject(i);
                        transfer.setLatitude(opper.getDouble("lat"));
                        transfer.setLongitude(opper.getDouble("lng"));
                        transfer.setTitle(opper.getString("name"));
                        streamMarker = new MarkerOptions()
                                .position(new LatLng(transfer.getLatitude(), transfer.getLongitude()))
                                .title(transfer.getTitle())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        googleMap.addMarker(streamMarker);
                    }
                }
                catch (JSONException e){
                    System.out.println(e.toString());
                }

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



    private void refresh() {
        try {
            progress.setVisibility(View.VISIBLE);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.get(this, CONSTANTS.BASE_URL+"/mobileallstreams", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(json.toString());
                    progress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                    try {
                        googleMap.clear();
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        prefs.edit().putString(CONSTANTS.ALL_STREAMS, json.toString()).apply();
                        // create marker
                        userMarkerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Location");

                        // adding marker
                        userMarker = googleMap.addMarker(userMarkerOptions);

                        MarkerOptions marker;
                        JSONObject opper;
                        Stream transfer;
                        for (int i = 0; i < json.length(); i++) {
                            opper = json.getJSONObject(i);
                            transfer = new Stream();
                            transfer.setLatitude(opper.getDouble("lat"));
                            transfer.setLongitude(opper.getDouble("lng"));
                            transfer.setTitle(opper.getString("name"));
                            marker = new MarkerOptions()
                                    .position(new LatLng(transfer.getLatitude(), transfer.getLongitude()))
                                    .title(transfer.getTitle())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            googleMap.addMarker(marker);
                        }
                        progress.setVisibility(View.INVISIBLE);
                    }
                    catch (Exception e) {
                        progress.setVisibility(View.INVISIBLE);
                        System.out.println(e.toString());
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });


        } catch (Exception e) {
            progress.setVisibility(View.INVISIBLE);
            System.out.println(e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu:
                toggleActionBarMenu();
                return true;
            case R.id.refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleActionBarMenu() {
        if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        else
            mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void addDrawerItems() {
        SharedPreferences prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final String token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
        List<String> optionsArray = new ArrayList<String>();
        if (token == null) {
            optionsArray.add("SIGN UP");
            optionsArray.add("LOGIN");
        } else {
            optionsArray.add("MY PROFILE");
            optionsArray.add("CREATE BROADCAST");
            optionsArray.add("PAYMENT");
            optionsArray.add("MY ORDERS");
        }
        ArrayAdapter<String> settingsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, optionsArray);
        mDrawerList.setAdapter(settingsAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (token == null) {
                    switch (position) {
                        case 0:
                            gotosignup();
                            break;
                        case 1:
                            gotologin();
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            gotoprofile();
                            break;
                        case 1:
                            createBroadcast();
                            break;
                        case 2:
                            gotopaymentinfo();
                            break;
                        case 3:
                            gotoorderinfo();
                            break;
                    }
                }
            }
        });
    }

    public void gotosignup()
    {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    public void gotologin()
    {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void gotoprofile()
    {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    public void createBroadcast()
    {
        SharedPreferences prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        boolean is_cyclops = prefs.getBoolean(CONSTANTS.IS_CYCLOPS_KEY, false);
        if (is_cyclops == true)
        {
            Intent i = new Intent(this, StreamSetupActivity.class);
            startActivity(i);
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Become a Cyclops")
                    .setMessage("Complete your registration to create your own broadcasts!")
                    .setPositiveButton("Complete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(StreamMapActivity.this, StripeActivity2.class);
                            startActivity(i);
                            StreamMapActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(R.drawable.ic_launcher)
                    .show();
        }
    }

    public void gotopaymentinfo()
    {
        Intent i = new Intent(this, PaymentInfoActivity.class);
        startActivity(i);
    }

    public void gotoorderinfo()
    {
        Intent i = new  Intent(this, OrderInfoActivity.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }

}
