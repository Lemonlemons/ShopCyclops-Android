package com.shopcyclops.Fragments.Stream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.shopcyclops.Activities.DeliveryActivity;
import com.shopcyclops.Activities.ViewerActivity;
import com.shopcyclops.Adapters.StreamGridAdapter;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew on 9/26/2015.
 */
public class StreamGridFragment extends Fragment implements AbsListView.OnItemClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private List<Stream> mStreams;
    private ProgressBar GPSspinner;
    private TextView GPSstatus;
    private CircleProgressBar buffer;

    Button mapBtn;

    /**
     * The fragment's ListView/GridView.
     *///
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private StreamGridAdapter mAdapter;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StreamGridFragment newInstance(int sectionNumber) {
        StreamGridFragment fragment = new StreamGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public StreamGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.grid_o_streams, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        //GPSspinner = (ProgressBar) getActivity().findViewById(R.id.GPSspinner);
        //GPSstatus = (TextView) getActivity().findViewById(R.id.GPSstatus);
        buffer = (CircleProgressBar) getActivity().findViewById(R.id.bufferingProgress);

        // Set the adapter
        mListView = (AbsListView) getActivity().findViewById(android.R.id.list);
        mListView.setEmptyView(getActivity().findViewById(android.R.id.empty));

        mapBtn = (Button)getActivity().findViewById(R.id.btnMap);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        setupListViewAdapter();
        getStreams();

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DeliveryActivity.class);
                i.putExtra(CONSTANTS.CURRENT_STREAM_ID, 148);
                i.putExtra(CONSTANTS.CURRENT_STREAM_HOME_POINT_LAT, (double)46.854371);
                i.putExtra(CONSTANTS.CURRENT_STREAM_HOME_POINT_LNG, (double)-96.852111);
                startActivity(i);
            }
        });
    }

    public void refreshlist()
    {
        getStreams();
    }

    private void getStreams() {
        if (isNetworkAvailable())
        {
            mStreams.clear();
            mAdapter.notifyDataSetChanged();
            final SharedPreferences prefs = getActivity().getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
            String user_email = prefs.getString(CONSTANTS.EMAIL_KEY, null);
            buffer.setVisibility(View.VISIBLE);
            if (token == null && user_email == null) {
                System.out.println("guest");
                guestindex(prefs);
            } else {
                System.out.println("user");
                userindex(token, user_email, prefs);
            }
        }
        else
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle("No Internet")
                    .setMessage("Please connect to the internet to view streams")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void userindex(String token, String user_email, SharedPreferences prefs) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.addHeader("X-User-Token", token);
        client.addHeader("X-User-Email", user_email);
        client.get(getActivity(), CONSTANTS.BASE_URL+"/mobileuserindex?lat="+prefs.getFloat(CONSTANTS.CURRENT_DELIVERY_LAT, 0)+"&lng="+prefs.getFloat(CONSTANTS.CURRENT_DELIVERY_LNG, 0), commonhandler);
    }

    private void guestindex(SharedPreferences prefs) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.get(getActivity(), CONSTANTS.BASE_URL+"/mobileuserindex?lat="+prefs.getFloat(CONSTANTS.CURRENT_DELIVERY_LAT, 0)+"&lng="+prefs.getFloat(CONSTANTS.CURRENT_DELIVERY_LNG, 0), commonhandler);
    }

    private JsonHttpResponseHandler commonhandler = new JsonHttpResponseHandler() {
        @Override
        public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_LONG).show();
            System.out.println(error.toString());

            buffer.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
            try {
                System.out.println(json.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject opper = json.getJSONObject(i);
                    Stream transfer = new Stream();
                    transfer.setStreamId(opper.getInt("id"));
                    transfer.setOwnerId(opper.getInt("host_user_id"));
                    transfer.setLatitude(opper.getDouble("lat"));
                    transfer.setLongitude(opper.getDouble("lng"));
                    transfer.setDescription(opper.getString("description"));
                    transfer.setThumbnailUrl(opper.getString("thumbnail_url"));
                    transfer.setTitle(opper.getString("name"));
                    transfer.setStore(opper.getString("store"));
                    System.out.println(json.get(i).toString());
                    mStreams.add(transfer);
                    mAdapter.notifyDataSetChanged();
                }
                buffer.setVisibility(View.INVISIBLE);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
                buffer.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Stream stream = mAdapter.getItem(position);
        final SharedPreferences prefs = getActivity().getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final String token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
        String user_email = prefs.getString(CONSTANTS.EMAIL_KEY, null);
        boolean creditcheck = prefs.getBoolean(CONSTANTS.CREDIT_CHECK, false);
        prefs.edit().putInt(CONSTANTS.STREAM_PROGRESS, 2).apply();
        if (creditcheck) {
            gotostream(true, stream, token);
        }
        else {
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            client.get(getActivity(), CONSTANTS.BASE_URL+"/mobilecardsindex", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(json.toString());
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    try {
                        JSONArray data = json.getJSONArray("data");
                        System.out.println("data: "+data.toString());
                        if (data.length() > 0) {
                            prefs.edit().putBoolean(CONSTANTS.CREDIT_CHECK, true).apply();
                            gotostream(true, stream, token);
                        }
                        else {
                            prefs.edit().putBoolean(CONSTANTS.CREDIT_CHECK, false).apply();
                            gotostream(false, stream, token);
                        }
                    }
                    catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            });
        }
    }

    private void gotostream(boolean credit, Stream stream, String token) {
        System.out.println(4);
        final Intent i = new Intent(getActivity(), ViewerActivity.class);
        i.putExtra(CONSTANTS.CURRENT_STREAM_ID, stream.getStreamId());
        i.putExtra(CONSTANTS.CURRENT_STREAM_TITLE, stream.getTitle());
        i.putExtra(CONSTANTS.CURRENT_STREAM_DESCRIPTION, stream.getDescription());
        i.putExtra(CONSTANTS.CURRENT_STREAM_STORE, stream.getStore());
        i.putExtra(CONSTANTS.INTENT_CREDIT_CHECK, credit);
        if (token == null) {
            i.putExtra("LEVEL", 1);
        } else {
            i.putExtra("LEVEL", 2);
        }
        startActivity(i);
    }

    private void setupListViewAdapter() {
        if (mAdapter == null) {
            mStreams = new ArrayList<>(0);
            mAdapter = new StreamGridAdapter(getActivity(), mStreams);
            displayStreams(mStreams, false);
            mAdapter.setNotifyOnChange(false);
            mListView.setAdapter(mAdapter);
        }
    }

    private void displayStreams(List<Stream> streams, boolean append) {
        if (append) {
            mStreams.addAll(streams);
        } else {
            mStreams = streams;
        }
        Collections.sort(mStreams);
        mAdapter.refresh(mListView, mStreams);
        if (mStreams.size() == 0) {
            View emptyView = mListView.getEmptyView().findViewById(R.id.emptytext);

            if (emptyView instanceof TextView) {
                ((TextView) emptyView).setText("Use the search above to find broadcasts near you!");
            }
        }
    }
}
