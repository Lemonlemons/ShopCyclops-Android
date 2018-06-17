package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.R;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by Andrew on 9/26/2015.
 */
public class ProfileActivity extends Activity {

    TextView firstName, lastName, displayName, email, phoneNumber;
    EditText editFirstName, editLastName, editDisplayName, editEmail, editPhoneNumber;
    SharedPreferences prefs;
    ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        firstName = (TextView)findViewById(R.id.firstNameText);
        lastName = (TextView)findViewById(R.id.lastNameText);
        displayName = (TextView)findViewById(R.id.displayNameText);
        email = (TextView)findViewById(R.id.emailText);
        phoneNumber = (TextView)findViewById(R.id.phoneNumberText);
        spinner = (ProgressBar) findViewById(R.id.progressProfileBar);

        prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        firstName.setText(prefs.getString(CONSTANTS.FIRST_NAME_KEY, ""));
        lastName.setText(prefs.getString(CONSTANTS.LAST_NAME_KEY, ""));
        displayName.setText(prefs.getString(CONSTANTS.DISPLAY_NAME_KEY, ""));
        email.setText(prefs.getString(CONSTANTS.EMAIL_KEY, ""));
        phoneNumber.setText(prefs.getString(CONSTANTS.PHONE_NUMBER_KEY, ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateAcct:
                updateAccount();
                return true;
            case R.id.signOut:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateAccount()
    {
        Intent i = new Intent(this, ProfileUpdateActivity.class);
        startActivity(i);
    }

    public void signOut()
    {
        try {
            spinner.setVisibility(View.VISIBLE);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", prefs.getString(CONSTANTS.TOKEN_KEY, null));
            client.addHeader("X-User-Email", prefs.getString(CONSTANTS.EMAIL_KEY, null));
            client.delete(this, CONSTANTS.BASE_URL + "/users/sign_out", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    spinner.setVisibility(View.INVISIBLE);
                    System.out.println(response.toString());
                    Toast.makeText(getApplicationContext(), "Wrong email or password, please try again", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        prefs.edit()
                                .remove(CONSTANTS.TOKEN_KEY)
                                .remove(CONSTANTS.EMAIL_KEY)
                                .remove(CONSTANTS.USER_ID_KEY)
                                .remove(CONSTANTS.FIRST_NAME_KEY)
                                .remove(CONSTANTS.LAST_NAME_KEY)
                                .remove(CONSTANTS.DISPLAY_NAME_KEY)
                                .remove(CONSTANTS.PHONE_NUMBER_KEY)
                                .remove(CONSTANTS.IS_ADMIN_KEY)
                                .remove(CONSTANTS.IS_CYCLOPS_KEY)
                                .apply();
                        Intent i = new Intent(ProfileActivity.this, StreamMainActivity.class);
                        startActivity(i);
                        spinner.setVisibility(View.INVISIBLE);
                        ProfileActivity.this.finish();
                    } catch (Exception e) {
                        spinner.setVisibility(View.INVISIBLE);
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
