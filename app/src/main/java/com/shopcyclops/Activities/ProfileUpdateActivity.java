package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.CONSTANTS;
import com.shopcyclops.R;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by Andrew on 9/27/2015.
 */
public class ProfileUpdateActivity extends Activity {

    EditText editFirstName, editLastName, editDisplayName, editEmail, editPhoneNumber;
    SharedPreferences prefs;
    Button update;
    ProgressBar updateProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        editFirstName = (EditText)findViewById(R.id.firstNameEdit);
        editLastName = (EditText)findViewById(R.id.lastNameEdit);
        editDisplayName = (EditText)findViewById(R.id.displayNameEdit);
        editEmail = (EditText)findViewById(R.id.emailEdit);
        editPhoneNumber = (EditText)findViewById(R.id.phoneNumberEdit);

        prefs = this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        editFirstName.setText(prefs.getString(CONSTANTS.FIRST_NAME_KEY, ""));
        editLastName.setText(prefs.getString(CONSTANTS.LAST_NAME_KEY, ""));
        editDisplayName.setText(prefs.getString(CONSTANTS.DISPLAY_NAME_KEY, ""));
        editEmail.setText(prefs.getString(CONSTANTS.EMAIL_KEY, ""));
        editPhoneNumber.setText(prefs.getString(CONSTANTS.PHONE_NUMBER_KEY, ""));

        update = (Button) findViewById(R.id.btnUpdate);
        updateProgress = (ProgressBar) findViewById(R.id.updateProgress);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProgress.setVisibility(View.VISIBLE);
                try {
                    AsyncHttpClient client = new AsyncHttpClient();
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(ProfileUpdateActivity.this);
                    client.setCookieStore(myCookieStore);
                    client.addHeader("Accept", "application/json");
                    client.addHeader("X-User-Token", prefs.getString(CONSTANTS.TOKEN_KEY, null));
                    client.addHeader("X-User-Email", prefs.getString(CONSTANTS.EMAIL_KEY, null));
                    JSONObject wrapper = new JSONObject();
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("firstname", editFirstName.getText().toString());
                    jsonParams.put("lastname", editLastName.getText().toString());
                    jsonParams.put("phonenumber", editPhoneNumber.getText().toString());
                    jsonParams.put("email", editEmail.getText().toString());
                    jsonParams.put("displayname", editDisplayName.getText().toString());
                    wrapper.put("user", jsonParams);
                    StringEntity entity = new StringEntity(wrapper.toString());
                    client.put(ProfileUpdateActivity.this, CONSTANTS.BASE_URL + "/users", entity, "application/json", new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                            Toast.makeText(ProfileUpdateActivity.this, throwable.toString(), Toast.LENGTH_LONG);
                            System.out.println(throwable.toString());
                            updateProgress.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String user_email = editEmail.getText().toString();
                                String first_name = editFirstName.getText().toString();
                                String last_name = editLastName.getText().toString();
                                String display_name = editDisplayName.getText().toString();
                                String phone_number = editPhoneNumber.getText().toString();
                                prefs.edit()
                                        .putString(CONSTANTS.EMAIL_KEY, user_email)
                                        .putString(CONSTANTS.FIRST_NAME_KEY, first_name)
                                        .putString(CONSTANTS.LAST_NAME_KEY, last_name)
                                        .putString(CONSTANTS.DISPLAY_NAME_KEY, display_name)
                                        .putString(CONSTANTS.PHONE_NUMBER_KEY, phone_number)
                                        .apply();
                                Intent i = new Intent(ProfileUpdateActivity.this, ProfileActivity.class);
                                startActivity(i);
                                ProfileUpdateActivity.this.finish();
                            }
                            catch (Exception e) {
                                System.out.print(e.toString());
                            }
                            updateProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                catch (Exception e) {
                    System.out.print(e.toString());
                    updateProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
