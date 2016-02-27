package com.shopcyclops.Fragments.Cart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.shopcyclops.Activities.PaymentInfoActivity;
import com.shopcyclops.Activities.StreamMainActivity;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Andrew on 9/4/2015.
 */
public class ViewerCartFragment extends android.support.v4.app.Fragment {

    ListView list;
    ViewerCartItemAdapter adapter;
    public ArrayList<CartItem> CustomListViewValuesArr = new ArrayList<CartItem>();

    Pusher pusher;
    PrivateChannel channel;
    EditText itemtext;
    Button submitButton;
    Spinner quantityChooser;
    TextView emptyText;
    Button leaveButton;
    Button purchaseAll;

    String user_email;
    int stream_id;
    int user_id;
    String token;
    List<String> cards;
    List<String> codes;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        Resources res = getResources();
        adapter=new ViewerCartItemAdapter( getActivity(), CustomListViewValuesArr, res );

        HttpAuthorizer authorizer = new HttpAuthorizer(SECRETS.PUSHER_AUTH_ENDPOINT);
        prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        token = prefs.getString(SECRETS.TOKEN_KEY, null);
        user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);
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
                    System.out.println("Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    CartItem sending = new CartItem();
                    sending.setId(json.getInt("id"));
                    sending.setItemname(json.getString("contents"));
                    sending.setPrice((float)json.getDouble("price"));
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
                    System.out.println("Received event with data: " + data);
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
        View rootView = inflater.inflate(R.layout.fragment_viewer_cart, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle b)
    {
        super.onActivityCreated(b);
        list = ( ListView )getView().findViewById( R.id.ViewerCartlist );  // List defined in XML ( See Below )

        cards = new ArrayList<String>();
        codes = new ArrayList<String>();

        submitButton = (Button) getView().findViewById(R.id.btnSubmit);
        itemtext = (EditText) getView().findViewById(R.id.itemMsg);
        leaveButton = (Button) getView().findViewById(R.id.btnCancel);
        purchaseAll = (Button) getView().findViewById(R.id.btnPurchase);

        emptyText = (TextView) getView().findViewById(R.id.cartempty);
        if (CustomListViewValuesArr.size() == 0) {
            emptyText.setText("Create items that you want added to the cart");
            emptyText.setVisibility(View.VISIBLE);
        }
        //
        final boolean creditok = prefs.getBoolean(SECRETS.CREDIT_CHECK, false);

        quantityChooser = (Spinner) getView().findViewById(R.id.quantitySpinner);
        ArrayAdapter<CharSequence> quantityadapter = ArrayAdapter.createFromResource(getActivity(), R.array.quantity_array, android.R.layout.simple_spinner_item);
        quantityadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantityChooser.setAdapter(quantityadapter);


//        /**************** Create Custom Adapter *********/
//        list.setAdapter( adapter );

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (prefs.getInt(SECRETS.STREAM_PROGRESS, 1) == 2) {
                    if (creditok) {
                        try {
                            final CartItem sending = new CartItem();
                            sending.setItemname(itemtext.getText().toString());
                            sending.setQuantity(Integer.parseInt(quantityChooser.getSelectedItem().toString()));
                            sending.setStatus("On Shopping List");
                            sending.setProgress(1);
                            sending.setStream_id(stream_id);
                            sending.setViewer_id(user_id);
                            JSONObject wrapper = new JSONObject();
                            JSONObject jObject = new JSONObject();
                            jObject.put("contents", sending.getItemname());
                            jObject.put("price", sending.getPrice());
                            jObject.put("quantity", sending.getQuantity());
                            jObject.put("status", sending.getStatus());
                            jObject.put("imageurl", sending.getImageurl());
                            jObject.put("progress", sending.getProgress());
                            jObject.put("stream_id", sending.getStream_id());
                            jObject.put("viewer_id", sending.getViewer_id());
                            wrapper.put("item", jObject);
                            AsyncHttpClient client = new AsyncHttpClient();
                            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                            client.setCookieStore(myCookieStore);
                            client.addHeader("Accept", "application/json");
                            client.addHeader("X-User-Token", token);
                            client.addHeader("X-User-Email", user_email);
                            StringEntity entity = new StringEntity(wrapper.toString());
                            client.post(getActivity(), SECRETS.BASE_URL + "/items", entity, "application/json", new JsonHttpResponseHandler() {
                                @Override
                                public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                                    System.out.println(throwable.toString());
                                    try {
                                        if (itemtext != null) {
                                            itemtext.setText("");
                                            quantityChooser.setSelection(0);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.toString());
                                    }
                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                                    System.out.println(json.toString());
                                    try {
                                        if (itemtext != null) {
                                            itemtext.setText("");
                                            quantityChooser.setSelection(0);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.toString());
                                    }
                                }
                            });
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("How are you going to pay for that?")
                                .setMessage("You need to add a credit card to your account before we can let you purchase items. Would you like to leave this stream and be redirected to your payment profile?")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getActivity(), PaymentInfoActivity.class);
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
                else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Stream Ending Soon")
                            .setMessage("The streamer has stopped accepting new items and after the timer ends the streamer will checkout the cart.")
                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        leaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("Are you sure you want to leave?")
                    .setMessage("All items you've added to the cart will be removed.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AsyncHttpClient client = new AsyncHttpClient();
                            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                            client.setCookieStore(myCookieStore);
                            client.addHeader("Accept", "application/json");
                            client.addHeader("X-User-Token", token);
                            client.addHeader("X-User-Email", user_email);
                            for (int i = 0; i < CustomListViewValuesArr.size(); i++) {
                                if (CustomListViewValuesArr.get(i).getViewer_id() == user_id) {
                                    try {

                                        client.delete(getActivity(), SECRETS.BASE_URL + "/items/" + CustomListViewValuesArr.get(i).getId(), new JsonHttpResponseHandler() {
                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                                                System.out.println(throwable.toString());
                                            }

                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                                                System.out.println(json.toString());
                                            }
                                        });
                                    }
                                    catch (Exception e) {
                                        System.out.println(e.toString());
                                    }
                                }
                            }
                            Intent i = new Intent(getActivity(), StreamMainActivity.class);
                            startActivity(i);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        });

        purchaseAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Are you ready to purchase all your items?")
                        .setMessage("After paying for the items you will not be able to remove them from the cart.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getCards();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        /**************** Create Custom Adapter *********/
        list.setAdapter( adapter );
    }

    public void getCards() {
        try {
            final SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String token = prefs.getString(SECRETS.TOKEN_KEY, null);
            String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            client.get(getActivity(), SECRETS.BASE_URL+"/mobilecardsindex", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(json.toString());
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    try {
                        JSONArray data = json.getJSONArray("data");
                        System.out.println(data.toString());
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject opper = data.getJSONObject(i);
                                String code = opper.getString("id");
                                String ello = opper.getString("brand")+" **** **** **** "+opper.getString("last4")+"   EXP: "
                                        +opper.getString("exp_month")+"/"+opper.getString("exp_year");
                                System.out.println(ello);
                                cards.add(ello);
                                System.out.println(code);
                                codes.add(code);
                            }
                            final String[] cardsArr = cards.toArray(new String[cards.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Choose the card you want to use to pay for the items");
                            builder.setItems(cardsArr, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    chooseCards(codes.get(which));
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.show();
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                    catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            });
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void chooseCards(String code) {
        try {
            prefs.edit().putString(SECRETS.CURRENT_CARDCODE, code).commit();
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            JSONObject wrapper = new JSONObject();
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("status", "Paid For");
            jsonParams.put("progress", 3);
            wrapper.put("item", jsonParams);
            StringEntity entity = new StringEntity(wrapper.toString());
            for (int i = 0; i < CustomListViewValuesArr.size(); i++) {
                if ((CustomListViewValuesArr.get(i).getViewer_id() == user_id) && (CustomListViewValuesArr.get(i).getProgress() == 2)) {
                    try {
                        client.put(getActivity(), SECRETS.BASE_URL + "/items/" + CustomListViewValuesArr.get(i).getId(), entity, "application/json", new JsonHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                                System.out.println(throwable.toString());
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                                System.out.println(json.toString());
                            }
                        });
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void onDestroy() {
//        for (int i = 0; i < CustomListViewValuesArr.size(); i++) {
//            if (CustomListViewValuesArr.get(i).getViewerId() == user_id) {
//                try {
//                    AsyncHttpClient client = new AsyncHttpClient();
//                    PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
//                    client.setCookieStore(myCookieStore);
//                    client.addHeader("Accept", "application/json");
//                    client.addHeader("X-User-Token", token);
//                    client.addHeader("X-User-Email", user_email);
//                    client.delete(getActivity(), SECRETS.BASE_URL + "/items/" + CustomListViewValuesArr.get(i).getId(), new JsonHttpResponseHandler() {
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
//                            System.out.println(throwable.toString());
//                        }
//
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
//                            System.out.println(json.toString());
//                        }
//                    });
//                }
//                catch (Exception e) {
//                    System.out.println(e.toString());
//                }
//            }
//        }
        super.onDestroy();
    }


    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        CartItem tempValues = (CartItem) CustomListViewValuesArr.get(mPosition);


        // SHOW ALERT

        Toast.makeText(getActivity(), "" + tempValues.getItemname() + "Quantity:" + tempValues.getQuantity(), Toast.LENGTH_LONG).show();
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

    private void deleteItem(final int m) {
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
