package com.shopcyclops.Fragments.Broadcast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
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
import com.shopcyclops.Activities.DeliveryActivity;
import com.shopcyclops.Activities.LoginActivity;
import com.shopcyclops.Fragments.Chat.ChatMessage;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.source.R5Camera;
import com.red5pro.streaming.source.R5Microphone;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by Andrew on 7/14/2015.
 */
public class BroadcastFragment extends android.support.v4.app.Fragment implements SurfaceHolder.Callback, View.OnClickListener {

    private int cameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    private List<Camera.Size> sizes = new ArrayList<Camera.Size>();
    public static Camera.Size selected_size = null;
    public static String selected_item = null;
    public static int preferedResolution = 0;
    public static PublishStreamConfig config = null;
    private R5Camera r5Cam;
    private R5Microphone r5Mic;
    private SurfaceView surfaceForCamera;
    File pictureFile;
    int stream_id;
    TextView countdown;
    TextView snapshotText;


    static {
        if(config==null){
            config = new PublishStreamConfig();
        }
    }

    protected Camera camera;
    protected boolean isPublishing = false;

    R5Stream stream;
    public R5Configuration configuration;

    public final static String TAG = "Preview";

    public void onStateSelection(AppState state) {
        getActivity().finish();
    }

    public void onSettingsClick() {
        //openSettings();
    }

    public String getStringResource(int id) {
        return getResources().getString(id);
    }

    //grab user data to be used in R5Configuration

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //assign the layout view
        View rootView = inflater.inflate(R.layout.fragment_broadcast, container, false);

        //setup properties in configuration

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View v = getActivity().findViewById(android.R.id.content);

        v.setKeepScreenOn(true);

        //activate the camera//
        showCamera();

        ImageButton rButton = (ImageButton) getActivity().findViewById(R.id.recordButton);
        rButton.setOnClickListener(this);

        ImageButton cameraButton = (ImageButton) getActivity().findViewById(R.id.cameraFlipper);
        cameraButton.setOnClickListener(this);

        countdown = (TextView) getActivity().findViewById(R.id.countdown);
        snapshotText = (TextView) getActivity().findViewById(R.id.snapshottext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration = new R5Configuration(R5StreamProtocol.RTSP, SECRETS.RED_HOST, SECRETS.RED_PORT, SECRETS.RED_APP_NAME, 1f);
        stream_id = getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);

