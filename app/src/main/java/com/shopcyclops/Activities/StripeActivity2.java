package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.thiagolocatelli.stripe.StripeApp;
import com.github.thiagolocatelli.stripe.StripeButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import com.stripe.Stripe;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by Andrew on 8/18/2015.
 */
public class StripeActivity2 extends Activity {

    private StripeApp mApp2;
    private StripeButton mStripeButton2;
    private ProgressBar stripeSpinner;

    AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);

        stripeSpinner = (ProgressBar) findViewById(R.id.stripeSpinner);

        mApp2 = new StripeApp(this, "StripeAccount", SECRETS.STRIPE_CLIENT_ID,
                SECRETS.STRIPE_SECRET_KEY, SECRETS.STRIPE_CALLBACK_URL, "read_write");
        mStripeButton2 = (StripeButton) findViewById(R.id.btnConnect2);
        mStripeButton2.setStripeApp(mApp2);
        mStripeButton2.setConnectMode(StripeApp.CONNECT_MODE.ACTIVITY);

        Stripe.apiKey = mApp2.getAccessToken();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(resultCode) {
            case StripeApp.RESULT_CONNECTED: {
                stripeSpinner.setVisibility(View.VISIBLE);
                try {
                    SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                    String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                    String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                    //int user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);
                    JSONObject wrapper = new JSONObject();
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("publishable_key", data.getStringExtra("STRIPE_PUBLISHABLE_KEY"));
                    jsonParams.put("provider", "stripe_connect");
                    jsonParams.put("uid", data.getStringExtra("STRIPE_USER_ID"));
                    jsonParams.put("access_code", data.getStringExtra("ACCESS_TOKEN"));
                    jsonParams.put("is_cyclops", true);
                    wrapper.put("user", jsonParams);
                    client = new AsyncHttpClient();
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
                    client.setCookieStore(myCookieStore);
                    client.addHeader("Accept", "application/json");
                    StringEntity entity = new StringEntity(wrapper.toString());
                    client.addHeader("X-User-Token", token);
                    client.addHeader("X-User-Email", user_email);
                    client.put(this, SECRETS.BASE_URL+"/users", entity, "application/json", new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                            Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                            System.out.println(json.toString());
                            stripeSpinner.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            System.out.println(json.toString());
                            SharedPreferences prefs2 = StripeActivity2.this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                            prefs2.edit().putBoolean(SECRETS.IS_CYCLOPS_KEY, true).apply();
                            Intent i = new Intent(StripeActivity2.this, StreamMainActivity.class);
                            startActivity(i);
                            StripeActivity2.this.finish();
                            stripeSpinner.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }
                break;
            }
            case StripeApp.RESULT_ERROR:
                String error_description = data.getStringExtra("error_description");
                Toast.makeText(StripeActivity2.this, error_description, Toast.LENGTH_SHORT).show();
                stripeSpinner.setVisibility(View.INVISIBLE);
                break;
        }

    }
}
