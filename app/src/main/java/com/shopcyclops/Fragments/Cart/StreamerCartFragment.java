package com.shopcyclops.Fragments.Cart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.shopcyclops.Fragments.Chat.ChatMessage;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Andrew on 7/20/2015.
 */
public class StreamerCartFragment extends android.support.v4.app.Fragment {

    ListView list;
    StreamerCartItemAdapter adapter;
    Button closeStreamBtn;
    Button checkoutBtn;
    TextView emptyText;

    public ArrayList<CartItem> CustomListViewValuesArr = new ArrayList<CartItem>();
    Pusher pusher;
    PrivateChannel channel;
    String user_email;
    int stream_id;
    String token;
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        Resources res = getResources();
        adapter=new StreamerCartItemAdapter( getActivity(), CustomListViewValuesArr, res );

        HttpAuthorizer authorizer = new HttpAuthorizer(SECRETS.PUSHER_AUTH_ENDPOINT);
        prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        token = prefs.getString(SECRETS.TOKEN_KEY, null);
        user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        stream_id = getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("X-User-Token", token);
        hmap.put("X-User-Email", user_email);
        authorizer.setHeaders(hmap);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        pusher = new Pusher(SECRETS.PUSHER_KEY, options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println(code+" : "+message);
            }
        }, ConnectionState.ALL);

        // Subscribe to a channel
        channel = pusher.subscribePrivate("private-shopping-cart-"+stream_id, new PrivateChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Subscribed to channel: " + channelName);
            }

            @Override
            public void onEvent(String channelName, String eventName, String data) {
                System.out.println("event: " + eventName + " channel: " + channelName + " Data: " + data);
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.println(message);
                System.out.println(e.toString());
                System.out.println("failure");
            }
        });

        // Bind to listen for events called "my-event" sent to "my-channel"
        channel.bind("client-new_item", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    System.out.println("New Item Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    CartItem sending = new CartItem();
                    sending.setId(json.getInt("id"));
                    sending.setItemname(json.getString("contents"));
                    sending.setPrice((float) json.getDouble("price"));
                    sending.setQuantity(json.getInt("quantity"));
                    sending.setStatus(json.getString("status"));
                    sending.setImageurl(json.getString("imageurl"));
                    sending.setProgress(json.getInt("progress"));
                    sending.setStream_id(json.getInt("stream_id"));
                    sending.setViewer_id(json.getInt("viewer_id"));
                    appendItem(sending);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.println("Received event with message: " + message);
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Received channel on: " + channelName);
            }
        });

        channel.bind("client-delete_item", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    System.out.println("Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    int selectedItem = json.getInt("id");
                    deleteItem(selectedItem);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.println("Received event with message: " + message);
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Received channel on: " + channelName);
            }
        });

        channel.bind("client-cart_item", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    System.out.println("Cart Item Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    int selectedItem = json.getInt("id");
                    int progress = json.getInt("progress");
                    String status = json.getString("status");
                    float price = (float)json.getDouble("price");
                    cartItem(selectedItem, progress, status, price);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.println("Received event with message: " + message);
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Received channel on: " + channelName);
            }
        });

        channel.bind("client-viewerpaidfor_item", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    System.out.println("Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    int selectedItem = json.getInt("id");
                    int progress = json.getInt("progress");
                    String status = json.getString("status");
                    viewerpaidforItem(selectedItem, progress, status);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.println("Received event with message: " + message);
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Received channel on: " + channelName);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_streamer_cart, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle b)
    {
        super.onActivityCreated(b);
        list = ( ListView )getView().findViewById( R.id.StreamerCartlist );  // List defined in XML ( See Below )
        closeStreamBtn = (Button)getView().findViewById(R.id.btnCloseStream);
        checkoutBtn = (Button)getView().findViewById(R.id.btnCheckout);
        final String[] timerArr = {"1 minute", "2 minute", "3 minute", "4 minute", "5 minute"};

        emptyText = (TextView) getView().findViewById(R.id.streamercartempty);
        if (CustomListViewValuesArr.size() == 0) {
            emptyText.setText("This is where your viewers will add items to your cart");
            emptyText.setVisibility(View.VISIBLE);
        }

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (prefs.getInt(SECRETS.STREAM_PROGRESS, 0) == 4) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Checkout")
                        .setMessage("By pressing agree, you claim you've checked out and paid for all items in the cart.")
                        .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            try {
                                JSONObject wrapper = new JSONObject();
                                JSONObject jsonParams = new JSONObject();
                                jsonParams.put("progress", 5);
                                wrapper.put("stream", jsonParams);
                                AsyncHttpClient client = new AsyncHttpClient();
                                PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                                client.setCookieStore(myCookieStore);
                                client.addHeader("Accept", "application/json");
                                client.addHeader("X-User-Token", token);
                                client.addHeader("X-User-Email", user_email);
                                StringEntity entity = new StringEntity(wrapper.toString());
                                client.put(getActivity(), SECRETS.BASE_URL + "/streams/" + stream_id + "/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                                        Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                                        System.out.println("failure:"+string.toString());
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                                        System.out.println("success"+json.toString());
                                    }
                                });
                            } catch (Exception e) {
                                Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                System.out.println(e.toString());
                            }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else if ((prefs.getInt(SECRETS.STREAM_PROGRESS, 0) == 2) || (prefs.getInt(SECRETS.STREAM_PROGRESS, 0) == 3)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Not yet!")
                        .setMessage("You need to close the stream off to new viewers before checking out")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else if (prefs.getInt(SECRETS.STREAM_PROGRESS, 0) == 3) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Not yet!")
                        .setMessage("You need to wait until the timer ends")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Not yet!")
                        .setMessage("You need to start the stream first!")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            }
        });

        closeStreamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefs.getInt(SECRETS.STREAM_PROGRESS, 0) == 2) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Are you sure?")
                            .setMessage("Press OK to close your stream off to new viewers and the creation of new items")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Choose how long you want to give your viewers to pay for their items:");
                                    prefs.edit().putInt(SECRETS.STREAM_PROGRESS, 3).apply();
                                    builder.setItems(timerArr, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                JSONObject wrapper = new JSONObject();
                                                JSONObject jsonParams = new JSONObject();
                                                jsonParams.put("progress", 3);
                                                jsonParams.put("timerlength", ((which+1) * 60));
                                                wrapper.put("stream", jsonParams);
                                                AsyncHttpClient client = new AsyncHttpClient();
                                                PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                                                client.setCookieStore(myCookieStore);
                                                client.addHeader("Accept", "application/json");
                                                client.addHeader("X-User-Token", token);
                                                client.addHeader("X-User-Email", user_email);
                                                StringEntity entity = new StringEntity(wrapper.toString());
                                                client.put(getActivity(), SECRETS.BASE_URL + "/streams/" + stream_id + "/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                                                        Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                                                        System.out.println("failure:"+string.toString());
                                                    }

                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                                                        System.out.println("success"+json.toString());
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                                System.out.println(e.toString());
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();

                                }
                            })
                            .setNegativeButton("Not Yet", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Start the Stream")
                            .setMessage("You to start recording before you can end the stream!")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        /**************** Create Custom Adapter *********/
        list.setAdapter( adapter );
    }

    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        CartItem tempValues = (CartItem) CustomListViewValuesArr.get(mPosition);


        // SHOW ALERT

        Toast.makeText(getActivity(),"" + tempValues.getItemname() + "Quantity:"+tempValues.getQuantity(),Toast.LENGTH_LONG).show();
    }

    private void appendItem(final CartItem m) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CustomListViewValuesArr.add(m);

                if (emptyText != null) {
                    emptyText.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteItem(int m) {
        for (int i =0; i < CustomListViewValuesArr.size(); i++) {
            if (CustomListViewValuesArr.get(i).getId() == m) {
                CustomListViewValuesArr.remove(i);
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void cartItem(int id, int progress, String status, float price) {
        CartItem selected;
        for (int i =0; i < CustomListViewValuesArr.size(); i++) {
            if (CustomListViewValuesArr.get(i).getId() == id) {
                selected = CustomListViewValuesArr.get(i);
                selected.setProgress(progress);
                selected.setStatus(status);
                selected.setPrice(price);
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void viewerpaidforItem(int id, int progress, String status) {
        CartItem selected;
        for (int i =0; i < CustomListViewValuesArr.size(); i++) {
            if (CustomListViewValuesArr.get(i).getId() == id) {
                selected = CustomListViewValuesArr.get(i);
                selected.setProgress(progress);
                selected.setStatus(status);
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
