package com.shopcyclops.Fragments.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.R;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andrew on 9/4/2015.
 */
public class ViewerChatFragment extends android.support.v4.app.Fragment {

    Pusher pusher;
    PrivateChannel channel;
    EditText messagetext;
    Button sendButton;

    private ChatMessagesListAdapter adapter;
    private List<ChatMessage> listMessages;
    private ListView listViewMessages;
    private TextView emptyText;

    String user_email;
    String token;
    int user_id;
    int stream_id;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        listMessages = new ArrayList<ChatMessage>();
        adapter = new ChatMessagesListAdapter(getActivity(), listMessages);

        HttpAuthorizer authorizer = new HttpAuthorizer(CONSTANTS.PUSHER_AUTH_ENDPOINT);
        SharedPreferences prefs = getActivity().getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
        user_email = prefs.getString(CONSTANTS.EMAIL_KEY, null);
        user_id = prefs.getInt(CONSTANTS.USER_ID_KEY, 0);
        stream_id = getActivity().getIntent().getIntExtra(CONSTANTS.CURRENT_STREAM_ID, 0);
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("X-User-Token", token);
        hmap.put("X-User-Email", user_email);
        authorizer.setHeaders(hmap);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        pusher = new Pusher(CONSTANTS.PUSHER_KEY, options);

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
        channel = pusher.subscribePrivate("private-chat-"+stream_id, new PrivateChannelEventListener() {
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
        channel.bind("client-new_message", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    System.out.println("Received event with data: " + data);
                    JSONObject json = new JSONObject(data);
                    ChatMessage sending = new ChatMessage();
                    sending.setMessage(json.getString("contents"));
                    sending.setFromName(json.getString("username"));
                    if (json.getInt("user_id") == user_id) {
                        sending.setSelf(true);
                    }
                    else {
                        sending.setSelf(false);
                    }
                    appendMessage(sending);
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

        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.addHeader("X-User-Token", token);
        client.addHeader("X-User-Email", user_email);
        client.get(getActivity(), CONSTANTS.BASE_URL+"/streams/"+stream_id+"/mobileassociatedmessages", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                System.out.println(throwable.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                try {
                    System.out.println(json.toString());
                    for (int i = 0; i < json.length(); i++) {
                        ChatMessage transfer = new ChatMessage();
                        JSONObject opper = json.getJSONObject(i);
                        transfer.setFromName(opper.getString("username"));
                        transfer.setMessage(opper.getString("contents"));
                        if (opper.getInt("user_id") == user_id) {
                            transfer.setSelf(true);
                        }
                        else {
                            transfer.setSelf(false);
                        }
                        appendMessage(transfer);
                    }
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        sendButton = (Button) getView().findViewById(R.id.btnSend);
        messagetext = (EditText) getView().findViewById(R.id.inputMsg);
        listViewMessages = (ListView) getView().findViewById(R.id.list_view_messages);
        emptyText = (TextView) getView().findViewById(R.id.chatempty);
        if (listMessages.size() == 0) {
            emptyText.setText("Use the Chat to talk to the streamer!");
            emptyText.setVisibility(View.VISIBLE);
        }

        listViewMessages.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    JSONObject wrapper = new JSONObject();
                    JSONObject jObject = new JSONObject();
                    final ChatMessage sending = new ChatMessage(user_email, messagetext.getText().toString(), true);
                    jObject.put("username", sending.getFromName());
                    jObject.put("contents", sending.getMessage());
                    jObject.put("stream_id", stream_id);
                    jObject.put("user_id", user_id);
                    wrapper.put("message", jObject);
                    AsyncHttpClient client = new AsyncHttpClient();
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                    client.setCookieStore(myCookieStore);
                    client.addHeader("Accept", "application/json");
                    client.addHeader("X-User-Token", token);
                    client.addHeader("X-User-Email", user_email);
                    StringEntity entity = new StringEntity(wrapper.toString());
                    client.post(getActivity(), CONSTANTS.BASE_URL + "/messages", entity, "application/json", new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                            System.out.println(throwable.toString());
                            try {
                                if (messagetext != null) {
                                    messagetext.setText("");
                                }
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            System.out.println(json.toString());
                            try {
                                if (messagetext != null) {
                                    messagetext.setText("");
                                }
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }
                        }
                    });
                }
                catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        messagetext.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        pusher.disconnect();
    }

    private void appendMessage(final ChatMessage m) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                listMessages.add(m);

                if (emptyText != null) {
                    emptyText.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

}
