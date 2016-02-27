package com.shopcyclops.Activities;

/**
 * Created by Andrew on 7/22/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class SplashScreenActivity extends Activity {

    //// Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();

        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        client.addHeader("Accept", "application/json");
        client.get(this, SECRETS.BASE_URL+"/mobileallstreams", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                System.out.println(throwable.toString());
                Intent i = new Intent(SplashScreenActivity.this, StreamMapActivity.class);
                startActivity(i);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json)
            {
                Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                System.out.println(throwable.toString());
                Intent i = new Intent(SplashScreenActivity.this, StreamMapActivity.class);
                startActivity(i);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                // This method will be executed once the timer is over
                // Start your app main activity
                final SharedPreferences prefs = SplashScreenActivity.this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                Intent i = new Intent(SplashScreenActivity.this, StreamMapActivity.class);
                prefs.edit().putString(SECRETS.ALL_STREAMS, json.toString()).apply();
                startActivity(i);

                // close this activity
                finish();
            }
        });
    }

}
