package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 9/13/2015.
 */
public class PaymentInfoActivity extends Activity {
    ListView cardList;
    List cards;
    ArrayAdapter<String> adapter;
    ProgressBar paymentProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymentinfo);

        // Set the adapter
        cardList = (ListView) findViewById(R.id.cardlist);
        paymentProgress = (ProgressBar) findViewById(R.id.paymentProgress);

        cards = new ArrayList();

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, cards);


        // Assign adapter to ListView
        cardList.setAdapter(adapter);

        // ListView Item Click Listener
//        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            }
//
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getCards();
    }

    public void getCards() {
        try {
            paymentProgress.setVisibility(View.VISIBLE);
            final SharedPreferences prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String token = prefs.getString(CONSTANTS.TOKEN_KEY, null);
            String user_email = prefs.getString(CONSTANTS.EMAIL_KEY, null);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            client.get(this, CONSTANTS.BASE_URL+"/mobilecardsindex", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(json.toString());
                    paymentProgress.setVisibility(View.GONE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    try {
                        JSONArray data = json.getJSONArray("data");
                        System.out.println(data.toString());
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject opper = data.getJSONObject(i);
                                String card = opper.getString("brand")+" PERSONAL **** "+opper.getString("last4");
                                System.out.println(card);
                                cards.add(card);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                    catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    paymentProgress.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e) {
            System.out.println(e.toString());
            paymentProgress.setVisibility(View.GONE);
        }
    }

    public void addCard(View v) {
        Intent i = new Intent(PaymentInfoActivity.this, EnterPaymentActivity.class);
        startActivity(i);
    }
}
