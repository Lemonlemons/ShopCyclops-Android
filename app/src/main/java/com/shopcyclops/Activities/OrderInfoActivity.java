package com.shopcyclops.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.Adapters.OrderListAdapter;
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.Fragments.Delivery.Order;
import com.shopcyclops.Fragments.OrderList.OrderListFragment;
import com.shopcyclops.R;
import com.shopcyclops.CONSTANTS;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 9/19/2015.
 */
public class OrderInfoActivity extends FragmentActivity {

    SharedPreferences mPrefs;
    private List<CartItem> mItems;
    private List<Order> mOrders;
    CircleProgressBar mProgress;
    ListView mListView;
    private OrderListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderinfo);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        mProgress = (CircleProgressBar) findViewById(R.id.bufferingListProgress);

        // Set the adapter
        mListView = (ListView) findViewById(R.id.orderList);
        mListView.setEmptyView(findViewById(R.id.emptyList));

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int selected_order_id = mOrders.get(i).getId();
                ArrayList<CartItem> cartList = new ArrayList<CartItem>();
                for (int j = 0; j < mItems.size(); j++) {
                    if (mItems.get(j).getOrder_id() == selected_order_id) {
                        cartList.add(mItems.get(j));
                    }
                }
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.orderFrame, OrderListFragment.newInstance(cartList))
                    .addToBackStack("")
                    .commit();
            }
        });

        setupListViewAdapter();
        getOrders();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        switch (item.getItemId()) {
            case android.R.id.home:
                if (count == 0) {
                    NavUtils.navigateUpFromSameTask(this);
                    //additional code
                } else {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupListViewAdapter() {
        if (mAdapter == null) {
            mOrders = new ArrayList<>(0);
            mItems = new ArrayList<>(0);
            mAdapter = new OrderListAdapter(this, mOrders);
            mAdapter.setNotifyOnChange(false);
            mListView.setAdapter(mAdapter);
        }
    }

    private void getOrders() {
        mProgress.setVisibility(View.VISIBLE);
        mItems.clear();
        mOrders.clear();
        final SharedPreferences prefs = getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
        String user_email = prefs.getString(CONSTANTS.EMAIL_KEY, null);
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.addHeader("X-User-Token", token);
        client.addHeader("X-User-Email", user_email);
        client.get(this, CONSTANTS.BASE_URL + "/mobileusersordersanditems", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                if (json.toString() == null)
                {
                    Toast.makeText(OrderInfoActivity.this, "Not connected to the internet", Toast.LENGTH_LONG).show();
                } else {
                    System.out.println(json.toString());
                    Toast.makeText(OrderInfoActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                }
                mProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                System.out.println(throwable.toString());
                Toast.makeText(OrderInfoActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                mProgress.setVisibility(View.INVISIBLE);
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

                    for (int l = 0; l < mOrders.size(); l++) {
                        int total_Quantity = 0;
                        for (int k = 0; k < mItems.size(); k++) {
                            if (mOrders.get(l).getId() == mItems.get(k).getOrder_id()) {
                                total_Quantity = total_Quantity + mItems.get(k).getQuantity();
                            }
                        }
                        mOrders.get(l).setTotalQuantity(total_Quantity);
                    }

                    mProgress.setVisibility(View.INVISIBLE);
                    mAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        });
    }

}