        HttpAuthorizer authorizer = new HttpAuthorizer(SECRETS.PUSHER_AUTH_ENDPOINT);
        final SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final String token = prefs.getString(SECRETS.TOKEN_KEY, null);
        final String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        stream_id = getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
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
            public void onEvent(String channel, String event, final String data) {
                try {
                    JSONObject json = new JSONObject(data);
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
                                    snapshotText.setText("You are now cleared to checkout.");
                                    countdown.setVisibility(View.GONE);
                                    prefs.edit().putInt(SECRETS.STREAM_PROGRESS, 4).apply();
                                    try {
                                        JSONObject wrapper = new JSONObject();
                                        JSONObject jsonParams = new JSONObject();
                                        jsonParams.put("progress", 4);
                                        wrapper.put("stream", jsonParams);
                                        AsyncHttpClient client = new AsyncHttpClient();
                                        PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                                        client.setCookieStore(myCookieStore);
                                        client.addHeader("Accept", "application/json");
                                        client.addHeader("X-User-Token", token);
                                        client.addHeader("X-User-Email", user_email);
                                        StringEntity entity = new StringEntity(wrapper.toString());
                                        client.put(getActivity(), SECRETS.BASE_URL + "/streams/"+stream_id+"/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
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

        // Bind to listen for events called "my-event" sent to "my-channel"
        channel.bind("client-endstream", new PrivateChannelEventListener() {
            @Override
            public void onEvent(String channel, String event, final String data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getActivity(), DeliveryActivity.class);
                        i.putExtra(SECRETS.CURRENT_STREAM_ID, stream_id);
                        i.putExtra(SECRETS.CURRENT_STREAM_HOME_POINT_LAT, (double)prefs.getFloat(SECRETS.CURRENT_DELIVERY_LAT, 0));
                        i.putExtra(SECRETS.CURRENT_STREAM_HOME_POINT_LNG, (double)prefs.getFloat(SECRETS.CURRENT_DELIVERY_LNG, 0));
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

    @Override
    public void onResume() {
        super.onResume();
        //openSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopPublishing();
        stopCamera();
        super.onDestroy();
    }

    private void toggleCamera() {
        cameraSelection = (cameraSelection + 1) % 2;
        try {
            Camera.getCameraInfo(cameraSelection, cameraInfo);
            cameraSelection = cameraInfo.facing;
        }
        catch(Exception e) {
            // can't find camera at that index, set default
            cameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopCamera();
        showCamera();
    } //

    private void showCamera() {
        if(camera == null) {
            camera = Camera.open(cameraSelection);
            camera.setDisplayOrientation(0);
            sizes = camera.getParameters().getSupportedPreviewSizes();
            SurfaceView sufi = (SurfaceView) getActivity().findViewById(R.id.surfaceView);
            if(sufi.getHolder().isCreating()) {
                sufi.getHolder().addCallback(this);
            }
            else {
                sufi.getHolder().addCallback(this);
                this.surfaceCreated(sufi.getHolder());
            }
        }
    }

    private void stopCamera() {
        if(camera != null) {
            SurfaceView sufi = (SurfaceView) getActivity().findViewById(R.id.surfaceView);
            sufi.getHolder().removeCallback(this);
            sizes.clear();

            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"ShopCyclops");
        System.out.println(mediaStorageDir.toString());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                System.out.println("failed to create directory");
            }
        }
        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator+ "PreviewImage" +stream_id + ".jpg");

        return mediaFile;
    }

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureFile = getOutputMediaFile();

            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                System.out.println(e.toString());
            } catch (IOException e) {
                System.out.println(e.toString());
            }

            addImageToBucket task = new addImageToBucket();
            task.execute();

            continuePublishing();
        }
    };

    private class addImageToBucket extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( SECRETS.AWS_ACCESS_KEY, SECRETS.AWS_SECRET_KEY ) );
            PutObjectRequest putRequest = new PutObjectRequest( SECRETS.AWS_BUCKET_NAME, pictureFile.getName(), pictureFile );
            s3Client.putObject(putRequest);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    //called by record button//
    private void startPublishing() {
        if(!isPublishing) {
            snapshotText.setText("Preview pic being taken in:");
            snapshotText.setVisibility(View.VISIBLE);
            countdown.setVisibility(View.VISIBLE);
            new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long l) {
                    int secs = ((int)(l/1000));
                    String stringsecs = String.valueOf(secs);
                    countdown.setText(stringsecs);
                }

                @Override
                public void onFinish() {
                    snapshotText.setVisibility(View.INVISIBLE);
                    countdown.setVisibility(View.INVISIBLE);
                    camera.takePicture(null, null, myPictureCallback_JPG);
                }
            }.start();
        }
    }

    private void continuePublishing() {
        if(!isPublishing) {

            stream = new R5Stream(new R5Connection(configuration));
            stream.setLogLevel(R5Stream.LOG_LEVEL_DEBUG);

            stream.connection.addListener(new R5ConnectionListener() {
                @Override
                public void onConnectionEvent(R5ConnectionEvent event) {
                    Log.d("publish","connection event code "+event.value()+"\n");
                    switch(event.value()){
                        case 0://open
                            break;
                        case 1://close
                            break;
                        case 2://error
                            break;

                    }
                }
            });

            stream.setListener(new R5ConnectionListener() {
                @Override
                public void onConnectionEvent(R5ConnectionEvent event) {
                    switch (event) {
                        case CONNECTED:
                            break;
                        case DISCONNECTED:
                            break;
                        case START_STREAMING:
                            break;
                        case STOP_STRAMING:
                            break;
                        case CLOSE:
                            break;
                        case TIMEOUT:
                            break;
                        case ERROR:
                            break;
                    }
                }
            });

            camera.stopPreview();

            //assign the surface to show the camera output
            this.surfaceForCamera = (SurfaceView) getActivity().findViewById(R.id.surfaceView);
            stream.setView((SurfaceView) getActivity().findViewById(R.id.surfaceView));

            //add the camera for streaming
            if(selected_item != null) {
                Log.d("publisher","selected_item "+selected_item);
                String bits[] = selected_item.split("x");
                int pW= Integer.valueOf(bits[0]);
                int pH=  Integer.valueOf(bits[1]);
                if((pW/2) %16 !=0){
                    pW=320;
                    pH=240;
                }
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(pW, pH);
                camera.setParameters(parameters);
                r5Cam = new R5Camera(camera,pW,pH);
                r5Cam.setBitrate(SECRETS.RED_BITRATE);
            }
            else {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(320, 240);

                camera.setParameters(parameters);
                r5Cam = new R5Camera(camera,320,240);
                r5Cam.setBitrate(SECRETS.RED_BITRATE);
            }

            if(cameraSelection==1) {
                r5Cam.setOrientation(270);
            }
            else {
                r5Cam.setOrientation(0);
            }
            r5Mic = new R5Microphone();

            if(true) {
                stream.attachCamera(r5Cam);
            }

            if(true) {
                stream.attachMic(r5Mic);
            }


            isPublishing = true;
            stream.publish(getActivity().getIntent().getStringExtra(SECRETS.CURRENT_STREAM_TITLE)+getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0), R5Stream.RecordType.Live);
            camera.startPreview();

            try {
                SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                int stream_id = getActivity().getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
                prefs.edit().putInt(SECRETS.STREAM_PROGRESS, 2).apply();
                JSONObject wrapper = new JSONObject();
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("progress", 2);
                jsonParams.put("thumbnail_url", SECRETS.AWS_IMAGE_URL+ pictureFile.getName().toString());
                wrapper.put("stream", jsonParams);
                AsyncHttpClient client = new AsyncHttpClient();
                PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                client.setCookieStore(myCookieStore);
                client.addHeader("Accept", "application/json");
                client.addHeader("X-User-Token", token);
                client.addHeader("X-User-Email", user_email);
                StringEntity entity = new StringEntity(wrapper.toString());
                client.put(getActivity(), SECRETS.BASE_URL + "/streams/"+stream_id+"/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
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
    }

    private void stopPublishing() {
        if(stream!=null) {
            stream.stop();
        }
        isPublishing = false;
    }

    public void onClick(View view) {
        ImageButton rButton = (ImageButton) getActivity().findViewById(R.id.recordButton);
        ImageButton cameraButton = (ImageButton) getActivity().findViewById(R.id.cameraFlipper);

        if(view.getId() == R.id.recordButton) {
            if(isPublishing) {
                stopPublishing();
                rButton.setImageResource(R.drawable.red_dot);
                cameraButton.setVisibility(View.VISIBLE);
                try {
                    SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                    String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                    String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                    JSONObject wrapper = new JSONObject();
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("progress", 99);
                    wrapper.put("stream", jsonParams);
                    AsyncHttpClient client = new AsyncHttpClient();
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
                    client.setCookieStore(myCookieStore);
                    client.addHeader("Accept", "application/json");
                    client.addHeader("X-User-Token", token);
                    client.addHeader("X-User-Email", user_email);
                    StringEntity entity = new StringEntity(wrapper.toString());
                    client.put(getActivity(), SECRETS.BASE_URL + "/streams/"+stream_id+"/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
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
                    System.out.println(e.toString());
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("Stream Complete")
                        .setMessage("Now directing you to the checkout")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else {
                startPublishing();
                rButton.setImageResource(R.drawable.red_dot_stop);
                cameraButton.setVisibility(View.GONE);
            }
        }
        else if(view.getId() == R.id.cameraFlipper) {
            toggleCamera();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try{
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        };

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {}


}
