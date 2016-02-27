package com.shopcyclops.Activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.shopcyclops.R;
import com.shopcyclops.Adapters.ViewerAdapter;

/**
 * Created by Andrew on 8/15/2015.
 */
public class ViewerActivity extends FragmentActivity {
    private ViewPager viewPager;
    private ViewerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = {"Chat", "Stream", "Cart"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        Intent intent = getIntent();
        int level = intent.getIntExtra("LEVEL", 0);
        String mediaUrl = intent.getStringExtra("URL");

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        actionBar = getActionBar();
        actionBar.hide();
        mAdapter = new ViewerAdapter(getSupportFragmentManager(), level, mediaUrl);

        viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        Fragment fragment = mAdapter.getItem(2);
        fragment.onDestroy();
        this.finish();
    }

    public void Login(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        this.finish();
    }

    public void SignUp(View v) {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
        this.finish();
    }
}
