package com.shopcyclops.Activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.shopcyclops.Adapters.StreamMainAdapter;
import com.shopcyclops.Fragments.Stream.StreamGridFragment;
import com.shopcyclops.Fragments.Stream.StreamListFragment;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

public class StreamMainActivity extends FragmentActivity implements android.app.ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     *
     * asdfsadf
     */
    StreamMainAdapter mSectionsPagerAdapter;
    private ListView mDrawerList;
    private ArrayAdapter<String> settingsAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private StreamGridFragment gridFragment;
    private StreamListFragment listFragment;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new StreamMainAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        gridFragment = (StreamGridFragment) mSectionsPagerAdapter.getItem(0);
        listFragment = (StreamListFragment) mSectionsPagerAdapter.getItem(1);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        setupDrawer();

        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();

        final ActionBar actionBar = getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        actionBar.addTab(actionBar.newTab().setText("GRID").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("LIST").setTabListener(this));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isNetworkAvailable()) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {
                case R.id.refresh:
                    if (0 == mViewPager.getCurrentItem()) {
                        gridFragment.refreshlist();
                    } else {
                        listFragment.refreshlist();
                    }
                    return true;
                case R.id.menu:
                    toggleActionBarMenu();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet")
                    .setMessage("Please connect to the internet to view streams")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toggleActionBarMenu() {
        if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        else
            mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    private void addDrawerItems() {
        SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final String token = prefs.getString(SECRETS.TOKEN_KEY, null);
        List<String> optionsArray = new ArrayList<String>();
        if (token == null) {
            optionsArray.add("SIGN UP");
            optionsArray.add("LOGIN");
        } else {
            optionsArray.add("MY PROFILE");
            optionsArray.add("CREATE BROADCAST");
            optionsArray.add("PAYMENT");
            optionsArray.add("MY ORDERS");
        }
        settingsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, optionsArray);
        mDrawerList.setAdapter(settingsAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (token == null) {
                    switch (position) {
                        case 0:
                            gotosignup();
                            break;
                        case 1:
                            gotologin();
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            gotoprofile();
                            break;
                        case 1:
                            createBroadcast();
                            break;
                        case 2:
                            gotopaymentinfo();
                            break;
                        case 3:
                            gotoorderinfo();
                            break;
                    }
                }
            }
        });
    }

    public void gotosignup()
    {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    public void gotologin()
    {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void gotoprofile()
    {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    public void createBroadcast()
    {
        SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        boolean is_cyclops = prefs.getBoolean(SECRETS.IS_CYCLOPS_KEY, false);
        if (is_cyclops == true)
        {
            Intent i = new Intent(this, StreamSetupActivity.class);
            startActivity(i);
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Become a Cyclops")
                    .setMessage("Complete your registration to create your own broadcasts!")
                    .setPositiveButton("Complete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(StreamMainActivity.this, StripeActivity2.class);
                            startActivity(i);
                            StreamMainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(R.drawable.ic_launcher)
                    .show();
        }
    }

    public void gotopaymentinfo()
    {
        Intent i = new Intent(this, PaymentInfoActivity.class);
        startActivity(i);
    }

    public void gotoorderinfo()
    {
        Intent i = new  Intent(this, OrderInfoActivity.class);
        startActivity(i);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onTabSelected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }
}
