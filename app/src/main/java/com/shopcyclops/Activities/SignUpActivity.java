package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Andrew on 8/13/2015.
 */
public class SignUpActivity extends Activity {

    private EditText editEmail, editPassword, editDisplayname, editPasswordConfirm;
    private ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        editEmail = (EditText) findViewById(R.id.email);
        editPassword = (EditText) findViewById(R.id.password);
        editDisplayname = (EditText) findViewById(R.id.displayname);
        editPasswordConfirm  = (EditText) findViewById(R.id.password_confirmation);
        spinner = (ProgressBar) findViewById(R.id.progressSpin);
    }

    public void Cancel(View v)
    {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void SignUp(View v)
    {
        spinner.setVisibility(View.VISIBLE);
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String displayname = editDisplayname.getText().toString();
        String passwordconfirmation = editPasswordConfirm.getText().toString();
        if (email.equals("")) {
            Toast.makeText(getApplicationContext(), "Email is empty", Toast.LENGTH_LONG).show();
        } else if (displayname.equals("")) {
            Toast.makeText(getApplicationContext(), "Displayname is empty", Toast.LENGTH_LONG).show();
        } else if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_LONG).show();
        } else if (passwordconfirmation.equals("")) {
            Toast.makeText(getApplicationContext(), "Password Confirmation is empty", Toast.LENGTH_LONG).show();
        } else {
            try {
                JSONObject wrapper = new JSONObject();
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("displayname", displayname);
                jsonParams.put("password", password);
                jsonParams.put("password_confirmation", passwordconfirmation);
                wrapper.put("user", jsonParams);
                AsyncHttpClient client = new AsyncHttpClient();
                PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
                client.setCookieStore(myCookieStore);
                client.addHeader("Accept", "application/json");
                StringEntity entity = new StringEntity(wrapper.toString());
                System.out.println(wrapper.toString());
                client.post(this, CONSTANTS.BASE_URL+"/users", entity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                        spinner.setVisibility(View.INVISIBLE);
                        System.out.println(throwable.toString());
                        Toast.makeText(getApplicationContext(), "Wrong email or password, please try again", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            SharedPreferences prefs = SignUpActivity.this.getSharedPreferences(CONSTANTS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                            String token = response.getString("authentication_token");
                            int user_id = response.getInt("id");
                            String user_email = response.getString("email");
                            String first_name = response.getString("firstname");
                            String last_name = response.getString("lastname");
                            String display_name = response.getString("displayname");
                            String phone_number = response.getString("phonenumber");
                            boolean is_admin = response.getBoolean("is_admin");
                            boolean is_cyclops = response.getBoolean("is_cyclops");
                            prefs.edit()
                                    .putString(CONSTANTS.TOKEN_KEY, token)
                                    .putInt(CONSTANTS.USER_ID_KEY, user_id)
                                    .putString(CONSTANTS.EMAIL_KEY, user_email)
                                    .putString(CONSTANTS.FIRST_NAME_KEY, first_name)
                                    .putString(CONSTANTS.LAST_NAME_KEY, last_name)
                                    .putString(CONSTANTS.DISPLAY_NAME_KEY, display_name)
                                    .putString(CONSTANTS.PHONE_NUMBER_KEY, phone_number)
                                    .putBoolean(CONSTANTS.IS_ADMIN_KEY, is_admin)
                                    .putBoolean(CONSTANTS.IS_CYCLOPS_KEY, is_cyclops)
                                    .apply();
                            Intent i = new Intent(SignUpActivity.this, StreamMainActivity.class);
                            spinner.setVisibility(View.INVISIBLE);
                            startActivity(i);
                        }
                        catch (Exception e)
                        {
                            spinner.setVisibility(View.INVISIBLE);
                            System.out.println(e.toString());
                        }
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
