package com.shopcyclops.Fragments.Subscribe;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.view.R5VideoView;
import com.red5pro.streaming.view.RendererWrapper;
import com.shopcyclops.Activities.DeliveryActivity;
import com.shopcyclops.Activities.StreamEndedActivity;
import com.shopcyclops.Fragments.Broadcast.AppState;
import com.shopcyclops.Fragments.Broadcast.PublishStreamConfig;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Andrew on 8/31/2015.
 */
public class SubscribeFragment extends android.support.v4.app.Fragment {

    R5Stream stream;
    R5VideoView videoView;

    float lat;
    float lng;
    int user_id;
    String creditcode;

    public boolean isStreaming = false;


    public final static String TAG = "Subscribe";

    public void onStateSelection(AppState state) {
        getActivity().finish();
    }

    public String getStringResource(int id) {
        return getResources().getString(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_subscribe, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        startStream();
        final TextView countdown = (TextView) getActivity().findViewById(R.id.countdown);
        final TextView snapshotText = (TextView) getActivity().findViewById(R.id.snapshottext);

        HttpAuthorizer authorizer = new HttpAuthorizer(SECRETS.PUSHER_AUTH_ENDPOINT);
        final SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final String token = prefs.getString(SECRETS.TOKEN_KEY, null);
        final String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        final int stream_id = getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
        lat = prefs.getFloat(SECRETS.CURRENT_DELIVERY_LAT, 0);
        lng = prefs.getFloat(SECRETS.CURRENT_DELIVERY_LNG, 0);
        user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);
        creditcode = prefs.getString(SECRETS.CURRENT_CARDCODE, "");
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("X-User-Token", token);
        hmap.put("X-User-Email", user_email);
        authorizer.setHeaders(hmap);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        Pusher pusher = new Pusher(SECRETS.PUSHER_KEY, options);

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
        PrivateChannel channel = pusher.subscribePrivate("private-broadcast-"+stream_id, new PrivateChannelEventListener() {
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
        channel.bind("client-timerstart", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    JSONObject json = new JSONObject(data);
                    prefs.edit().putInt(SECRETS.STREAM_PROGRESS, 3).apply();
                    final int milliseconds = (json.getInt("timerlength")*1000);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            snapshotText.setText("Time until checkout:");
                            snapshotText.setVisibility(View.VISIBLE);
                            countdown.setVisibility(View.VISIBLE);
                            new CountDownTimer(milliseconds, 1000) {
                                int minutes;
                                int seconds;
                                String stringsecs;

                                @Override
                                public void onTick(long l) {
                                    minutes = ((int)l) / (60 * 1000);
                                    seconds = (((int)l) / 1000) % 60;
                                    stringsecs = String.format("%d:%02d", minutes, seconds);
                                    countdown.setText(stringsecs);
                                }

                                @Override
                                public void onFinish() {
                                    snapshotText.setText("The streamer is now checking out");
                                    countdown.setVisibility(View.GONE);
                                    try {
                                        JSONObject wrapper = new JSONObject();
                                        JSONObject jsonParams = new JSONObject();
                                        jsonParams.put("lat", lat);
                                        jsonParams.put("lng", lng);
                                        jsonParams.put("viewer_id", user_id);
                                        jsonParams.put("stream_id", stream_id);
                                        jsonParams.put("cardcode", creditcode);
                                        wrapper.put("order", jsonParams);
                                        AsyncHttpClient client = new AsyncHttpClient();
                                        PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                                        client.setCookieStore(myCookieStore);
                                        client.addHeader("Accept", "application/json");
                                        client.addHeader("X-User-Token", token);
                                        client.addHeader("X-User-Email", user_email);
                                        StringEntity entity = new StringEntity(wrapper.toString());
                                        client.post(getActivity(), SECRETS.BASE_URL + "/orders", entity, "application/json", new JsonHttpResponseHandler() {
                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                                                Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                                                System.out.println(json.toString());
                                            }

                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                                                System.out.println(json.toString());
                                            }
                                        });
                                    }
                                    catch (Exception e)
                                    {
                                        Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                        System.out.println(e.toString());
                                    }
                                }
                            }.start();
                        }
                    });
                }
                catch (Exception e) {
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

        channel.bind("client-endstream", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, final String data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getActivity(), StreamEndedActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    }
                });
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

    private void toggleStream() {
        if(isStreaming) {
            stopStream();
        }
        else {
            startStream();
        }
    }

    private void setStreaming(boolean ok) {
        //ViewGroup playPauseButton = (ViewGroup) getActivity().findViewById(R.id.btnSubscribePlayPause);
        //TextView textView = (TextView) playPauseButton.getChildAt(0);

        isStreaming = ok;
    }

    private void startStream() {

        //grab the main view where our video object resides
        View v = getActivity().findViewById(android.R.id.content);

        v.setKeepScreenOn(true);

        //setup the stream with the user config settings
        stream = new R5Stream(new R5Connection(new R5Configuration(R5StreamProtocol.RTSP, SECRETS.RED_HOST, SECRETS.RED_PORT, SECRETS.RED_APP_NAME, 1.0f)));

        //set log level to be informative
        stream.setLogLevel(R5Stream.LOG_LEVEL_INFO);

        //set up our listener
        stream.setListener(new R5ConnectionListener() {
            @Override
            public void onConnectionEvent(R5ConnectionEvent r5event) {
                //this is getting called from the network thread, so handle appropriately
                final R5ConnectionEvent event = r5event;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getActivity().getApplicationContext();
                        CharSequence text = event.message;
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        if (event.message.equals("DISCONNECTED")) {
                            Intent i = new Intent(getActivity(), StreamEndedActivity.class);
                            startActivity(i);
                            getActivity().finish();
                        }
                    }
                });
            }
        });

        //associate the video object with the red5 SDK video view
        videoView = (R5VideoView)v.findViewById(R.id.video);

        ImageButton ib = (ImageButton) v.findViewById(R.id.imageCapture);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(1);
                TakeScreenshot();
            }
        });

        //View snapshot = getActivity().findViewById(R.id.imageCapture);

        //attach the stream
        videoView.attachStream(stream);

        //start the stream
        String appcode = getActivity().getIntent().getStringExtra(SECRETS.CURRENT_STREAM_TITLE) + getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
        stream.play(appcode);
        //update the state for the toggle button
        setStreaming(true);

    }

    public void TakeScreenshot(){    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
        Random num = new Random();
        int nu=num.nextInt(1000); //PRODUCING A RANDOM NUMBER FOR FILE NAME
        videoView.setDrawingCacheEnabled(true); //CamView OR THE NAME OF YOUR LAYOUR
        videoView.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createBitmap(videoView.getDrawingCache());
        videoView.setDrawingCacheEnabled(false); // clear drawing cache
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

        String picId=String.valueOf(nu);
        String myfile="Ghost"+picId+".jpeg";

        File dir_image = new  File(Environment.getExternalStorageDirectory()+//<---
                File.separator+"Ultimate Entity Detector");          //<---
        dir_image.mkdirs();             //<---
        //^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
        //THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"

        try {
            File tmpFile = new File(dir_image,myfile);
            FileOutputStream fos = new FileOutputStream(tmpFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fis.close();
            fos.close();
            Toast.makeText(getActivity(),"The file is saved at :SD/Ultimate Entity Detector",Toast.LENGTH_LONG).show();
//            bmp1 = null;
//            camera_image.setImageBitmap(bmp1); //RESETING THE PREVIEW
//            camera.startPreview();             //RESETING THE PREVIEW
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopStream() {

        if(stream != null) {
            View v = getActivity().findViewById(android.R.id.content);
            R5VideoView videoView = (R5VideoView)v.findViewById(R.id.video);
            videoView.attachStream(null);
            stream.stop();

            stream = null;
        }
        setStreaming(false);

    }

//    private void openSettings() {
//        try {
//            DialogFragment newFragment = SettingsDialogFragment.newInstance(AppState.SUBSCRIBE);
//            newFragment.show(getFragmentManager().beginTransaction(), "settings_dialog");
//        }
//        catch(Exception e) {
//            Log.i(TAG, "Can't open settings: " + e.getMessage());
//        }
//    }

    @Override
    public void onPause() {
        stopStream();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        stopStream();
        super.onDestroy();
    }
}
