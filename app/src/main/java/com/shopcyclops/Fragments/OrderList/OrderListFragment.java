package com.shopcyclops.Fragments.OrderList;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.Adapters.DeliveryListAdapter;
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 10/24/2015.
 */
public class OrderListFragment extends Fragment implements ListView.OnItemClickListener {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private List<CartItem> mItems;
    private CircleProgressBar buffer;
    private ListView mListView;
    private DeliveryListAdapter mAdapter;
    private List<CartItem> cartItems;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static OrderListFragment newInstance(ArrayList<CartItem> Clist) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("cart_array", Clist);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderListFragment() {

    }

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        cartItems = getArguments().getParcelableArrayList("cart_array");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        //GPSspinner = (ProgressBar) getActivity().findViewById(R.id.GPSspinner);
        //GPSstatus = (TextView) getActivity().findViewById(R.id.GPSstatus);
        buffer = (CircleProgressBar) getActivity().findViewById(R.id.bufferingOrderListProgress);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set the adapter
        mListView = (ListView) getActivity().findViewById(R.id.itemList);
        mListView.setEmptyView(getActivity().findViewById(R.id.emptyItemList));

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        setupListViewAdapter();

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        addItems(cartItems);
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
