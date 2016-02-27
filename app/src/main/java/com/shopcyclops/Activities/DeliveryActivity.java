package com.shopcyclops.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.Fragments.Delivery.DeliveryListFragment;
import com.shopcyclops.Fragments.Delivery.DeliverySignFragment;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;
import com.shopcyclops.Fragments.Delivery.Order;
import com.shopcyclops.Utils.GPSTracker;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 9/27/2015.
 */
public class DeliveryActivity extends FragmentActivity implements DeliveryListFragment.OnDeliveryInterface {

    private GoogleMap googleMap;
    GPSTracker gps;
    double latitude;
    double longitude;

    LatLng start;
    LatLng end;

    private List<CartItem> mItems;
    private List<Order> mOrders;
    CircleProgressBar progress;
    LinearLayout deliveryBtnContainer;
    Button finishDeliveryBtn;

    List<LatLng> waypoints;
    private Polyline bigMapPolyline;
    private Polyline littleMapPolyline;

    Order[] deliveryLegs;

    List<Integer> viewerIds;
    List<Boolean> currentPolylineBooleans;

    StringBuilder googleURL;

    int stream_id;
    int selected_order_id;
    int chosen_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new DeliveryAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        //mViewPager = (ViewPager) findViewById(R.id.pager);
        //mViewPager.setAdapter(mSectionsPagerAdapter);

        progress = (CircleProgressBar) findViewById(R.id.deliveryProgress);
        deliveryBtnContainer = (LinearLayout) findViewById(R.id.finishDeliveryLayout);
        finishDeliveryBtn = (Button) findViewById(R.id.btnCompleteDelivery);

        finishDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(DeliveryActivity.this)
                        .setTitle("Congrats!")
                        .setMessage("You've completed your deliveries and your payments are in your stripe account")
                        .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(DeliveryActivity.this, StreamMainActivity.class);
                                startActivity(i);
                                DeliveryActivity.this.finish();
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
        });

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getOrders();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                getActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompleted(int position)
    {
        //Do something with the position value passed back
        Toast.makeText(this, "Clicked " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void switchFragment(int order_id)
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.deliveryFrame, DeliverySignFragment.newInstance(order_id))
                .addToBackStack(null)
                .commit();
    }

    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.DeliveryMap)).getMap();

            gps = new GPSTracker(this);
            if (gps.canGetLocation()) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        ArrayList<CartItem> cartList = new ArrayList<CartItem>();
                        if (marker.getTitle().equals("start"))
                        {
                            Toast.makeText(DeliveryActivity.this, "You can't select the starting point", Toast.LENGTH_SHORT).show();
                        }
                        else if (marker.getTitle().equals("end")) {
                            Toast.makeText(DeliveryActivity.this, "You can't select the ending point", Toast.LENGTH_SHORT).show();
                        }
                        else if (Integer.parseInt(marker.getTitle()) != chosen_order) {
                            Toast.makeText(DeliveryActivity.this, "You must make deliveries in the proper order", Toast.LENGTH_SHORT).show();
                        }
                        else if (Integer.parseInt(marker.getTitle()) == chosen_order) {
                            selected_order_id = Integer.parseInt(marker.getTitle());
                            for (int i = 0; i < mItems.size(); i++) {
                                if (mItems.get(i).getOrder_id() == selected_order_id) {
                                    cartList.add(mItems.get(i));
                                }
                            }
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.deliveryFrame, DeliveryListFragment.newInstance(cartList))
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else {
                            Toast.makeText(DeliveryActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
            }
            else {
                Toast.makeText(this, "Please turn on GPS!", Toast.LENGTH_SHORT).show();
            }

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void getOrders() {
        progress.setVisibility(View.VISIBLE);
        mItems = new ArrayList<>(0);
        mItems.clear();
        mOrders = new ArrayList<>(0);
        mOrders.clear();
        final SharedPreferences prefs = getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String token = prefs.getString(SECRETS.TOKEN_KEY, null);
        String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        stream_id = getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.addHeader("X-User-Token", token);
        client.addHeader("X-User-Email", user_email);
        client.get(this, SECRETS.BASE_URL + "/mobileassociatedordersanditems?streamid=" + stream_id, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                if (json.toString() == null)
                {
                    Toast.makeText(DeliveryActivity.this, "Not connected to the internet", Toast.LENGTH_LONG).show();
                } else {
                    System.out.println(json.toString());
                    Toast.makeText(DeliveryActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                System.out.println(throwable.toString());
                Toast.makeText(DeliveryActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                try {
                    JSONArray orders = json.getJSONArray("orders");
                    JSONArray items = json.getJSONArray("items");

                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject order = orders.getJSONObject(i);
                        Order container = new Order();
                        container.setId(order.getInt("id"));
                        container.setLat((float) order.getDouble("lat"));
                        container.setLng((float) order.getDouble("lng"));
                        container.setViewer_id(order.getInt("viewer_id"));
                        container.setStream_id(order.getInt("stream_id"));
                        container.setTaxrate((float) order.getDouble("taxrate"));
                        container.setPricebeforetax(order.getInt("pricebeforetax"));
                        container.setPricebeforefees(order.getInt("pricebeforefees"));
                        container.setTotalprice(order.getInt("totalprice"));
                        container.setCardcode(order.getString("cardcode"));
                        container.setIs_delivered(order.getBoolean("is_delivered"));
                        container.setWaypoint(new LatLng(container.getLat(), container.getLng()));
                        System.out.println(container.toString());
                        mOrders.add(container);
                    }

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        CartItem transfer = new CartItem();
                        transfer.setId(item.getInt("id"));
                        transfer.setItemname(item.getString("contents"));
                        transfer.setPrice((float) item.getDouble("price"));
                        transfer.setQuantity(item.getInt("quantity"));
                        transfer.setStatus(item.getString("status"));
                        transfer.setImageurl(item.getString("imageurl"));
                        transfer.setStream_id(item.getInt("stream_id"));
                        transfer.setProgress(item.getInt("progress"));
                        transfer.setViewer_id(item.getInt("viewer_id"));
                        transfer.setOrder_id(item.getInt("order_id"));
                        System.out.println(transfer.toString());
                        mItems.add(transfer);
                    }
                    createRoute();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        });
    }

    public void createRoute()
    {
        try {
            start = new LatLng(latitude, longitude);
            waypoints = new ArrayList<>();
            waypoints.add(start);
            end = new LatLng(getIntent().getDoubleExtra(SECRETS.CURRENT_STREAM_HOME_POINT_LAT, 0), getIntent().getDoubleExtra(SECRETS.CURRENT_STREAM_HOME_POINT_LNG, 0));
            googleURL = new StringBuilder();
            googleURL.append("https://maps.googleapis.com/maps/api/directions/json?origin=" + start.latitude + "," + start.longitude + "&destination=" + end.latitude + "," + end.longitude + "&waypoints=optimize:true");
            for (int i = 0; i < mOrders.size(); i++) {
                googleURL.append("|" + mOrders.get(i).getLat() + "," + mOrders.get(i).getLng());
                waypoints.add(mOrders.get(i).getWaypoint());
            }
            googleURL.append("&key=AIzaSyA8P6lCZ8eRjE1Hilp94HhHyHTiG6Kk-k4");
            waypoints.add(end);
            System.out.println(googleURL.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Accept-Encoding", "identity");
            client.get(DeliveryActivity.this, googleURL.toString(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String ex, Throwable throwable) {
                    Toast.makeText(DeliveryActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(throwable.toString());
                    progress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    try {
                        JSONArray routesArray = new JSONArray(json.getJSONArray("routes").toString());
                        JSONObject routes = new JSONObject(routesArray.getJSONObject(0).toString());

                        //Polyline Segments
                        JSONArray legs = new JSONArray(routes.getJSONArray("legs").toString());
                        JSONArray steps;
                        List<List<LatLng>> latlngLegs = new ArrayList<>();
                        List<LatLng> medium;
                        for (int i = 0; i < legs.length();i++)
                        {
                            steps = new JSONArray(legs.getJSONObject(i).getJSONArray("steps").toString());
                            medium = new ArrayList<>();
                            for (int j = 0; j < steps.length();j++)
                            {
                                String this_step = steps.getJSONObject(j).getJSONObject("polyline").getString("points");
                                medium.addAll(decodePoly(this_step));
                            }
                            latlngLegs.add(medium);
                        }

                        //Delivery Legs
                        JSONArray orderArr = routes.getJSONArray("waypoint_order");
                        deliveryLegs = new Order[orderArr.length()];
                        for (int i = 0; i < deliveryLegs.length; i++)
                        {
                            //likely error
                            int selected_point = orderArr.getInt(i);
                            System.out.println(selected_point+" , "+i);
                            mOrders.get(selected_point).setLeg(latlngLegs.get(i));
                            deliveryLegs[i] = mOrders.get(selected_point);
                        }

                        //put on the big line
                        String encodedBigPolyline = routes.getJSONObject("overview_polyline").getString("points");
                        List<LatLng> bigPolyline = decodePoly(encodedBigPolyline);
                        PolylineOptions polyOptions = new PolylineOptions();
                        polyOptions.color(getResources().getColor(R.color.CyclopsLightestRed));
                        polyOptions.width(10);
                        polyOptions.addAll(bigPolyline);
                        bigMapPolyline = googleMap.addPolyline(polyOptions);

                        //add the start marker
                        MarkerOptions options = new MarkerOptions();
                        options.position(start);
                        options.title("start");
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        googleMap.addMarker(options);

                        //add the middle markers
                        for (int i = 0; i < deliveryLegs.length; i++) {
                            options = new MarkerOptions();
                            options.position(deliveryLegs[i].getWaypoint());
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            options.title(String.valueOf(deliveryLegs[i].getId()));
                            googleMap.addMarker(options);
                        }

                        //add the end marker
                        options = new MarkerOptions();
                        options.position(end);
                        options.title("end");
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        googleMap.addMarker(options);

                        for (int i = 0; i < (deliveryLegs.length + 1); i++)
                        {
                            System.out.println("polyline"+i);
                            if (i == deliveryLegs.length) {
                                PolylineOptions littlePolyOptions = new PolylineOptions();
                                littlePolyOptions.color(getResources().getColor(R.color.CyclopsDarkRed));
                                littlePolyOptions.width(10);
                                littlePolyOptions.addAll(latlngLegs.get(i));
                                littleMapPolyline = googleMap.addPolyline(littlePolyOptions);
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(waypoints.get(i)).zoom(14).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                deliveryBtnContainer.setVisibility(View.VISIBLE);
                                break;
                            }
                            else if (deliveryLegs[i].isIs_delivered() == false)
                            {
                                //put on the first little line
                                chosen_order = deliveryLegs[i].getId();
                                PolylineOptions littlePolyOptions = new PolylineOptions();
                                littlePolyOptions.color(getResources().getColor(R.color.CyclopsDarkRed));
                                littlePolyOptions.width(10);
                                littlePolyOptions.addAll(deliveryLegs[i].getLeg());
                                littleMapPolyline = googleMap.addPolyline(littlePolyOptions);
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(waypoints.get(i)).zoom(14).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                break;
                            }
                        }


                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    progress.setVisibility(View.INVISIBLE);
                }
            });
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            progress.setVisibility(View.INVISIBLE);
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
