package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;

/**
 * Created by Andrew on 8/15/2015.
 */
public class TransitionActivity extends Activity {

    AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        checkcyclops();

    }

    public void checkcyclops()
    {
        SharedPreferences prefs = this.getSharedPreferences("com.shopcyclops", Context.MODE_PRIVATE);
        String token = prefs.getString(SECRETS.TOKEN_KEY, null);
        String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        int user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);
        client = new AsyncHttpClient();
        client.addHeader("Content-Type","application/json");
        client.addHeader("X-User-Token", token);
        client.addHeader("X-User-Email", user_email);
        client.get("http://5a3d710f.ngrok.com/users/edit", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                System.out.println(responseString);
                for (int i = 0; i < headers.length; i++) {
                    System.out.println(headers[i].toString());
                }
                Intent i = new Intent(TransitionActivity.this, StreamSetupActivity.class);
                startActivity(i);
            }
        });
    }
}
