package com.shopcyclops.Activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;
import com.shopcyclops.Adapters.ShoppingAdapter;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;


public class ShoppingActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private ShoppingAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = {"Chat", "Stream", "Cart"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        viewPager = (ViewPager)findViewById(R.id.pager);
        actionBar = getActionBar();
        actionBar.hide();
        mAdapter = new ShoppingAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);

        //actionBar.setHomeButtonEnabled(false);

        //Adding Tabs
        //for (String tab_name : tabs)
        //{
        //    actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        //}

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                //actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });



    }

    public void addToCart (View v) {
        LinearLayout parentRow = (LinearLayout)v.getParent();
        LinearLayout titleRow = (LinearLayout)parentRow.getChildAt(0);

        TextView hello = (TextView)titleRow.getChildAt(0);
        System.out.println(hello.getText());

        LinearLayout textRow = (LinearLayout)titleRow.getChildAt(1);

        TextView asdf = (TextView)textRow.getChildAt(5);
        System.out.println(asdf.getText());
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft){

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
    //
    @Override
    public void onStop() {
        try {
            SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String token = prefs.getString(SECRETS.TOKEN_KEY, null);
            String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
            int stream_id = this.getIntent().getIntExtra(SECRETS.CURRENT_STREAM_ID, 0);
            JSONObject wrapper = new JSONObject();
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("progress", 99);
            wrapper.put("stream", jsonParams);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            StringEntity entity = new StringEntity(wrapper.toString());
            client.put(this, SECRETS.BASE_URL + "/streams/"+stream_id+"/mobileupdate", entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                    System.out.println(throwable.toString());
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
        super.onStop();
        Fragment fragment = mAdapter.getItem(1);
        fragment.onDestroy();
        this.finish();
    }
}
