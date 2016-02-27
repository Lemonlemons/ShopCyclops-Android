package com.shopcyclops.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

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

        prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        firstName.setText(prefs.getString(SECRETS.FIRST_NAME_KEY, ""));
        lastName.setText(prefs.getString(SECRETS.LAST_NAME_KEY, ""));
        displayName.setText(prefs.getString(SECRETS.DISPLAY_NAME_KEY, ""));
        email.setText(prefs.getString(SECRETS.EMAIL_KEY, ""));
        phoneNumber.setText(prefs.getString(SECRETS.PHONE_NUMBER_KEY, ""));
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
            client.addHeader("X-User-Token", prefs.getString(SECRETS.TOKEN_KEY, null));
            client.addHeader("X-User-Email", prefs.getString(SECRETS.EMAIL_KEY, null));
            client.delete(this, SECRETS.BASE_URL + "/users/sign_out", new JsonHttpResponseHandler() {
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
                                .remove(SECRETS.TOKEN_KEY)
                                .remove(SECRETS.EMAIL_KEY)
                                .remove(SECRETS.USER_ID_KEY)
                                .remove(SECRETS.FIRST_NAME_KEY)
                                .remove(SECRETS.LAST_NAME_KEY)
                                .remove(SECRETS.DISPLAY_NAME_KEY)
                                .remove(SECRETS.PHONE_NUMBER_KEY)
                                .remove(SECRETS.IS_ADMIN_KEY)
                                .remove(SECRETS.IS_CYCLOPS_KEY)
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
