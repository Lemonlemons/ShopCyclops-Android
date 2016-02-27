package com.shopcyclops.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;
import com.shopcyclops.Utils.GPSTracker;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Andrew on 7/22/2015.
 */
public class StreamSetupActivity extends Activity {

    private ActionBar actionBar;
    private EditText editTitle, editDescription, editStore;
    private TextView errorText;
    private ProgressBar setupProgress;
    //private Spinner editStyle;

    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamsetup);

        actionBar = getActionBar();

        editTitle = (EditText) findViewById(R.id.enter_title);
        editDescription = (EditText) findViewById(R.id.enter_description);
        editStore = (EditText) findViewById(R.id.enter_store);
        errorText = (TextView) findViewById(R.id.error);
        setupProgress = (ProgressBar) findViewById(R.id.streamSetupProgress);

        gps = new GPSTracker(this);

        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.styles_array, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //editStyle.setAdapter(adapter);
    }

    public void Setup(View v)
    {
        //String style = editStyle.getSelectedItem().toString();
        final String title = editTitle.getText().toString();
        final String description = editDescription.getText().toString();
        final String store = editStore.getText().toString();
        if(title.equals("")) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Missing Title");
        }
        else if(description.equals("")) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Missing Description");
        }
        else if(store.equals("")) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Missing Store Info");
        }
        else if(!gps.canGetLocation()) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Can't get GPS lock, please turn on your GPS");
        }
        else {
            setupProgress.setVisibility(View.VISIBLE);
            try {
                final SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                int user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);
                String stream_url = "rtsp://"+SECRETS.RED_HOST+":"+SECRETS.RED_PORT+"/"+SECRETS.RED_APP_NAME+"/"+title;
                JSONObject wrapper = new JSONObject();
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("name", title);
                jsonParams.put("description", description);
                jsonParams.put("store", store);
                jsonParams.put("lat", gps.getLatitude());
                jsonParams.put("lng", gps.getLongitude());
                jsonParams.put("host_user_id", user_id);
                jsonParams.put("url", stream_url);
                wrapper.put("stream", jsonParams);
                AsyncHttpClient client = new AsyncHttpClient();
                PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
                client.setCookieStore(myCookieStore);
                client.addHeader("Accept", "application/json");
                client.addHeader("X-User-Token", token);
                client.addHeader("X-User-Email", user_email);
                StringEntity entity = new StringEntity(wrapper.toString());
                client.post(this, SECRETS.BASE_URL+"/streams", entity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                        Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(json.toString());
                        setupProgress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                        System.out.println(json.toString());
                        try {
                            Intent i = new Intent(StreamSetupActivity.this, ShoppingActivity.class);
                            i.putExtra(SECRETS.CURRENT_STREAM_TITLE, title);
                            i.putExtra(SECRETS.CURRENT_STREAM_DESCRIPTION, description);
                            i.putExtra(SECRETS.CURRENT_STREAM_STORE, store);
                            i.putExtra(SECRETS.CURRENT_STREAM_ID, json.getInt("id"));
                            prefs.edit().putInt(SECRETS.STREAM_PROGRESS, 1).apply();
                            startActivity(i);
                            setupProgress.setVisibility(View.INVISIBLE);
                            StreamSetupActivity.this.finish();
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
    }
}
