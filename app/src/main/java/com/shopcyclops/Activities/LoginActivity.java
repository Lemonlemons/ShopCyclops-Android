package com.shopcyclops.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Andrew on 8/11/2015.
 */
public class LoginActivity extends Activity {
    private ActionBar actionBar;
    private EditText editEmail, editPassword;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editEmail = (EditText) findViewById(R.id.email);
        editPassword = (EditText) findViewById(R.id.password);
        spinner = (ProgressBar) findViewById(R.id.progressSpin);
    }

    public void Login(View v) {
        spinner.setVisibility(View.VISIBLE);
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        if (email.equals("")) {
            Toast.makeText(getApplicationContext(), "Email is empty", Toast.LENGTH_LONG).show();
        } else if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_LONG).show();
        } else {
            try {
                JSONObject wrapper = new JSONObject();
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("password", password);
                jsonParams.put("remember_me", "0");
                wrapper.put("user", jsonParams);
                AsyncHttpClient client = new AsyncHttpClient();
                PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
                client.setCookieStore(myCookieStore);
                client.addHeader("Accept", "application/json");
                StringEntity entity = new StringEntity(wrapper.toString());
                client.post(this, SECRETS.BASE_URL+"/users/sign_in", entity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                        spinner.setVisibility(View.INVISIBLE);
                        System.out.println(response.toString());
                        Toast.makeText(getApplicationContext(), "Wrong email or password, please try again", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            SharedPreferences prefs = getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
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
                                    .putString(SECRETS.TOKEN_KEY, token)
                                    .putInt(SECRETS.USER_ID_KEY, user_id)
                                    .putString(SECRETS.EMAIL_KEY, user_email)
                                    .putString(SECRETS.FIRST_NAME_KEY, first_name)
                                    .putString(SECRETS.LAST_NAME_KEY, last_name)
                                    .putString(SECRETS.DISPLAY_NAME_KEY, display_name)
                                    .putString(SECRETS.PHONE_NUMBER_KEY, phone_number)
                                    .putBoolean(SECRETS.IS_ADMIN_KEY, is_admin)
                                    .putBoolean(SECRETS.IS_CYCLOPS_KEY, is_cyclops)
                                    .apply();
                            //for (int i = 0; i < headers.length; i++) {
                            //   System.out.println(headers[i].toString());
                            //}
                            Intent i = new Intent(LoginActivity.this, StreamMainActivity.class);
                            spinner.setVisibility(View.INVISIBLE);
                            startActivity(i);
                            LoginActivity.this.finish();
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

    public void SignUp(View v)
    {
        Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(i);
    }

}
