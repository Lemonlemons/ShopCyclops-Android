package com.shopcyclops.Fragments.Delivery;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.Activities.ViewerActivity;
import com.shopcyclops.Adapters.DeliveryListAdapter;
import com.shopcyclops.Adapters.StreamListAdapter;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew on 9/27/2015.
 */
public class DeliveryListFragment extends Fragment implements ListView.OnItemClickListener {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private List<CartItem> mItems;
    private CircleProgressBar buffer;
    private ListView mListView;
    private DeliveryListAdapter mAdapter;
    private List<CartItem> cartItems;
    private Button deliveryBtn;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static DeliveryListFragment newInstance(ArrayList<CartItem> Clist) {
        DeliveryListFragment fragment = new DeliveryListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("cart_array", Clist);
        fragment.setArguments(args);
        return fragment;
    }

    public DeliveryListFragment() {

    }

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        cartItems = getArguments().getParcelableArrayList("cart_array");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_delivery_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        //GPSspinner = (ProgressBar) getActivity().findViewById(R.id.GPSspinner);
        //GPSstatus = (TextView) getActivity().findViewById(R.id.GPSstatus);
        buffer = (CircleProgressBar) getActivity().findViewById(R.id.bufferingDeliveryListProgress);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set the adapter
        mListView = (ListView) getActivity().findViewById(R.id.itemList);
        mListView.setEmptyView(getActivity().findViewById(R.id.emptyItemList));

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        setupListViewAdapter();

        deliveryBtn = (Button)getActivity().findViewById(R.id.btnCompleteDelivery);
        deliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ((OnDeliveryInterface) getActivity()).switchFragment(mItems.get(0).getOrder_id());
                }catch (ClassCastException cce){
                    System.out.println(cce.toString());
                }
            }
        });

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        addItems(cartItems);
    }

    public interface OnDeliveryInterface{
        public void onCompleted(int position);
        public void switchFragment(int order_id);
    }

    private void setupListViewAdapter() {
        mItems = new ArrayList<>(0);
        mAdapter = new DeliveryListAdapter(getActivity(), mItems);
        displayItems(mItems, false);
        mAdapter.setNotifyOnChange(false);
        mListView.setAdapter(mAdapter);
    }

    private void displayItems(List<CartItem> items, boolean append) {
        if (append) {
            mItems.addAll(items);
        } else {
            mItems = items;
        }
//        Collections.sort(mItems);
        mAdapter.refresh(mListView, mItems);
        if (mItems.size() == 0) {
            View emptyView = mListView.getEmptyView().findViewById(R.id.emptytext);

            if (emptyView instanceof TextView) {
                ((TextView) emptyView).setText("Finding the Items!");
            }
        }
    }

    public void addItems(List<CartItem> cartItems) {
        mItems.addAll(cartItems);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }
}
